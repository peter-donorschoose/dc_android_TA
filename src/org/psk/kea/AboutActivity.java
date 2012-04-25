package org.psk.kea;

import org.psk.kea.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * @author Pete
 * Displays information about the app and an email contact address.
 */
public class AboutActivity extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		// hook us up as listener for any clicks on the email link
		View v = findViewById(R.id.email_text);
		v.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.email_text:
			composeEmailToDev();
		}

	}

	/**
	 * open the message compose window pre-populated with my contact details
	 */
	private void composeEmailToDev() {
		Sender sender = new Sender(this);
		sender.send(new HappyEmail(
				"peterkingswell+kea@gmail.com", "Keep Er Appy: ", "Hi Pete,\n"));
	}

}
