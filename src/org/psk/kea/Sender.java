package org.psk.kea;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

/**
 * @author Pete
 * Encapsulates the task of starting and populating a message composition action.
 * I say 'action' since it's unknown what will actually service the Intent - it could
 * be an MMS or Email client for example. This class makes an attempt to prefer Gmail.
 * The user's informed if no suitable service is found. 
 */
public class Sender {
	// _act is required for activity-starting abilities
	private Activity _act;
	
	public Sender(Activity act) { _act = act; }
	
	/**
	 * @param email - details of the email to be sent
	 * @return - true if composition screen started, false otherwise
	 */
	public boolean send(final HappyEmail email) {
		assert(_act != null);
		
		final Intent intent = buildEmailIntent(email);

	    try {
			_act.startActivity(intent);
			return true;
		} catch (android.content.ActivityNotFoundException exc) {
			exc.printStackTrace();
			Toast.makeText(_act.getApplicationContext(),
					"No mail program found, can't send mail.", 3);
			return false;
		}

	}

	/**
	 * @param email - contains the details of the email to be sent
	 * @return - an Intent that will bring up a message compose activity when posible
	 */
	Intent buildEmailIntent(final HappyEmail email) {
		
		final Intent intent = new Intent(Intent.ACTION_SEND);
		configureIntentForEmail(email, intent);
		
		final PackageManager pm = _act.getPackageManager();

		final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
	    
		ResolveInfo best = null;
	    for (final ResolveInfo info : matches)
	      if (info.activityInfo.packageName.endsWith(".gm") ||
	    		  info.activityInfo.name.toLowerCase().contains("gmail") ||
	    		  info.activityInfo.name.toLowerCase().contains("email")) best = info;
	    
	    if (best != null)
	      intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
		
	    return intent;
	}

	/**
	 * @param email - contains the details of the email to be sent
	 * @param intent - intent to email-ize 
	 */
	void configureIntentForEmail(final HappyEmail email, Intent intent) {
		
		final String[] addressList = new String[1];
		
		// KLUDGE: if "," isn't appended, something undesired happens. I think maybe
		// MMS is preferred over email or something. Or the address doesn't populate.
		addressList[0] = email.getTo() + ",";
		
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL,	addressList);
		intent.putExtra(Intent.EXTRA_SUBJECT, email.getSubject());
		intent.putExtra(Intent.EXTRA_TEXT, email.getBody());
	}
}
