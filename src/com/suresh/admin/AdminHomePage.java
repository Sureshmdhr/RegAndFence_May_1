package com.suresh.admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.suresh.extras.GPSTracker;
import com.suresh.extras.SessionManager;
import com.suresh.extras.Utils;
import com.suresh.form.R;
import com.suresh.geofence.FenceService;
import com.suresh.geofence.Getdata;
import com.suresh.routing.GHAsyncTask;
import com.suresh.routing.POI;

@SuppressLint("ResourceAsColor")
public class AdminHomePage extends AdminMenuActivity implements OnItemLongClickListener{
	Point[][] my_polygon;
	public ArrayList<String> longitude=new ArrayList<String>();
	public ArrayList<String> latitude=new ArrayList<String>();
	public ArrayList<String> radius=new ArrayList<String>();
	public ArrayList<String> shape=new ArrayList<String>();
	public ArrayList<String> type=new ArrayList<String>();
    public ArrayList<Point> poly_vertices=new ArrayList<Point>();
	public ArrayList<String> message,userlat,userlgt,uname,geofencename,fencenames,status=new ArrayList<String>();
	private String username;
	
	private ArrayList<String> admin_geofencename=new ArrayList<String>();
	private ArrayList<String> admin_type=new ArrayList<String>();
	private ArrayList<String> admin_shape=new ArrayList<String>();
	private ArrayList<String> admin_latitude=new ArrayList<String>();
	private ArrayList<String> admin_longitude=new ArrayList<String>();
	private ArrayList<String> admin_radius=new ArrayList<String>();
	public static MapView mv;

	String MAPFILE =Environment.getExternalStorageDirectory().getPath()+"/girc/nepal-gh/nepal.map";
	String MAPPATH =Environment.getExternalStorageDirectory().getPath()+"/girc/nepal-gh/";
	private TileCache tileCache;
	private TappableMarker usermarker;
	private Layer tileRendererLayer;
	int j;
	private ArrayList<POI> hospitals;
    private GraphHopper hopper;
	protected Polyline localPolyline;
	private ImageButton btn_hospital;
	private ImageButton btn_police;
	private ArrayList<POI> police;
	private TextView distance;
	private TappableMarker endmarker;
	static boolean is_first = true;
	private Getdata mydata;
	private ArrayList<String> admin_status;
	private POI[] nearest_location=new POI[5];
	private Activity activity=null;
	private ArrayList<Polygon> user_polygon=new ArrayList<Polygon>();
	private ArrayList<Polygon> admin_polygon=new ArrayList<Polygon>();
	private ArrayList<Circle> user_circle=new ArrayList<Circle>();
	private ArrayList<Circle> admin_circle=new ArrayList<Circle>();
	private ArrayList<TappableMarker> admin_centre=new ArrayList<TappableMarker>();
	private ArrayList<TappableMarker> user_centre=new ArrayList<TappableMarker>();
	private Switch admin_on_off_swh;
	private Switch user_on_off_swh;
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		AndroidGraphicFactory.createInstance(this.getApplication());

		getLayoutInflater().inflate(R.layout.activity_geofence, frameLayout);

		mDrawerList.setItemChecked(position, true);
		
		setTitle(navMenuTitles[position]);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
	    SessionManager session = new SessionManager(getApplicationContext());
	    HashMap<String, String> user = session.getUserDetails();
	    username = user.get(SessionManager.KEY_EMAIL);
	    getAllData(); 
        mv=(MapView)findViewById(R.id.map);
        mv.setClickable(true);
        mv.setBuiltInZoomControls(true);
        tileCache = AndroidUtil.createTileCache(this, "mapcache",
        		mv.getModel().displayModel.getTileSize(), 1f, 
        		mv.getModel().frameBufferModel.getOverdrawFactor());
        mv.getMapZoomControls().setZoomLevelMin((byte) 10);
        mv.getMapZoomControls().setZoomLevelMax((byte) 18);
        
