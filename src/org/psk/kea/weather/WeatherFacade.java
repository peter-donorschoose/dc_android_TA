package org.psk.kea.weather;

import org.psk.kea.HappyEmail;
import org.psk.kea.Sender;
import org.psk.kea.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

enum WeatherCondition {
	HOT, COLD, OK;
};

/**
 * @author Pete
 * This is the interface to the weather functionality. All non-weather functionality/
 * code must go through this class. 
 */
public class WeatherFacade implements LocationListener, OnClickListener {

	private Activity _act;	// to get at Activity goodies
	
	
	private WeatherCondition _condition;	// hot, cold or OK?

	public static final Integer DEFAULT_COLD = new Integer(30);
	public static final Integer DEFAULT_HOT = new Integer(85);

	private LocationManager _mgr;
	private String _best;

	private SharedPreferences _prefs;

	private WeatherCurrentCondition _current;

	public String getPref(final String pref) {
		return _prefs.getString(pref, "");
	}

	/**
	 * @param activity - main KEA activity, required for access to Context stuff
	 */
	public WeatherFacade(Activity activity) {
		_act = activity;
		_mgr = (LocationManager) _act
				.getSystemService(android.content.Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		_best = _mgr.getBestProvider(criteria, true);

		_prefs = PreferenceManager.getDefaultSharedPreferences(_act
				.getBaseContext());
	}

	public void resume() {
		// Start updates (120 minutes and every 1 KM is fine)
		if (_best != null) {
			_mgr.requestLocationUpdates(_best, 120 * 60000, 1000, this);
		}
	}

	public void pause() {
		// Stop updates to save power while app paused
		_mgr.removeUpdates(this);
	}

	public void onLocationChanged(Location location) {
		//_location = location;
	}

	/**
	 * get the current conditions
	 */
	public void fetchCurrent() {
		final String city = _prefs.getString("etpWeatherCity", "");

		if (city.length() == 0)
			return;

		try {
			String queryString = "http://www.google.com/ig/api?weather=" + city;
			/* Replace blanks with HTML-Equivalent. */
			java.net.URL url = new java.net.URL(queryString.replace(" ", "%20"));

			/* Get a SAXParser from the SAXPArserFactory. */
			javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory
					.newInstance();
			javax.xml.parsers.SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			org.xml.sax.XMLReader xr = sp.getXMLReader();

			/*
			 * Create a new ContentHandler and apply it to the XML-Reader
			 */
			GoogleWeatherHandler gwh = new GoogleWeatherHandler();
			xr.setContentHandler(gwh);

			/* Parse the xml-data our URL-call returned. */
			xr.parse(new org.xml.sax.InputSource(url.openStream()));

			/* Our Handler now provides the parsed weather-data to us. */
			WeatherSet ws = gwh.getWeatherSet();

			_current = ws.getWeatherCurrentCondition();

		} catch (Exception e) {
			Log.e(Util.TAG, "WeatherQueryError", e);
			showDialog("Couldn't get current conditions for " + city);
		}
	}

	/**
	 * ask the user if they'd like to send their significant other 
	 * a considerate extreme-weather message.
	 */
	public void prompt() {

		boolean enabled = _prefs.getBoolean("cbpWeatherToggle", false);

		if (_current == null || !enabled) {
			return;	// don't ask the user if the pref is disabled
		}

		final String cold = _prefs.getString("etpWeatherCold", DEFAULT_COLD.toString());
		final String hot = _prefs.getString("etpWeatherHot", DEFAULT_HOT.toString());

		final int temp = _current.getTempFahrenheit();
		final int coldF = strToInt(cold, DEFAULT_COLD);
		final int hotF = strToInt(hot, DEFAULT_HOT);
		
		if (temp <= coldF) {
			_condition = WeatherCondition.COLD;
			showDialog("Brrr - it's " + temp + "F out. Compose a 'wrap-up warm' message?");
		} else if (temp >= hotF) {
			_condition = WeatherCondition.HOT;
			showDialog("Phew - it's " + temp + "F out. Compose a 'keep cool' message?");
		} else {
			_condition = WeatherCondition.OK;
		}

	}

	/**
	 * @param threshold - string temp to parse to int
	 * @param def - default, if the string cannot be converted 
	 * @return - int value of threshold input
	 */
	static public int strToInt(final String threshold, final Integer def) {
		int retVal = def;
		
		try {
			retVal = Integer.parseInt(threshold);
		} catch (final NumberFormatException e) {
			//
		}
		return retVal;
	}

	/**
	 * @param text - message to ask user
	 */
	private void showDialog(final String text) {
		Runnable runner = new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(_act);

				builder.setMessage(text).setCancelable(false)
						.setPositiveButton("Yes", WeatherFacade.this)
						.setNegativeButton("No", WeatherFacade.this);
				AlertDialog alert = builder.create();
				alert.show();
			}
		};
		
		View v = _act.findViewById(org.psk.kea.R.id.recents_button);
		v.post(runner);
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dlg, int button) {
		if (button == DialogInterface.BUTTON_POSITIVE) {
			Sender sender = new Sender(_act);
			
			final String text = _prefs.getString(
					isCold() ? "etpWeatherTextCold" : "etpWeatherTextHot", "");

			sender.send(new HappyEmail(_prefs.getString("etpHerEmail1", ""),
					isCold() ? "Brrr!" : "Phew!",
					text));
		}
	}
	
	/**
	 * @return - is it cold out?
	 */
	private boolean isCold() {
		return _condition.equals(WeatherCondition.COLD);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Nothing to do
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Nothing to do
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Nothing to do
	}
}
