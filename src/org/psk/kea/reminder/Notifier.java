package org.psk.kea.reminder;

import org.psk.kea.KeepErAppyActivity;
import org.psk.kea.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Pete
 * Responsible for listening out for the reminder's alarm and composing
 * an appropriate notification. When clicked, the notification activates
 * KEA, starting it if necessary.
 */
public class Notifier extends BroadcastReceiver {

	private static final int KEA_NOTIFICATION_ID = 1151;	// 11 5 1 ~ K E A (for no particular reason)
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notMgr = (NotificationManager) context.getSystemService(ns);

		// Instantiate the Notification:
		final int icon = R.drawable.beer_sm;
		final CharSequence tickerText = "Send er a msg?";
		final long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);

		// Define the notification's message and PendingIntent:
		CharSequence contentTitle = "Keep Er Appy";
		
		Intent i = new Intent(context, KeepErAppyActivity.class);
		i.putExtra(context.getString(R.string.originator), "Notifier");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				i, 0);
		notification.setLatestEventInfo(context, contentTitle, tickerText,
				contentIntent);

	    // We'll have this notification do the default sound, vibration, and led.
	    notification.defaults = Notification.DEFAULT_ALL;

	    // The long array defines the alternating pattern for the length of
		// vibration off and on (in milliseconds). The first value is how long
		// to wait (off) before beginning, the second value is the length of the
		// first vibration, the third is the next length off, and so on. The
		// pattern can be as long as you like, but it can't be set to repeat.
		long[] vibrate = { 0, 100, 200, 300 };
		notification.vibrate = vibrate;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Pass the Notification to the NotificationManager:
		notMgr.notify(KEA_NOTIFICATION_ID, notification);
	}
};