        new FenceService(this).startService(15000);
        btn_hospital=(ImageButton)findViewById(R.id.hospital);
    	registerForContextMenu(btn_hospital);
        btn_hospital.setOnClickListener(new OnClickListener()
        {
     	   public void onClick(View view) 
     	   {
     		   view.showContextMenu();
     	   }
        });
        btn_police=(ImageButton)findViewById(R.id.police);
        registerForContextMenu(btn_police);	
        btn_police.setOnClickListener(new OnClickListener()
        {
     	   public void onClick(View view) 
     	   {
     		   view.showContextMenu();
     	   }
        });
        
		distance=(TextView)findViewById(R.id.distance);
		
		admin_on_off_swh=(Switch)findViewById(R.id.switch1);
		admin_on_off_swh.setChecked(true);
		admin_on_off_swh.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				if(isChecked)
				{
					admin_on();
				}
				else
				{
					admin_off();
				}
			}
		});
		
		user_on_off_swh=(Switch)findViewById(R.id.switch2);
		user_on_off_swh.setVisibility(View.GONE);
		user_on_off_swh.setChecked(true);
/*		user_on_off_swh.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				if(isChecked)
				{
					user_on();
				}
				else
				{
					user_off();
				}
			}
		});
		
*/		
	}
      
	 public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
	 {
		 super.onCreateContextMenu(menu, v, menuInfo);

		 if(v.getId()==R.id.hospital)
		 {
			nearest_location=getNearestPoint("hospitals");
			menu.setHeaderTitle("5 Nearest Hospitals");

		 }
		 else if(v.getId()==R.id.police)
		 {
			 nearest_location=getNearestPoint("police");
			 menu.setHeaderTitle("5 Nearest Police Stations");
		 }

		 menu.add(0, v.getId(), 0, nearest_location[0].getName());
		 menu.add(0, v.getId(), 1, nearest_location[1].getName());
		 menu.add(0, v.getId(), 2, nearest_location[2].getName());
		 menu.add(0, v.getId(), 3, nearest_location[3].getName());
		 menu.add(0, v.getId(), 4, nearest_location[4].getName());
	 }

	 public boolean onContextItemSelected(MenuItem item) 
	 {
		 int position=item.getOrder();
		 GPSTracker gps=new GPSTracker(this);
		 calcPath(gps.getLatitude(), gps.getLongitude(), nearest_location[position].getLat(), nearest_location[position].getLng(), nearest_location[position].getName());
		 createEndMarker(nearest_location[position].getLat(), nearest_location[position].getLng());
		 return true;
	 }
	 
	 	private void getAllData()
	 	{
	        mydata = new Getdata(username,this);
		    if(new GPSTracker(getApplicationContext()).haveNetworkConnection())
		    {
		    	String all_data=mydata.getData();
		        geofencename=mydata.convertStringtoArrayList(all_data,"name");
			    status=mydata.convertStringtoArrayList(all_data,"status");
			    type=mydata.convertStringtoArrayList(all_data,"type");
			    latitude=mydata.convertStringtoArrayList(all_data,"lat");
			    longitude=mydata.convertStringtoArrayList(all_data,"lgt");
			    radius=mydata.convertStringtoArrayList(all_data,"radius");
			    message=mydata.convertStringtoArrayList(all_data,"message");
			    shape=mydata.convertStringtoArrayList(all_data,"shape");
			    
/*			    admin_geofencename=mydata.getAdminData("name");
			    admin_status=mydata.getAdminData("status");
			    admin_type=mydata.getAdminData("type");
			    admin_latitude=mydata.getAdminData("lat");
			    admin_longitude=mydata.getAdminData("lgt");
			    admin_radius=mydata.getAdminData("radius");
			    mydata.getAdminData("message");
			    admin_shape=mydata.getAdminData("shape");
*/		    }
		    else
		    {
		    	mydata=null;
		    	toaster("No Internet Connection");
		    }
        }

	protected void onStart()
	{
		super.onStart();
        GPSTracker gps=new GPSTracker(this);
        mv.getModel().mapViewPosition.setZoomLevel((byte) 12);
        mv.getModel().mapViewPosition.setCenter(new LatLong(gps.getLatitude(), gps.getLongitude()));
        this.tileRendererLayer = new TileRendererLayer(tileCache, mv.getModel().mapViewPosition,
        		true, AndroidGraphicFactory.INSTANCE);
        ((TileRendererLayer) this.tileRendererLayer).setMapFile(getMapFile());
        ((TileRendererLayer) this.tileRendererLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        mv.getLayerManager().getLayers().add(tileRendererLayer);
        createUserMarker(gps.getLatitude(), gps.getLongitude());
    	
        if(mydata!=null)
    	{
	        for(int i=0;i<shape.size();i++)
	    	{
	    		if(status.get(i).equals("1"))
	    		{
	    			if(type.get(i).equals("0"))
	    			{
	    				user_circle.add(drawCircles(new LatLong(Double.valueOf(latitude.get(i)), Double.valueOf(longitude.get(i))),Float.valueOf(radius.get(i)),geofencename.get(i),"user"));
	    				LatLong centre=new LatLong(Double.valueOf(latitude.get(i)),Double.valueOf(longitude.get(i)));
	    				user_centre.add(new TappableMarker(R.drawable.circle_icon, centre));
	    			}
	    			else
	    			{
	    				j=i;
	    				List<LatLong> myPolygonPoints=getpolygonPoints(shape.get(i));
	    				user_polygon.add(drawPolygon(myPolygonPoints,"user"));
	    				user_centre.add(new TappableMarker(R.drawable.polygon_icon, new LatLong(Double.valueOf(latitude.get(i)),Double.valueOf(longitude.get(i)))));
	
	    			}
	    		}	
	    	}
	    	if(!username.equals("admin"))
	    	{
	        	for(int i=0;i<admin_shape.size();i++)
	        	{
	        		if(admin_status.get(i).equals("1"))
		    		{
		    			if(admin_type.get(i).equals("0"))
		    			{
		    				admin_circle.add(drawCircles(new LatLong(Double.valueOf(admin_latitude.get(i)), Double.valueOf(admin_longitude.get(i))),Float.valueOf(admin_radius.get(i)),admin_geofencename.get(i),"admin"));
		    				LatLong centre=new LatLong(Double.valueOf(admin_latitude.get(i)),Double.valueOf(admin_longitude.get(i)));
		    				admin_centre.add(new TappableMarker(R.drawable.circle_icon, centre));
		    			}
		    			else
		    			{
		    				j=i;
		    				List<LatLong> myPolygonPoints=getpolygonPoints(admin_shape.get(i));
		    				admin_polygon.add(drawPolygon(myPolygonPoints,"admin"));
		    				admin_centre.add(new TappableMarker(R.drawable.polygon_icon,new LatLong(Double.valueOf(admin_latitude.get(i)),Double.valueOf(admin_longitude.get(i)))));
		    				//this.mv.getLayerManager().getLayers().add(new TappableMarker(R.drawable.polygon_icon,centre));
		    			}
		    		}	
	        	}
	    	}	    	
    	}
    	admin_on();
    	//user_on();

    	loadGraphStorage();
    	getallPoints("hospitals");
    	getallPoints("police");
	}
	
	private void admin_on()
	{
		for(int i=0;i<admin_centre.size();i++)
		{
			mv.getLayerManager().getLayers().add(admin_centre.get(i));
		}
    	for(int i=0;i<admin_circle.size();i++)
    	{
    		mv.getLayerManager().getLayers().add(admin_circle.get(i));
    	}
    	
    	for(int i=0;i<admin_polygon.size();i++)
    	{
    		mv.getLayerManager().getLayers().add(admin_polygon.get(i));
    	}

	}
	private void admin_off()
	{
		for(int i=0;i<admin_centre.size();i++)
		{
			mv.getLayerManager().getLayers().remove(admin_centre.get(i));
		}
    	for(int i=0;i<admin_circle.size();i++)
    	{
    		mv.getLayerManager().getLayers().remove(admin_circle.get(i));
    	}
    	for(int i=0;i<admin_polygon.size();i++)
    	{
    		mv.getLayerManager().getLayers().remove(admin_polygon.get(i));
    	}
	}
/*	private void user_on()
	{
		for(int i=0;i<user_centre.size();i++)
		{
			mv.getLayerManager().getLayers().add(user_centre.get(i));
		}
    	for(int i=0;i<user_circle.size();i++)
    	{
    		mv.getLayerManager().getLayers().add(user_circle.get(i));
    	}
    	
    	for(int i=0;i<user_polygon.size();i++)
    	{
    		mv.getLayerManager().getLayers().add(user_polygon.get(i));
    	}

	}
	private void user_off()
	{
		for(int i=0;i<user_centre.size();i++)
		{
			mv.getLayerManager().getLayers().remove(user_centre.get(i));
		}
    	for(int i=0;i<user_circle.size();i++)
    	{
    		mv.getLayerManager().getLayers().remove(user_circle.get(i));
    	}
    	for(int i=0;i<user_polygon.size();i++)
    	{
    		mv.getLayerManager().getLayers().remove(user_polygon.get(i));
    	}
	}

*/	
	private Circle drawCircles(LatLong latlong,float rad,final String name, String type)
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
		Circle myCircle=new Circle(latlong, rad,paintFill, null);
		return myCircle;
	 }

	 private Polygon drawPolygon(List<LatLong> myPolygonPoints, String type) 
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
		 Polygon myPolygon=new Polygon(paintFill,null,AndroidGraphicFactory.INSTANCE);
		 List<LatLong> latLongs = myPolygon.getLatLongs();
		 for(int i=0;i<myPolygonPoints.size();i++)
		 {
			 latLongs.add(myPolygonPoints.get(i));
		 }
		// this.mv.getLayerManager().getLayers().add(myPolygon);
		// LatLong centre = myPolygon.getPosition();
		// this.mv.getLayerManager().getLayers().add(new TappableMarker(R.drawable.blurmarker, centre));
		 return myPolygon;
	 }


	private class TappableMarker extends Marker
	{
		public TappableMarker(int user, LatLong localLatLong)
		{ 
			super(localLatLong,
					AndroidGraphicFactory.convertToBitmap(AdminHomePage.this.getResources().getDrawable(user)),0,0);
		}
		
		public TappableMarker(int user, LatLong localLatLong,String markertype)
		{
			super(localLatLong,
					AndroidGraphicFactory.convertToBitmap(AdminHomePage.this.getResources().getDrawable(user)),0,-1*AndroidGraphicFactory.convertToBitmap(AdminHomePage.this.getResources().getDrawable(user)).getHeight()/2);
		}

		public boolean onTap(LatLong tapLatLong,
				org.mapsforge.core.model.Point layerXY,
				org.mapsforge.core.model.Point tapXY) 
		{
			return super.onTap(tapLatLong, layerXY, tapXY);
		}
	}
	
	public void toaster(String s)
	{
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
	
	private List<LatLong> getpolygonPoints(String shape)
	{
		List<LatLong> my_points=new ArrayList<LatLong>();
		String vertices=shape.substring(9,shape.length()-2);
		String[] points=vertices.split(",");
		for(int j=0;j<points.length-1;j++)
		{
			String[] seperate=points[j].split(" ");
        	LatLong point = new LatLong(Double.valueOf(seperate[1]),Double.valueOf(seperate[0]));
        	my_points.add(point);
		}
		return my_points;
	}

	private void createUserMarker(double paramDouble1, double paramDouble2)
	{
		final LatLong localLatLong = new LatLong(paramDouble1, paramDouble2);
		if(usermarker!=null)
			mv.getLayerManager().getLayers().remove(usermarker);
		this.usermarker = new TappableMarker(R.drawable.markericon, localLatLong)
		{
			public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY)
			{
/*				mv.getModel().mapViewPosition.setCenter(tapLatLong);
				mv.getModel().mapViewPosition.setZoomLevel((byte) 16);
				toaster("Your Position");
*/				return super.onTap(tapLatLong, layerXY, tapXY);
			}
			
		};
		mv.getLayerManager().getLayers().add(usermarker);
		this.mv.getModel().mapViewPosition.setCenter(localLatLong);
	}
	
	private File getMapFile() 
	{	
		File file = new File(MAPFILE);
		return file;
	}

	protected void onStop() 
	{
		super.onStop();
		this.mv.getLayerManager().getLayers().remove(this.tileRendererLayer);
		this.tileRendererLayer.onDestroy();
	}

	protected void onDestroy() 
	{
		super.onDestroy();
		this.tileCache.destroy();
		//this.mv.getModel().mapViewPosition.destroy();
		//this.mv.destroy();
		AndroidResourceBitmap.clearResourceBitmaps();
	}
	
	public POI[] getNearestPoint(String type)
	{
		POI[] localPOI = new POI[5];
		if(type.equals("hospitals"))
		{
			localPOI = findNearestFivePoints(this.hospitals, usermarker.getLatLong().latitude, usermarker.getLatLong().longitude);
		}
		else if(type.equals("police"))
		{
			localPOI = findNearestFivePoints(this.police, usermarker.getLatLong().latitude, usermarker.getLatLong().longitude);
		}
	      //createEndMarker(localPOI.getLat(),localPOI.getLng());
	     // GPSTracker gps=new GPSTracker(this);
	     // calcPath(gps.getLatitude(), gps.getLongitude(), localPOI.getLat(), localPOI.getLng(),localPOI.getName());
	      return localPOI;
	}
	
	private void createEndMarker(double lat, double lng) 
	{
		if(endmarker!=null)
		{
			mv.getLayerManager().getLayers().remove(endmarker);
		}
		final LatLong localLatLong = new LatLong(lat, lng);
		this.endmarker = new TappableMarker(R.drawable.redmarker, localLatLong,"endmarker");
		mv.getLayerManager().getLayers().add(endmarker);
		this.mv.getModel().mapViewPosition.setCenter(localLatLong);

	}

	private POI[] findNearestFivePoints(ArrayList<POI> paramArrayList, double paramDouble1, double paramDouble2)
	{
		POI[] localObject = new POI[5];
		double[] distances=new double[5];
		for(int i=0;i<5;i++)
		{
			localObject[i]=paramArrayList.get(i);
		    double distance = Math.sqrt(Math.pow(paramDouble1 - localObject[i].getLat(), 2.0D) + Math.pow(paramDouble2 - localObject[i].getLng(), 2.0D));
		    distances[i]=distance;
		}
		//Sorting nearest to farthest acc to distance
		for(int i=0;i<4;i++)
		{
			for(int j=i+1;j<5;j++)
			{
				if(distances[i]>distances[j])
				{
					POI tempPOI=localObject[i];
					localObject[i]=localObject[j];
					localObject[j]=tempPOI;
					
					double tempDis=distances[i];
					distances[i]=distances[j];
					distances[j]=tempDis;
				}
			}
		}
		
		for(int i=5;i<paramArrayList.size();i++)
		{
		    POI localPOI = paramArrayList.get(i);
		    double d2 = Math.sqrt(Math.pow(paramDouble1 - localPOI.getLat(), 2.0D) + Math.pow(paramDouble2 - localPOI.getLng(), 2.0D));
		    if(d2<distances[4])
		    {
		    	localObject[4]=localPOI;
		    	distances[4]=d2;
		    	
				//Re Sorting nearest to farthest acc to distance
				for(int i1=0;i1<4;i1++)
				{
					for(int j=i1+1;j<5;j++)
					{
						if(distances[i1]>distances[j])
						{
							POI tempPOI=localObject[i1];
							localObject[i1]=localObject[j];
							localObject[j]=tempPOI;
							
							double tempDis=distances[i1];
							distances[i1]=distances[j];
							distances[j]=tempDis;
						}
					}
				}

		    }
		}
		return localObject;
	}

	void getallPoints(String type)
	{
		String tContents ="";
		try 
		{
	        InputStream stream = getAssets().open(type+".json");

	        int size = stream.available();
	        byte[] buffer = new byte[size];
	        stream.read(buffer);
	        stream.close();
	        tContents = new String(buffer);
	        if(type.equals("hospitals"))
	        {
	        	this.hospitals=getPointsFromJSON(tContents);
	        }
	        else if(type.equals("police"))
	        {
	        	this.police=getPointsFromJSON(tContents);
	        }
		} 
		catch (IOException e) 
		{

		}	
	}
	
	public ArrayList<POI> getPointsFromJSON(String paramString)
	  {
	    ArrayList<POI> localArrayList = new ArrayList<POI>();
	    try
	    {
	      JSONArray localJSONArray = new JSONObject(paramString).getJSONArray("features");
	      for (int i = 0; ; i++)
	      {
	        if (i >= localJSONArray.length())
	          return localArrayList;
	        JSONObject localJSONObject = localJSONArray.getJSONObject(i);
	        String str1 = localJSONObject.getJSONObject("properties").optString("name", "Unknown");
	        String str2 = localJSONObject.getJSONObject("properties").optString("amenity", "null");
	        String id = localJSONObject.getJSONObject("properties").optString("@id", "null");
	        localArrayList.add
	        (
	        		new POI(Double.parseDouble(localJSONObject.getJSONObject("geometry").getJSONArray("coordinates").getString(1)),
	        				Double.parseDouble(localJSONObject.getJSONObject("geometry").getJSONArray("coordinates").getString(0)),
	        				str1,
	        				str2,
	        				id));
	      }
	    }
	    catch (Exception localException)
	    {
	      localException.printStackTrace();
	    }
	    return localArrayList;
	  }
	  
	  
	void loadGraphStorage()
  {
      new GHAsyncTask<Void, Void, Path>()
      {
		protected Path saveDoInBackground( Void... v ) throws Exception
          {
              GraphHopper tmpHopp = new GraphHopper().forMobile();
              tmpHopp.load(MAPPATH);  
              hopper = tmpHopp;
              return null;
          }

          protected void onPostExecute( Path o )
          {
              if (hasError())
              {
                  logUser("An error happend while creating graph:"
                          + getErrorMessage());
                  log("An error happend while creating graph:"
                          + getErrorMessage());
              } else
              {
                  log("Finished loading graph. Press long to define where to start and end the route.");
              }
          }
      }.execute();
  }

	public void calcPath( final double fromLat, final double fromLon,
          final double toLat, final double toLon, final String name )
  {

      log("calculating path ...");
      new AsyncTask<Void, Void, GHResponse>()
      {
          float time;

          protected GHResponse doInBackground( Void... v )
          {
              StopWatch sw = new StopWatch().start();
              GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                      setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);
              req.getHints().put("instructions", true);
              GHResponse resp = hopper.route(req);
              time = sw.stop().getSeconds();
              return resp;
          }

          protected void onPostExecute( GHResponse resp )
          {
              if (!resp.hasErrors())
              {
          		if(localPolyline!=null){
          			mv.getLayerManager().getLayers().remove(localPolyline);
          		}
          		else{
          			log("here");
          		}
                  log("from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                          + toLon + " found path with distance:" + resp.getDistance()
                          / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                          + time + " " + resp.getDebugInfo());
                  logUser("the route is " + (int) (resp.getDistance() / 100) / 10f
                          + "km long, time:" + resp.getMillis() / 60000f + "min, debug:" + time);
                  
                  distance.setText(name+"\n"+(int) (resp.getDistance() / 100) / 10f+ "km long");
                  
                  localPolyline=createPolyline(resp);
                  mv.getLayerManager().getLayers().add(localPolyline);
                  mv.getModel().mapViewPosition.setCenter(new LatLong(toLat, toLon));
                  mv.getModel().mapViewPosition.setZoomLevel((byte) 16);
              } else
              {
                  logUser("Error:" + resp.getErrors());
              }
          }
      }.execute();
  }
  
	private Polyline createPolyline( GHResponse response )
  {
      Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
      paintStroke.setStyle(Style.STROKE);
      paintStroke.setColor(Color.BLUE);

      paintStroke.setStrokeWidth(8);

      Polyline line = new Polyline((org.mapsforge.core.graphics.Paint) paintStroke, AndroidGraphicFactory.INSTANCE);
      List<LatLong> geoPoints = line.getLatLongs();
      PointList tmp = response.getPoints();
      for (int i = 0; i < response.getPoints().getSize(); i++)
      {
          geoPoints.add(new LatLong(tmp.getLatitude(i), tmp.getLongitude(i)));
      }

      return line;
  }
  
	private void log( String str )
  {
      Log.i("GH", str);
  }

	private void logUser( String str )
  {
      Toast.makeText(this, str, Toast.LENGTH_LONG).show();
  }

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) 
	{
        activity.closeContextMenu();
		return false;
	}


	  public boolean haveNetworkConnection() {
		    boolean haveConnectedWifi = false;
		    boolean haveConnectedMobile = false;
		    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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