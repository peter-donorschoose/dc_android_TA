package org.psk.kea;

/**
 * THIS FILE IS FROM Hello, Android (Pragmatic Programmer series)
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

public class Music {
	private static MediaPlayer mp = null;

	/** Stop old song and start new one */
	public static void play(Context context, int resource) {
		if (!enabled(context))
			return;
		
		stop(context);
		mp = MediaPlayer.create(context, resource);
		mp.setLooping(false);
		mp.start();
	}

	/**
	 * @param context - so prefs can be accessed
	 */
	public static boolean enabled(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getString(R.string.fx_toggle), true);
	}

	/** Stop the music - note that it may have been enabled, started and then
	 * disabled and thus would need release()ing. */
	public static void stop(Context context) {
		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
		}
	}
}
