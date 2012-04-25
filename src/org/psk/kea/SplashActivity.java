package org.psk.kea;

import org.psk.kea.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * 
 * @author Pete Entry point to KEA app. Shows the beer mug splash screen and
 *         forks new thread to display the main screen. New thread waits 1
 *         second before showing main screen. It does this in the UI thread, as
 *         is required.
 */
public class SplashActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		Runnable showMain = new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this, KeepErAppyActivity.class);
				startActivity(intent);
				Log.i(Util.TAG, "SplashActivity.onCreate.run");
			}
		};

		View vw = findViewById(R.id.big_beer);
		vw.postDelayed(showMain, 1000);
		Log.i(Util.TAG, "SplashActivity.onCreate");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(Util.TAG, "SplashActivity.onResume()");
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(Util.TAG, "SplashActivity.onNewIntent");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(Util.TAG, "SplashActivity.onDestroy()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(Util.TAG, "SplashActivity.onPause()");
	}
}