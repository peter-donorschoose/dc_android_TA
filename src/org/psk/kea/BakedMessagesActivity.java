package org.psk.kea;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Pete
 * POTENTIALLY TO BE MOTHBALLED/TRASHED
 * The idea of this activity is to present the user with example sweet messages
 * for the user to select from or to use as inspiration. However this has become
 * less important relative to the ability to compose their own messages.
 */
public class BakedMessagesActivity extends ListActivity implements
		OnItemClickListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] baked = getResources().getStringArray(R.array.baked_array);		
		String[] pBaked = personalizeBaked(baked, "KEA_SIGN_IN", "Hi Luvva!");
		String[] ppBaked = personalizeBaked(pBaked, "KEA_SIGN_OFF", "Chicka XXX");

		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ppBaked));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(this);
	}

	/**
	 * Take the pre-baked message and replace the KEA_SIGN_* placeholders with
	 * the user's preferred names.
	 * @param baked
	 * @param reg
	 * @param rep
	 * @return
	 */
	private String[] personalizeBaked(final String[] baked, final String reg, final String rep) {
		String[] res = new String[baked.length];
		
		for (int i = 0; i < baked.length; i++) {
			final String str = baked[i];
			res[i] = str.replaceAll(reg, rep);
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 * Slurp out the user's selection, grab the necessary prefs and compose an
	 * email from them. 
	 */
	@Override
	public void onItemClick(android.widget.AdapterView<?> parent, View view,
			int position, long id) {

		final CharSequence body = ((TextView) view).getText();
		Log.d(Util.TAG, body.toString());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		final String eAddr = prefs.getString("etpHerEmail1", "");
		final String subject = prefs.getString("etpSubject1", "");
		
		HappyEmail hemail = new HappyEmail(eAddr, subject, body.toString());

		Sender sender = new Sender(this);
		sender.send(hemail);

		// add to recents
		Editor ed = prefs.edit();
		ed.putString("recents1", body.toString());
		ed.commit();
	}

}
