package com.suresh.geofence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Polygon;

import android.annotation.SuppressLint;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.suresh.extras.SessionManager;
import com.suresh.extras.TappableMarker;
import com.suresh.extras.Utils;
import com.suresh.extras.mymapview;
import com.suresh.form.Form;
import com.suresh.form.R;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class updategeofence extends TabActivity 
{
	String geofencetype;
	String shape;
	String lats;
	String lgts;
	String radius;
	String catagory;
	String message;
	static String polygon_shape;
	static public String fencename,old_lat="",old_lgt="",old_rad="";
    public ArrayList<Point> poly_vertices=new ArrayList<Point>();
    static int no;
    String overlay_type;
	TabHost tabHost;
    //mymapview mv;
    String port=Form.fence;
	private String uname;
	private Circle myCircle;
	private TappableMarker usermarker;
	private Polygon myPolygon;
	
    public void onCreate(Bundle savedInstanceState)
    {       
    	super.onCreate(savedInstanceState);
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	StrictMode.setThreadPolicy(policy);
    	setContentView(R.layout.admin_update);
    	SessionManager session = new SessionManager(getApplicationContext());
    	HashMap<String, String> user = session.getUserDetails();
    	uname = user.get(SessionManager.KEY_EMAIL);
        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {    
    		fencename =extras.getString("fencename");
    		message = extras.getString("message");
    		geofencetype = extras.getString("type");
    		shape= extras.getString("shape");
    		lats=extras.getString("lat");
    		lgts=extras.getString("lgt");
    		radius=extras.getString("radius");
    		catagory=extras.getString("catagory");
        }

    	tabHost = (TabHost)findViewById(android.R.id.tabhost);
    	TabSpec tab1 = tabHost.newTabSpec("First Tab");
    	TabSpec tab2 = tabHost.newTabSpec("Second Tab");
    	tab1.setIndicator("Circle");
    	tab1.setContent(new Intent().setClass(updategeofence.this,circleupdatefragment.class));
    	tab2.setIndicator("polygon");
    	tab2.setContent(new Intent().setClass(updategeofence.this,polygonupdatefragment.class));
    	tabHost.addTab(tab1);
    	tabHost.addTab(tab2);
		if(geofencetype.equals("0"))
    					{
    						tabHost.setCurrentTab(0);
    						old_lat=lats;
    						old_lgt=lgts;
    						old_rad=radius;
    						circleupdatefragment.msg.setText(message);
    						circleupdatefragment.lgt.setText(lgts);
    						circleupdatefragment.lat.setText(lats);
    						circleupdatefragment.rad.setText(radius);
    						circleupdatefragment.spinner.setSelection(Integer.valueOf(catagory)-1);
    						drawCircles(circleupdatefragment.mapview, new LatLong(Double.valueOf(lats), Double.valueOf(lgts)), Float.valueOf(radius), uname);
    					}
    					else
    					{    						
    						polygon_shape=shape.substring(9,shape.length()-2);
    						overlay_type="polygon";
    						tabHost.setCurrentTab(1);
    						polygonupdatefragment.msg.setText(message);
    						polygonupdatefragment.spinner.setSelection(Integer.valueOf(catagory)-1);
    						List<LatLong> poly_points=convertStringToPointList(polygon_shape);
    						drawPolygon(poly_points, uname);
    					}
   
    }
	
    private List<LatLong> convertStringToPointList(String polygon)
    {
    	Log.i("old polygon", polygon);
    	List<LatLong> old_polygon=new ArrayList<LatLong>();
    	String[] points=polygon.split(",");
    	for(int i=0;i<points.length;i++)
    	{
    		String[] coordinates=points[i].split(" ");
    		old_polygon.add(new LatLong(Double.valueOf(coordinates[1]), Double.valueOf(coordinates[0])));
    	}
    	old_polygon.remove(old_polygon.size()-1);
		return old_polygon;
	}

	public void toaster(String s)
    {
    	Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
	

	private void drawCircles(mymapview mapview,LatLong latlong,float rad, String type)
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
        mapview.getModel().mapViewPosition.setCenter(centre);
		createuserMarker(centre.latitude, centre.longitude);
	}
	
	protected void createuserMarker(double latitude2, double longitude2) 
	{
		if(usermarker!=null)
			circleupdatefragment.mapview.getLayerManager().getLayers().remove(usermarker);
		final LatLong localLatLong = new LatLong(latitude2, longitude2);
		this.usermarker = new com.suresh.extras.TappableMarker(updategeofence.this,R.drawable.redmarker, localLatLong);
		circleupdatefragment.mapview.getLayerManager().getLayers().add(usermarker);
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
			polygonupdatefragment.mapview.getLayerManager().getLayers().remove(myPolygon);
		myPolygon=new Polygon(paintFill,null,AndroidGraphicFactory.INSTANCE);
		List<LatLong> latLongs = myPolygon.getLatLongs();
		for(int i=0;i<myPolygonPoints.size();i++)
		{
			latLongs.add(myPolygonPoints.get(i));
		}
		polygonupdatefragment.mapview.getLayerManager().getLayers().add(myPolygon);
	}


}