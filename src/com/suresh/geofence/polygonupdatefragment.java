package com.suresh.geofence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

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

public class polygonupdatefragment extends Activity{
public static mymapview mapview;
Point pt;
String longitude,latitude;
static EditText lgt,lat,rad;
public static EditText msg;
static EditText e1;
TextView t1;
Button btngeo,clear;
protected String s;
String port=Form.fence;
String fencename=updategeofence.fencename;
int catagory;
public static Spinner spinner;
ArrayList<Point> mPoints=new  ArrayList<Point>();
ArrayList<Double> xcor=new  ArrayList<Double>();
ArrayList<Double> ycor=new  ArrayList<Double>();
String geom_q="",geom_sq="";
Button b1;
String uname;
String unchanged_polygon=updategeofence.polygon_shape;
private TileCache tileCache;
private Polygon myPolygon;
String MAPFILE =Environment.getExternalStorageDirectory().getPath()+"/girc/nepal-gh/nepal.map";
private TileRendererLayer tileRendererLayer;
List<LatLong> poly_points=new ArrayList<LatLong>();

protected void onCreate(Bundle savedInstanceState) 
{
	    super.onCreate(savedInstanceState);
		AndroidGraphicFactory.createInstance(this.getApplication());
	    setContentView(R.layout.activity_polygon);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		geom_sq=unchanged_polygon;

	    SessionManager session = new SessionManager(getApplicationContext());
	    HashMap<String, String> user = session.getUserDetails();
	    uname = user.get(SessionManager.KEY_EMAIL);
	    
		t1=(TextView)findViewById(R.id.textView7);
		t1.setVisibility(View.GONE);
		e1=(EditText)findViewById(R.id.name);
		e1.setVisibility(View.GONE);
		
	    mapview=(mymapview)findViewById(R.id.mapview1);
	    mapview.setClickable(true);
	    mapview.setFocusable(true);
	    mapview.setDuplicateParentStateEnabled(false);
	    mapview.setBuiltInZoomControls(true);
	    tileCache = AndroidUtil.createTileCache(this, "mapcache",
	    		 mapview.getModel().displayModel.getTileSize(), 1f, 
	    		 mapview.getModel().frameBufferModel.getOverdrawFactor());

	    btngeo=(Button)findViewById(R.id.create);
		btngeo.setText("Update");
	    
	    msg=(EditText)findViewById(R.id.message);
	    
	    spinner=(Spinner)findViewById(R.id.spinner1);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() 
	    {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long arg3) 
			{
				catagory=position+1;
			}
			public void onNothingSelected(AdapterView<?> arg0) 
			{
				
			}
		});
	    
	    if(!uname.equals("admin"))
	    {
	    	TextView tv=(TextView)findViewById(R.id.textView6);
	    	tv.setVisibility(View.INVISIBLE);
	    	spinner.setVisibility(View.INVISIBLE);
	    	msg.setVisibility(View.INVISIBLE);
	    }
	
		btngeo.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View arg0) 
			{
				if (msg.getText().toString().equals("")&&uname.equals("admin"))
				{
					toaster("All field are Empty");
				}
				else
				{
					if(myPolygon!=null)
					{
						List<LatLong> polygon_points=myPolygon.getLatLongs();
						String poly_form=arrangePoints(polygon_points);
						geom_sq=poly_form;
					}
					else
					{
						geom_sq="(("+geom_sq+"))";
					}
					SentDatatoUpdatePolygon();
				}
			}
		});
		
		clear=(Button)findViewById(R.id.clear_polygon);
		clear.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View arg0) 
			{
				mapview.getLayerManager().getLayers().remove(myPolygon);
				poly_points.clear();
			}
		});	

}

	protected String arrangePoints(List<LatLong> polygon_points)
	{
		String polygon="((";
		for(int i=0;i<polygon_points.size();i++)
		{
			polygon=polygon+""+polygon_points.get(i).longitude+" "+polygon_points.get(i).latitude+",";
		}
		polygon=polygon+""+polygon_points.get(0).longitude+" "+polygon_points.get(0).latitude+"))";
		Log.i("polygon", polygon);
		return polygon;
	}
	
	protected void SentDatatoUpdatePolygon() 
	{
		Receiver connect = new Receiver(this);
		connect.setPath("updatepolygon.php");
		connect.addNameValuePairs("value1", fencename);
	    connect.addNameValuePairs("value2", geom_sq);
	    connect.addNameValuePairs("value4", uname);
	    connect.addNameValuePairs("value5", String.valueOf(catagory));
	    connect.addNameValuePairs("value3", msg.getText().toString());
		AsyncTask<Void, Void, String> result=connect.execute(new Void[0]);
		try 
		{
			String res=result.get();
			Log.i("value1", fencename);
		    Log.i("value2", geom_sq);
		    Log.i("value4", uname);
		    Log.i("value5", String.valueOf(catagory));
		    Log.i("value3", msg.getText().toString());
			Log.i("result", res);
			toaster("Geofence Updated");
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

			public boolean onLongPress(LatLong tapLatLong,
	    			org.mapsforge.core.model.Point layerXY,
					org.mapsforge.core.model.Point tapXY) 
	    	{
	    		return super.onLongPress(tapLatLong, layerXY, tapXY);
			}
	    	
	    	public boolean onTap(LatLong tapLatLong, 
	    			org.mapsforge.core.model.Point layerXY, 
	    			org.mapsforge.core.model.Point tapXY) 
	    	{
	    		poly_points.add(tapLatLong);
	    		drawPolygon(poly_points, uname);
	    		return true;
	    	};
	    			
	    };
	    ((TileRendererLayer) this.tileRendererLayer).setMapFile(getMapFile());
	    ((TileRendererLayer) this.tileRendererLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
	    mapview.getLayerManager().getLayers().add(tileRendererLayer);
	}
	
	private void drawPolygon(List<LatLong> myPolygonPoints, String type) 
	{
		Paint paintFill;
		if(type.equals("admin"))
		{
			paintFill = Utils.createPaint(
					AndroidGraphicFactory.INSTANCE.createColor(80,255, 0, 102), 2,
					Style.FILL);
		}
		else
		{
			paintFill = Utils.createPaint(
					AndroidGraphicFactory.INSTANCE.createColor(80,0,0,100), 2,
					Style.FILL);
		}
		if(myPolygon!=null)
			mapview.getLayerManager().getLayers().remove(myPolygon);
		myPolygon=new Polygon(paintFill,null,AndroidGraphicFactory.INSTANCE);
		List<LatLong> latLongs = myPolygon.getLatLongs();
		for(int i=0;i<myPolygonPoints.size();i++)
		{
			latLongs.add(myPolygonPoints.get(i));
		}
		this.mapview.getLayerManager().getLayers().add(myPolygon);
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