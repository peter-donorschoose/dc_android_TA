package org.psk.kea;

import org.psk.kea.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * @author Pete
 * Handy functions used in disparate places across the app
 */
public class ResourceUtil {
	
	/**
	 * @param act - interrogate prefs to determine if app has ever been run before
	 * @return - true if app has been run before
	 */
	public static boolean isFirstRun(Activity act) {
		Resources res = act.getResources();
		final String NOT_PRESENT = res.getString(R.string.PREF_NOT_PRESENT);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(act.getBaseContext());

		String tmp = prefs.getString("etpSignIn1", NOT_PRESENT);
		return tmp.equals(NOT_PRESENT);
	}
}
