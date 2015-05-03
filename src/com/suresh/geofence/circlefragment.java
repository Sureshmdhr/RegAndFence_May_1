package com.suresh.geofence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.suresh.extras.GPSTracker;
import com.suresh.extras.SessionManager;
import com.suresh.extras.Utils;
import com.suresh.extras.mymapview;
import com.suresh.form.Form;
import com.suresh.form.R;
import com.suresh.network.Receiver;

@SuppressLint("DefaultLocale")
public class circlefragment  extends Activity{
		mymapview mapview;
		Point pt;
		Marker m;
		String longitude,latitude,uname;
		EditText lgt,lat,rad,msg,name;
		Button btngeo,preview;
		protected String s;
		String port=Form.fence;
		//IMapController mapcontroller;
		Spinner spinner;
		ArrayList<Point> mPoints=new  ArrayList<Point>();
		ArrayList<Double> xcor=new  ArrayList<Double>();
		ArrayList<Double> ycor=new  ArrayList<Double>();
	    String q="";
		String MAPFILE =Environment.getExternalStorageDirectory().getPath()+"/girc/nepal-gh/nepal.map";

	    Button b1;
	    int catagory=0;
		private TileCache tileCache;
		private TileRendererLayer tileRendererLayer;
		private com.suresh.extras.TappableMarker usermarker;
		private Circle myCircle;
		@SuppressLint("NewApi")
		protected void onCreate(Bundle savedInstanceState) {

		    super.onCreate(savedInstanceState);
			AndroidGraphicFactory.createInstance(this.getApplication());
		    setContentView(R.layout.activity_map);

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

			StrictMode.setThreadPolicy(policy); 
			SessionManager session = new SessionManager(getApplicationContext());
		    HashMap<String, String> user = session.getUserDetails();
		    
		    // Session name
		    uname = user.get(SessionManager.KEY_EMAIL);
		    Log.i("uname", uname);
		    
		    mapview=(mymapview)findViewById(R.id.mapview1);
	        mapview.setClickable(true);
	        mapview.setFocusable(true);
	        mapview.setDuplicateParentStateEnabled(false);
	        mapview.setBuiltInZoomControls(true);
	        tileCache = AndroidUtil.createTileCache(this, "mapcache",
	        		 mapview.getModel().displayModel.getTileSize(), 1f, 
	        		 mapview.getModel().frameBufferModel.getOverdrawFactor());

	        lat=(EditText)findViewById(R.id.latitude);
		    lgt=(EditText)findViewById(R.id.longitude);
		    rad=(EditText)findViewById(R.id.radius);
		    msg=(EditText)findViewById(R.id.message);
		    name=(EditText)findViewById(R.id.name);
		    spinner=(Spinner)findViewById(R.id.spinner1);
	        spinner.setOnItemSelectedListener(new OnItemSelectedListener() 
	        {
				public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) 
				{
					catagory=position+1;
				}
				public void onNothingSelected(AdapterView<?> arg0) 
				{
					catagory=0;
				}
			});
		    
	        if(!uname.equals("admin")){
	        	TextView tv=(TextView)findViewById(R.id.textView6);
	        	tv.setVisibility(View.INVISIBLE);
	        	spinner.setVisibility(View.INVISIBLE);
	        	msg.setVisibility(View.INVISIBLE);
	        }
	        
	        btngeo=(Button)findViewById(R.id.create);
	        btngeo.setOnClickListener(new OnClickListener() 
	        {
				public void onClick(View arg0) 
				{
					if(uname.equals("admin"))
					{
						if (name.getText().toString().equals("")||
								lat.getText().toString().equals("")||
								lgt.getText().toString().equals("")||
								msg.getText().toString().equals("")||
								rad.getText().toString().equals(""))
						{
							toaster("Some or all field are Empty");
						}
						else
						{
							if(!GeofenceNameExists())
								sentdatatocreatecircle();
							else
							{
								toaster("Name Already Exists");
								name.setText("");
							}
						}
					}
					else
					{
						if (name.getText().toString().equals("")||
								lat.getText().toString().equals("")||
								lgt.getText().toString().equals("")||
								rad.getText().toString().equals(""))
						{
							toaster("Some or all field are Empty");
						}
						else
						{
							if(!GeofenceNameExists())
								sentdatatocreatecircle();
							else
							{
								toaster("Name Already Exists");
								name.setText("");
							}
						}
					}
				}
			});
		    
