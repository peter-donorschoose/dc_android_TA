package org.psk.kea;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * @author Pete
 * Screen for choosing a subject line, greeting and sign off combination. 
 * These are pulled from shared preferences. When a message is composed
 * it records the combination as a "recent" for quick future access.
 * 
 * If the "Create shortcut?" box is checked, a shortcut is placed on the Home
 * screen which upon clicking, fires a message compose intent with the selected
 * subject, greeting and sign-off.
 */
public class ComposeActivity extends Activity implements OnClickListener {

	private SharedPreferences _prefs;
	private Sender _sender;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);

		_sender = new Sender(this);
		_prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		populateSpinner("etpSignOff", R.id.sign_off_spinner);
		populateSpinner("etpSignIn", R.id.signInSpinner);
		populateSpinner("etpSubject", R.id.subjectSpinner);

		Button compBut = (Button) findViewById(R.id.compose_compose_button);
		compBut.setOnClickListener(this);
	}

	/**
	 * @param prefPrefix
	 *            - e.g. subject, sign-in, sign-off
	 * @param spinnerID
	 *            - spinner UI component to populate
	 */
	private void populateSpinner(final String prefPrefix, final int spinnerID) {
		ArrayList<String> prefs = getPrefList(prefPrefix);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list_item, prefs);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner subSpin = (Spinner) findViewById(spinnerID);
		subSpin.setAdapter(adapter);
	}

	/**
	 * @param keyPrefix
	 *            - e.g. subject, sign-in, sign-off
	 * @return fetch keyPrefix1 through keyPrefix4
	 */
	private ArrayList<String> getPrefList(final String keyPrefix) {
		ArrayList<String> pl = new ArrayList<String>();

		for (int i = 1; i < Util.NUM_RECENTS; i++) {
			final String keyN = keyPrefix + (new Integer(i)).toString();
			pl.add(_prefs.getString(keyN, ""));
		}
		return pl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View) Called
	 * when Compose button is clicked. Extract the user's selections, populate a
	 * HappyEmail parameter object and send it. Add the selections to the
	 * recents list and create the shortcut if necessary.
	 */
	@Override
	public void onClick(View view) {
		final String email = _prefs.getString("etpHerEmail1", "");

		String sub = (String) ((Spinner) findViewById(R.id.subjectSpinner))
				.getSelectedItem();
		String si = (String) ((Spinner) findViewById(R.id.signInSpinner))
				.getSelectedItem();
		String so = (String) ((Spinner) findViewById(R.id.sign_off_spinner))
				.getSelectedItem();

		final String body = si + "\n" + so;

		HappyEmail he = new HappyEmail(email, sub, body);

		final boolean success = _sender.send(he);

		addSelectionsToRecents(he);

		final CheckBox cb = (CheckBox) findViewById(R.id.create_shortcut_checkbox);

		if (success && cb.isChecked()) {
			createShortcut(he);
		}

	}

	/**
	 * @param he - parameter object containing the email details
	 */
	private void createShortcut(final HappyEmail he) {
		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");

		Intent emailIntent = _sender.buildEmailIntent(he);

		// Shortcut name is KeepErAppy. Allow multiple shortcuts - this is something
		// the user might want.
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		shortcutIntent.putExtra("duplicate", true);

		// tell the shortcut intent what it's to do when clicked - compose an email
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, emailIntent);

		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
				this, R.drawable.beer_shortcut);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

		// shout that we want a shortcut created
		sendBroadcast(shortcutIntent);
	}

	/**
	 * Removes recent5. Demotes recents 1-4. Then adds this email at the top.
	 * @param he - the email information to be put at the top of the recents list
	 * (MRU list). 
	 */
	private void addSelectionsToRecents(final HappyEmail he) {
		// "checkout" the preferences for editing.
		Editor ed = _prefs.edit();

		for (int i = Util.NUM_RECENTS - 1; i > 0; i--) {
			demoteRecent(i, ed);
		}

		putRecent(ed, he, 1);

		ed.commit();
	}

	/**
	 * Get the next most recent after i and copy it to recent i + 1.
	 * @param i - index of recent to be demoted
	 * @param ed - our "checkout" of shared prefs
	 */
	private void demoteRecent(final int i, Editor ed) {
		final String eAddr = _prefs.getString("etpHerEmail1", "");

		HappyEmail he = new HappyEmail(eAddr, 
				_prefs.getString(Util.SUBJECT_RECENT_PREF + i, ""), 
				_prefs.getString(Util.BODY_RECENT_PREF + i, ""));
		
		putRecent(ed, he, i + 1);
	}

	/**
	 * Obtain the i'th recent pref keys and store the HappyEmail to them.
	 * @param ed - our "checkout" of the prefs
	 * @param he - paramter object containing the info of the most recent compose action
	 * @param i - the i'th recent keys to obtain for storing. 
	 */
	private void putRecent(Editor ed, HappyEmail he, final int i) {
		final String subTargetKey = Util.SUBJECT_RECENT_PREF + i;
		final String siTargetKey = Util.BODY_RECENT_PREF + i;

		ed.putString(subTargetKey, he.getSubject());
		ed.putString(siTargetKey, he.getBody());
	}
}
