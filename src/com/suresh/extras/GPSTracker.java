package com.suresh.extras;

import com.suresh.geofence.geofences;
import com.suresh.menus.BaseActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
 
public class GPSTracker extends Service implements LocationListener {
 
    private Context mContext;
 
    // flag for GPS status
    boolean isGPSEnabled = false;
 
    // flag for network status
    boolean isNetworkEnabled = false;
 
    // flag for GPS status
    boolean canGetLocation = false;
 
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
 
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
 
    // Declaring a Location Manager
    protected LocationManager locationManager;

	private ProgressDialog mProgressDialog;
 
    public GPSTracker(Context context) {
    	
        this.mContext = context;
        getLocation();
        
    }
    
    public GPSTracker()
    {
    }
 
    public Location getLocation() {
        try {
        		Log.i("name", mContext.getClass().getSimpleName());
        		mProgressDialog = new ProgressDialog(mContext);
        		mProgressDialog.setMessage("Please Wait for Satellites...");
        		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            	if(mContext.getClass().getSimpleName().equals("Reporting_pg1"))
            	{
            		mProgressDialog.setCancelable(true);
            		mProgressDialog.show(mContext,
                            "GPS",
                            "Please Wait for Satellites...",
                            false,
                            true,
                            new DialogInterface.OnCancelListener(){

								@Override
								public void onCancel(DialogInterface arg0)
								{
/*									Intent i=new Intent(mContext.getApplicationContext(),geofences.class);
									startActivity(i);
*/								};
            		}

            				);
            	}
            	else
            	{
            		mProgressDialog.setCancelable(true);
            		mProgressDialog.show(mContext,
                            "GPS",
                            "Please Wait for Satellites...",
                            false,
                            true,
                            new DialogInterface.OnCancelListener(){
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                }
                            }

            				);
            	}
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
 
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
        	//onGpsStatusChanged();
        	mProgressDialog.dismiss();
        	mProgressDialog.cancel();

            if (!isGPSEnabled) 
            {
            	this.canGetLocation=false;
            	Toast.makeText(mContext, "GPS is inactive", Toast.LENGTH_LONG).show();
            	showSettingsAlert();
            }
            else {
                this.canGetLocation = true;
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                    	
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) 
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.i("loc_gps_loc_not_null", latitude+"\n"+longitude);
                            }
                            else
                            {
                                Log.e("loc_gps_null", latitude+"\n"+longitude);

                            }
                        }
                    }
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
     
    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }       
    }
     
    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        // return latitude
        return latitude;
    }
     
    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
         
        // return longitude
        return longitude;
    }
     
    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                dialog.cancel();
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            ((Activity) mContext).finish();
            }
        });
        alertDialog.setCancelable(false);
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void onGpsStatusChanged() {
        int satellites = 0;
        int satellitesInFix = 0;
        int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
        Log.i("TAG", "Time to first fix = "+String.valueOf(timetofix));
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if(sat.usedInFix()) {
                satellitesInFix++;              
            }
            satellites++;
        }
        Log.i("GPS", String.valueOf(satellites) + " Used In Last Fix ("+satellitesInFix+")"); 
    }
    @Override
    public void onLocationChanged(Location location) {
    	this.location=location;
    }
 
    @Override
    public void onProviderDisabled(String provider)
    {
    }
 
    @Override
    public void onProviderEnabled(String provider) 
    {
    	
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

  public boolean haveNetworkConnection() 
  {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;
	    ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}

}