	        preview=(Button)findViewById(R.id.preview);
		    preview.setOnClickListener(new OnClickListener() 
		    {
				public void onClick(View arg0) 
				{
				    if(!lat.getText().toString().equals("")&&!lgt.getText().toString().equals("")&&!rad.getText().toString().equals(""))
				    {
				    	drawCircles(new LatLong(Double.valueOf(lat.getText().toString()),Double.valueOf(lgt.getText().toString())), Float.valueOf(rad.getText().toString()),uname);
				    }
				}
		    });
		}

		private void drawCircles(LatLong latlong,float rad, String type)
		{
			Paint paintFill;
			if(type.equals("admin"))
			{
				paintFill= Utils.createPaint(
						AndroidGraphicFactory.INSTANCE.createColor(80,255, 0, 102), 2,
						Style.FILL);
			}
			else
			{
				paintFill = Utils.createPaint(
					AndroidGraphicFactory.INSTANCE.createColor(80,0,0,100), 2,
					Style.FILL);
			}
			if((myCircle!=null))
				mapview.getLayerManager().getLayers().remove(myCircle);
			myCircle=new Circle(latlong, rad,paintFill, null);
			LatLong centre = myCircle.getPosition();
			mapview.getLayerManager().getLayers().add(myCircle);
			createuserMarker(centre.latitude, centre.longitude);
			
		}
		
		@SuppressLint("DefaultLocale")
		protected boolean GeofenceNameExists() 
		{
			Receiver connect =new Receiver();
			connect.setPath("selectgeofencename.php");
			connect.addNameValuePairs("value1", name.getText().toString().toLowerCase());
			AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
			String result = null;
			try 
			{
				result=output.get();
				Log.i("out", result);
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			catch (ExecutionException e) 
			{
				e.printStackTrace();
			}
			if(result.equals("[]"))
				return false;
			else 
				return true;
		}

		protected void sentdatatocreatecircle() 
		{
			Receiver connect = new Receiver(this);
			connect.setPath("circlecreate.php");
			connect.addNameValuePairs("value1", name.getText().toString().toLowerCase());
			connect.addNameValuePairs("value2",lgt.getText().toString());
			connect.addNameValuePairs("value3", lat.getText().toString());
			connect.addNameValuePairs("value4", rad.getText().toString());
			connect.addNameValuePairs("value5", msg.getText().toString());
			connect.addNameValuePairs("value6", uname);
			connect.addNameValuePairs("value7", String.valueOf(catagory));
			AsyncTask<Void, Void, String> result=connect.execute(new Void[0]);
			try 
			{
				String res=result.get();
				Log.i("circle create output", res);
				toaster("Geofence created");
				finish();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			catch (ExecutionException e) 
			{
				e.printStackTrace();
			}
		}


		protected void toaster(String s) 
		{
			Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT).show();
		}
		
		protected void onStart()
		{
			super.onStart();
	        GPSTracker gps=new GPSTracker(this);
	        mapview.getModel().mapViewPosition.setZoomLevel((byte) 12);
	        mapview.getModel().mapViewPosition.setCenter(new LatLong(gps.getLatitude(), gps.getLongitude()));
	        mapview.getModel().mapViewPosition.setCenter(new LatLong(27.6196988,85.5392799));
	        this.tileRendererLayer = new TileRendererLayer((TileCache) tileCache, mapview.getModel().mapViewPosition,
	        		true, AndroidGraphicFactory.INSTANCE)
	        {
	        			public boolean onTap(LatLong tapLatLong, 
	        					org.mapsforge.core.model.Point layerXY, 
	        					org.mapsforge.core.model.Point tapXY) 
	        			{
	        				createuserMarker(tapLatLong.latitude,tapLatLong.longitude);
	        				lat.setText(String.valueOf(tapLatLong.latitude));
	        				lgt.setText(String.valueOf(tapLatLong.longitude));
							return true;
	        			};
	        };
	        ((TileRendererLayer) this.tileRendererLayer).setMapFile(getMapFile());
	        ((TileRendererLayer) this.tileRendererLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
	        mapview.getLayerManager().getLayers().add(tileRendererLayer);
		}
		
		protected void createuserMarker(double latitude2, double longitude2) 
		{
			if(usermarker!=null)
				mapview.getLayerManager().getLayers().remove(usermarker);
			final LatLong localLatLong = new LatLong(latitude2, longitude2);
			this.usermarker = new com.suresh.extras.TappableMarker(circlefragment.this,R.drawable.redmarker, localLatLong);
			mapview.getLayerManager().getLayers().add(usermarker);
		}

		private File getMapFile() 
		{	
			File file = new File(MAPFILE);
			return file;
		}
		
		protected void onStop() 
		{
			super.onStop();
			this.mapview.getLayerManager().getLayers().remove(this.tileRendererLayer);
			this.tileRendererLayer.onDestroy();
		}

		protected void onDestroy() 
		{
			super.onDestroy();
			this.tileCache.destroy();
			this.mapview.getModel().mapViewPosition.destroy();
			this.mapview.destroy();
			AndroidResourceBitmap.clearResourceBitmaps();
		}

	}


