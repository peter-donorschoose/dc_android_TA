package org.psk.kea;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author Pete
 * When a message is composed, the address, subject and body text are saved
 * as a "recent". The last 5 recents are saved and are updated in MRU fashion.
 * The shared preferences mechanism is used for storing the recents.
 */
public class RecentsActivity extends ListActivity implements
		OnItemClickListener {

	private String[] _recents = new String[Util.NUM_RECENTS];
	private SharedPreferences _prefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		populateList();

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(this);
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 * the only clickable we listen to is the Compose button
	 */
	public void onItemClick(android.widget.AdapterView<?> parent,
			View view, int position, long id) {

		Sender sender = new Sender(this);
		
		StringBuilder sb = new StringBuilder();
		appendBodyText(position, sb);

		sender.send(new HappyEmail(_prefs.getString("etpHerEmail1", ""), 
				_prefs.getString(Util.SUBJECT_RECENT_PREF+(position+1), ""), 
				sb.toString()));
	}

	/**
	 * build the recents array from stored shared prefs and stuff it into this 
	 * activity 
	 */
	private void populateList() {
		buildRecentsArray();
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				_recents));
	}

	/**
	 * extract the recents strings from prefs and stuff them into our array
	 */
	private void buildRecentsArray() {

		for (int i = 0; i < Util.NUM_RECENTS; i++) {
			StringBuilder sb = new StringBuilder(_prefs.getString(
					Util.SUBJECT_RECENT_PREF + (i + 1), ""));

			sb.append("\t");
			appendBodyText(i, sb);

			_recents[i] = sb.toString();
		}
	}

	/**
	 * @param i - the i'th recent's email body text to fetch
	 * @param sb - the StringBuilder to append it to
	 */
	private void appendBodyText(int i, StringBuilder sb/*, boolean inflate*/) {
		sb.append(_prefs.getString(Util.BODY_RECENT_PREF + (i + 1), ""));
	}
}
