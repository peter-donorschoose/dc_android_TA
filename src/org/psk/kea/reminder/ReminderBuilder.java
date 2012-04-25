package org.psk.kea.reminder;

import java.text.DateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.psk.kea.R;
import org.psk.kea.Util;
import org.psk.kea.widget.TimePreference;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Encapsulates the mechanics of validating the user's reminder
 * input from the prefs screen and computing a suitable time for the
 * next reminder. The reminder time will be some time between the
 * earliest and latest configured times. If the earliest time > the
 * latest time, then it's assumed that the user intended an inter-day
 * reminder window. A toast notifies the user of the next reminder time.

 * @author Pete 
 */
public class ReminderBuilder {

	private static final int BCAST_REQUEST_ID = 771548738; // random number

	static String[] DAYS_OF_WEEK = new String[] { "Sun", "Mon", "Tue", "Wed",
			"Thu", "Fri", "Sat" };

	private Activity _act;
	private Context _ctx;
	private SharedPreferences _prefs;
	private Calendar _cal;

	/**
	 * @param act
	 *            - stash this since its context reference is required
	 */
	public ReminderBuilder(Activity act) {
		_act = act;
		_ctx = act.getBaseContext();
		_prefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
		// leave _endabled as null
	}

	/**
	 * If reminders are enabled, set the alarm that shows the KEA notification.
	 * Else, cancel any existing reminders.
	 * 
	 * @param inform
	 *            - show the Toast only if we're returning from Settings
	 * @param doRoll
	 *            - must we roll to the next reminder date?
	 * @return true if a reminder was set
	 */
	public void setReminder(boolean inform, boolean doRoll) {
		Intent intent = new Intent(_act, Notifier.class);

		AlarmManager am = (AlarmManager) _ctx
				.getSystemService(Context.ALARM_SERVICE);

		if (!isEnabled()) {
			PendingIntent pending = PendingIntent
					.getBroadcast(_ctx, BCAST_REQUEST_ID, intent,
							PendingIntent.FLAG_CANCEL_CURRENT);
			if (pending != null) {
				am.cancel(pending);
				pending.cancel();
			}

			_cal = null;
		} else {
			// get a Calendar object with current time
			_cal = getNextReminderTime(doRoll);
			PendingIntent pending = PendingIntent
					.getBroadcast(_ctx, BCAST_REQUEST_ID, intent,
							PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, _cal.getTimeInMillis(), pending);

			Log.i(Util.TAG, getInfoText());
		}
	}

	/**
	 * Interrogate the prefs, the current time and come up with a random(ish)
	 * alarm time between the earliest and latest times specified.
	 * 
	 * @param roll
	 *            - must we roll to the next date?
	 * @return - Calendar object set to the next alarm time.
	 */
	private Calendar getNextReminderTime(boolean doRoll) {
		final int alarmTimeMins = calcReminderTimeInMins();

		final int alarmHours = alarmTimeMins / 60;
		final int alarmMins = alarmTimeMins % 60;

		Calendar cal = Calendar.getInstance();
		final int currTimeMins = cal.get(Calendar.HOUR_OF_DAY) * 60
				+ cal.get(Calendar.MINUTE);

		cal.set(Calendar.HOUR_OF_DAY, alarmHours);
		cal.set(Calendar.MINUTE, alarmMins);

		// we must roll to the next applicable date if either we're already
		// at or past the alarm time, or we've explicitly been told to
		setReminderDateTime(cal, (currTimeMins >= alarmTimeMins) || doRoll);

		return cal;
	}

	/**
	 * @param prefs
	 *            - app's prefs
	 * @return - the number of minutes into the day (i.e. past 00:00) that the
	 *         remidner should be shown. Grab the earliest & latest times that
	 *         the reminder should be shown. This period is the "time window"
	 *         (e.g. 90 mins). Get a random percentage (e.g. 40%). Apply that
	 *         percentage to the time window (e.g. 36 mins). Add this to the
	 *         earliest time - this is the time the reminder will be set for.
	 */
	public int calcReminderTimeInMins() {

		final String earliest = _prefs.getString(
				_ctx.getString(R.string.reminder_earliest), "13:00");
		final String latest = _prefs.getString(
				_ctx.getString(R.string.reminder_latest), "17:00");

		final int earliestHour = TimePreference.getTimeElement(earliest, 0);
		final int earliestMinute = TimePreference.getTimeElement(earliest, 1);

		final int latestHour = TimePreference.getTimeElement(latest, 0);
		final int latestMinute = TimePreference.getTimeElement(latest, 1);

		// Time Window in which the reminder must fall
		int window = (latestHour * 60 + latestMinute)
				- (earliestHour * 60 + earliestMinute);

		if (window < 0) {
			// user has entered inter-day times. Bump "latest" to next day to
			// obtain window
			window = ((latestHour + 24) * 60 + latestMinute)
					- (earliestHour * 60 + earliestMinute);

			final String msg = "Info: Reminder times span midnight";
			Toast.makeText(_ctx, msg, Toast.LENGTH_LONG).show();
		}

		return getReminderTime(earliestHour, earliestMinute, window);
	}

	/**
	 * @param earliestHour
	 *            e.g. 14
	 * @param earliestMinute
	 *            e.g. 55
	 * @param window
	 *            - length in mins of period reminder may occur in
	 * @return - number of mins into day reminder will be set for
	 */
	public int getReminderTime(final int earliestHour,
			final int earliestMinute, int window) {
		// OK so we need a random time in the window.
		Random rand = new Random();
		float fraction = rand.nextFloat();
		int minsIntoWindow = (int) (window * fraction);

		final int alarmTimeMins = minsIntoWindow
				+ (earliestHour * 60 + earliestMinute);
		return alarmTimeMins;
	}

	/**
	 * @param cal
	 *            - cal to update with reminder time
	 * @param roll
	 *            - is a roll required?
	 * 
	 *            the desired alarm date can either be today or in the future.
	 *            If it's in the future, rolling is not necessary.
	 */
	void setReminderDateTime(Calendar cal, boolean roll) {

		BitSet set = new BitSet(8);
		set.clear();

		for (int i = 1; i < 8; i++)
			set.set(i, isDOWset(DAYS_OF_WEEK[i - 1]));

		// roll if necessary. this is 'today'
		if (roll)
			cal.roll(Calendar.DATE, true);

		// starting with today's dow, if this dow's bit is set, we've found
		// the earliest alarm date so exit. Else only iterate up to 7 times.
		int currDOW = cal.get(Calendar.DAY_OF_WEEK);

		boolean found = false;
		for (int i = currDOW; i < currDOW + 8 && (!found); i++) {
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (set.get(dow)) {
				found = true;
				continue;
			}

			cal.roll(Calendar.DATE, true);
		}

		if (!found) {
			Log.v(Util.TAG, "No days checked, defaulting to tomorrow");
			cal.setTime(new Date());
			cal.roll(Calendar.DATE, true);
		}
	}

	/**
	 * @param dow
	 *            - e.g. Tue, Wed
	 * @return - is this day checked for reminders in prefs?
	 */
	private boolean isDOWset(final String dow) {
		final String prefKey = "cbp" + dow + "Toggle";
		return _prefs.getBoolean(prefKey, false);
	}

	boolean isEnabled() {
		return _prefs.getBoolean("cbpReminderToggle", true);
	}

	public String getInfoText() {
		if (isEnabled()) {
			DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					DateFormat.SHORT);

			return "Info: next reminder at " + fmt.format(_cal.getTime());
		} else {
			return "";
		}
	}
}
