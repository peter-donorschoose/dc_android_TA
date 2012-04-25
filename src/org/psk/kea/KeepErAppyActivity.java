package org.psk.kea;

import org.psk.kea.reminder.ReminderBuilder;
import org.psk.kea.weather.WeatherFacade;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Pete The main activity of the application. Acts as controller.
 * From here all the app's functions are commenced: 
 * - message composition,
 * - preferences, 
 * - viewing help, 
 * - recent compositions 
 * - about box.
 * 
 * 
 */
public class KeepErAppyActivity extends Activity implements
		View.OnClickListener {

	static final String REMINDER_ENABLED = "REMINDER_ENABLED";
	
	private static final int SETTINGS_REQUEST_CODE = 771548739; // random number
	private WeatherFacade _weather;
	private ReminderBuilder _reminder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 * 
	 * OK so the user has (potentially after some time) selected the KEA
	 * Notification and KEA has been brought to front. Now we need to schedule
	 * the next reminder.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(Util.TAG, "onNewIntent");

		final String originator = intent.getStringExtra(getString(R.string.originator));
		if ((originator != null) && originator.equals("Notifier")) {
			// don't notify user. mandatory roll to next date since the reminder's
			// notification has just been clicked - we do not want another reminder
			// sent today.
			setReminder(false, true);
		}
	}

	/**
	 * retrieve the informative text from the reminder object and pop it into
	 * the status tray.
	 */
	private void updateReminderTray() {
		String infoText = _reminder.getInfoText();
		TextView tv = (TextView)findViewById(R.id.reminder_info);
		tv.setText(infoText);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(Util.TAG, "onDestroy()");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * Perform app-wide initialization of weather and reminder subsystems.
	 * Dis/En/able the Recents button, depending on existence of prefs. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.i(Util.TAG, "onCreate()");

		runOnceOnly();

		doPostInstallConfig();

		enableRecentsButton();

		initializeWeather(); // weather may have changed since app was last
								// launched
	}

	/**
	 * code that should run once during the lifetime of this object
	 */
	private void runOnceOnly() {

		if (_weather != null)
			return;
		
		Log.i(Util.TAG,"runOnceOnly()");

		// short kiss-kiss sfx (can be disabled)
		Music.play(this, R.raw.kisses);

		_weather = new WeatherFacade(this);
		
		_reminder = new ReminderBuilder(this);

		// don't notify user, no mandatory roll to next date
		setReminder(false, true);
		
		hookAsListenerOfButtons();
	}

	/**
	 * runs in a new thread so that UI does not lock-up should the weather take
	 * some time to retrieve.
	 */
	private void initializeWeather() {

		Runnable run = new Runnable() {
			public void run() {
				_weather.fetchCurrent();
				_weather.prompt();
			}
		};

		Thread weatherThread = new Thread(run, "WeatherThread");
		weatherThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(Util.TAG, "onPause()");

		Music.stop(this);	// not necessary but good form.
		_weather.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(Util.TAG, "onResume()");

		_weather.resume();
		// no music to resume - it's just a short SFX
	}

	/**
	 * enable the Recents button only if there have been previous emails
	 * composed.
	 */
	private void enableRecentsButton() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		final String s = new String(prefs.getString(
				Util.SUBJECT_RECENT_PREF + 1, ""));
		findViewById(R.id.recents_button).setEnabled(
				!(s.equals("") || s.equals(" ...")));
	}

	/**
	 * if this is the first time the app has run on this device since
	 * installation, prompt the user to enter some settings.
	 */
	private void doPostInstallConfig() {
		if (ResourceUtil.isFirstRun(this)) {
			final String help = "Welcome to Keep Er Appy. "
					+ "Take a moment to fill in a few bits of "
					+ "information about yourself.";
			Toast.makeText(getApplicationContext(), help, Toast.LENGTH_LONG)
					.show();

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onWindowFocusChanged(boolean) When we return to
	 * the screen, the Recents button may need to be
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
			enableRecentsButton();
	}

	/**
	 * this activity wants to listen to all its own UI widgets.
	 */
	private void hookAsListenerOfButtons() {
		findViewById(R.id.baked_messages_button).setOnClickListener(this);
		findViewById(R.id.compose_button).setOnClickListener(this);
		findViewById(R.id.about_button).setOnClickListener(this);
		findViewById(R.id.settings_button).setOnClickListener(this);
		findViewById(R.id.recents_button).setOnClickListener(this);
		findViewById(R.id.help_button).setOnClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * determine what was clicked and fire off the appropriate action.
	 */
	@Override
	public void onClick(View v) {
		Intent intent = null;

		switch (v.getId()) {
		case R.id.compose_button: {
			intent = new Intent(this, ComposeActivity.class);
			break;
		}
		case R.id.baked_messages_button: {
			intent = new Intent(this, BakedMessagesActivity.class);
			break;
		}
		case R.id.about_button: {
			intent = new Intent(this, AboutActivity.class);
			break;
		}
		case R.id.settings_button: {
			intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_REQUEST_CODE);
			intent = null; // so that we don't call startActivity(intent) below
			break;
		}
		case R.id.recents_button: {
			intent = new Intent(this, RecentsActivity.class);
			break;
		}
		case R.id.help_button: {
			intent = new Intent(this, HelpActivity.class);
			break;
		}
		default:
			Log.d(Util.TAG, "Unexpected view was clicked");
		};

		if (intent != null)
			startActivity(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult
	 * The user's finished with the prefs screen - handle their input
	 */ 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		assert (requestCode == SETTINGS_REQUEST_CODE);
		// do notify user, no mandatory roll to next date
		setReminder(true, false);
	}

	private void setReminder(boolean notifyUser, boolean doRoll) {
		_reminder.setReminder(notifyUser, doRoll); 
		updateReminderTray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int) The Help dialog is the only
	 * dialog that could have been created.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.v(Util.TAG, "onCreateDialog()");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.help_text))
				.setCancelable(false).setPositiveButton("OK", null);
		AlertDialog alert = builder.create();

		return alert;
	}
}