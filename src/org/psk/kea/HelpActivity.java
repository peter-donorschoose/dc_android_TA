package org.psk.kea;

import org.psk.kea.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Pete
 * Shown when the user selects Help from main screen
 */
public class HelpActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
	}
}
