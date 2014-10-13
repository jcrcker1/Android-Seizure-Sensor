package com.seizuresensor;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Context;

public class MyLocationListener implements LocationListener {

	private static Context context;
	
	public MyLocationListener(Context c) {
	     context = c;
	}
	
	public void onLocationChanged(Location location) {
	//	String message = String.format("New Location \n Longitude: %1$s \n Latitude: %2$s", location.getLongitude(), location.getLatitude());
	//	Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public void onStatusChanged(String s, int i, Bundle b) {
	//	Toast.makeText(context, "Provider status changed", Toast.LENGTH_LONG).show();
	}

	public void onProviderDisabled(String s) {
		Toast.makeText(context, "Provider disabled by the user. GPS turned off", Toast.LENGTH_LONG).show();
	}

	public void onProviderEnabled(String s) {
		Toast.makeText(context, "Provider enabled by the user. GPS turned on", Toast.LENGTH_LONG).show();
	}

}