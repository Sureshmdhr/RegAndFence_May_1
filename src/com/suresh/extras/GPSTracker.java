package com.suresh.extras;

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
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.suresh.reporting.Reporting_pg4;
 
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
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30 * 1;
 
    // Declaring a Location Manager
    protected LocationManager locationManager;

	private ProgressDialog mProgressDialog;
 
    public GPSTracker(Context context) {
    	
        this.mContext = context;
        getLocation();
        Log.i("loc","");
        Log.i("loc",""+getLocation().getLatitude());
    }
    
    public GPSTracker()
    {
    }
 
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
 
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
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
            		mProgressDialog = new ProgressDialog(mContext);
            		mProgressDialog.setMessage("Please Wait for Satellites...\nJust 30 seconds");
            		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                	if(mContext.getClass().getSimpleName().equals("Reporting_pg1"))
                	{
                		mProgressDialog.setCancelable(false);
                    	mProgressDialog.show();
                    	new Handler().postDelayed(new Runnable() {
							
							public void run() 
							{
								mProgressDialog.dismiss();
							}
						}, 30000);
                	}
                	else
                	{
                		mProgressDialog.setCancelable(true);
                    	mProgressDialog.show();
                	}
                	
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");                          
                        if (location == null) 
                        {
                            if (locationManager != null) 
                            {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if(location!=null)
                                {
                                	Log.i("loc", "yes");
                                	mProgressDialog.dismiss();
                                	latitude = location.getLatitude();
                                	longitude = location.getLongitude();
                                	Log.e("loc_gps_not_null", location.getLatitude()+"\n"+location.getLongitude());
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
    * Make use of location after deciding if it is better than previous one.
    *
    * @param location Newly acquired location.
    */
/*    void doWorkWithNewLocation(Location location) {
        if(isBetterLocation(getOldLocation(), location) {
            // If location is better, do some user preview.
            Toast.makeText(mContext,
                            "Better location found: " + provider, Toast.LENGTH_SHORT)
                            .show();
        }
     
        setOldLocation(location);
    }
*/     
    /**
    * Time difference threshold set for one minute.
    */
    static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;
     
    /**
    * Decide if new location is better than older by following some basic criteria.
    * This algorithm can be as simple or complicated as your needs dictate it.
    * Try experimenting and get your best location strategy algorithm.
    * 
    * @param oldLocation Old location used for comparison.
    * @param newLocation Newly acquired location compared to old one.
    * @return If new location is more accurate and suits your criteria more than the old one.
    */
    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }
     
        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();
     
        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();       
        if(isMoreAccurate && isNewer) {         
            // More accurate and newer is always better.         
            return true;     
        } else if(isMoreAccurate && !isNewer) {         
            // More accurate but not newer can lead to bad fix because of user movement.         
            // Let us set a threshold for the maximum tolerance of time difference.         
            long timeDifference = newLocation.getTime() - oldLocation.getTime(); 
     
            // If time difference is not greater then allowed threshold we accept it.         
            if(timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }
     
        return false;
    }
    
    protected void toaster(String string)
    {
    	Toast.makeText(mContext, string, Toast.LENGTH_LONG).show();
		
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
        	if(mContext.getClass().getSimpleName().equals("Reporting_pg1"))
        	{
        		((Activity) mContext).finish();
        	}
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
    public void onProviderDisabled(String provider) {
    	Toast.makeText(mContext,
                "Provider disabled: " + provider, Toast.LENGTH_SHORT)
                .show();
    }
 
    @Override
    public void onProviderEnabled(String provider) {
    	Toast.makeText(mContext,
            "Provider enabled: " + provider, Toast.LENGTH_SHORT)
            .show();
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