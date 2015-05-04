package com.suresh.geofence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
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
import com.suresh.extras.UnzipUtil;
import com.suresh.extras.Utils;
import com.suresh.form.R;
import com.suresh.menus.BaseActivity;
import com.suresh.routing.GHAsyncTask;
import com.suresh.routing.POI;

@SuppressLint("ResourceAsColor")
public class geofences extends BaseActivity implements OnItemLongClickListener,LocationListener{
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
	private POI[] nearest_location=new POI[15];
	private Activity activity=null;
	private ArrayList<Polygon> user_polygon=new ArrayList<Polygon>();
	private ArrayList<Polygon> admin_polygon=new ArrayList<Polygon>();
	private ArrayList<Circle> user_circle=new ArrayList<Circle>();
	private ArrayList<Circle> admin_circle=new ArrayList<Circle>();
	private ArrayList<TappableMarker> admin_centre=new ArrayList<TappableMarker>();
	private ArrayList<TappableMarker> user_centre=new ArrayList<TappableMarker>();
	private Switch admin_on_off_swh;
	private Switch user_on_off_swh;
	
    private ProgressDialog mProgressDialog;

    String file_url="http://116.90.239.21/dfencing/girc2.zip";
    String unzipLocation = Environment.getExternalStorageDirectory().getPath()+"/";
    String StorezipFileLocation =Environment.getExternalStorageDirectory().getPath() + "/DownloadedZip"; 
    String DirectoryName=Environment.getExternalStorageDirectory().getPath()+"/";
	private GPSTracker gps_conn;
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
	    mydata=new Getdata(username, geofences.this);
	    Boolean server_msg=mydata.checkServer();
		if(haveNetworkConnection())
	    {
		    if(server_msg)
		    {
		    	getAllData();
		    }
		    else
		    {
		    	mydata.showerrordialog();
		    }
	    }
		else
		{
			mydata=null;
			toaster("No Internet Connection");
		}

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
     		   if(gps_conn.canGetLocation())
     		   {
     			   if(gps_conn.getLocation()!=null)
     		   			view.showContextMenu();
     		   			else
     		   				toaster("Waiting GPS Connection");

     		   }
     		   else
     		   {
     			   toaster("Enable GPS");
     		   }
     	   }
        });
        btn_police=(ImageButton)findViewById(R.id.police);
        registerForContextMenu(btn_police);	
        btn_police.setOnClickListener(new OnClickListener()
        {
     	   public void onClick(View view) 
     	   {
     		   if(gps_conn.canGetLocation())
     		   {
     			   if(gps_conn.getLocation()!=null)
     		   			view.showContextMenu();
     		   			else
     		   				toaster("Waiting GPS Connection");

     		   }
     		   else
     		   {
     			   toaster("Enable GPS");
     		   }
     	   }
        });
        
		distance=(TextView)findViewById(R.id.distance);
		
		admin_on_off_swh=(Switch)findViewById(R.id.switch1);
		admin_on_off_swh.setChecked(false);
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
		user_on_off_swh.setChecked(false);
		user_on_off_swh.setOnCheckedChangeListener(new OnCheckedChangeListener() 
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
    	getallPoints("hospitals");
    	getallPoints("police");
		
		
	}
      
	 public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
	 {
		 super.onCreateContextMenu(menu, v, menuInfo);

		 if(v.getId()==R.id.hospital)
		 {
			nearest_location=getNearestPoint("hospitals");
			menu.setHeaderTitle("15 Nearest Hospitals");

		 }
		 else if(v.getId()==R.id.police)
		 {
			 nearest_location=getNearestPoint("police");
			 menu.setHeaderTitle("15 Nearest Police Stations");
		 }

		 menu.add(0, v.getId(), 0, nearest_location[0].getName());
		 menu.add(0, v.getId(), 1, nearest_location[1].getName());
		 menu.add(0, v.getId(), 2, nearest_location[2].getName());
		 menu.add(0, v.getId(), 3, nearest_location[3].getName());
		 menu.add(0, v.getId(), 4, nearest_location[4].getName());
		 menu.add(0, v.getId(), 5, nearest_location[5].getName());
		 menu.add(0, v.getId(), 6, nearest_location[6].getName());
		 menu.add(0, v.getId(), 7, nearest_location[7].getName());
		 menu.add(0, v.getId(), 8, nearest_location[8].getName());
		 menu.add(0, v.getId(), 9, nearest_location[9].getName());
		 menu.add(0, v.getId(), 10, nearest_location[10].getName());
		 menu.add(0, v.getId(), 11, nearest_location[11].getName());
		 menu.add(0, v.getId(), 12, nearest_location[12].getName());
		 menu.add(0, v.getId(), 13, nearest_location[13].getName());
		 menu.add(0, v.getId(), 14, nearest_location[14].getName());
	 }

	 public boolean onContextItemSelected(MenuItem item) 
	 {
		 int position=item.getOrder();
		 calcPath(usermarker.getLatLong().latitude,usermarker.getLatLong().longitude, nearest_location[position].getLat(), nearest_location[position].getLng(), nearest_location[position].getName());
		 createEndMarker(nearest_location[position].getLat(), nearest_location[position].getLng());
		 return true;
	 }
	 
	 	private void getAllData()
	 	{
	        mydata = new Getdata(username,this);
		    if(haveNetworkConnection())
		    {
		    	String all_data = mydata.getData();
		        geofencename=mydata.convertStringtoArrayList(all_data,"name");
			    status=mydata.convertStringtoArrayList(all_data,"status");
			    type=mydata.convertStringtoArrayList(all_data,"type");
			    latitude=mydata.convertStringtoArrayList(all_data,"lat");
			    longitude=mydata.convertStringtoArrayList(all_data,"lgt");
			    radius=mydata.convertStringtoArrayList(all_data,"radius");
			    message=mydata.convertStringtoArrayList(all_data,"message");
			    shape=mydata.convertStringtoArrayList(all_data,"shape");
			    
			    String admin_all_data=mydata.getAdminData();
			    admin_geofencename=mydata.convertStringtoArrayList(admin_all_data,"name");
			    admin_status=mydata.convertStringtoArrayList(admin_all_data,"status");
			    admin_type=mydata.convertStringtoArrayList(admin_all_data,"type");
			    admin_latitude=mydata.convertStringtoArrayList(admin_all_data,"lat");
			    admin_longitude=mydata.convertStringtoArrayList(admin_all_data,"lgt");
			    admin_radius=mydata.convertStringtoArrayList(admin_all_data,"radius");
			    mydata.convertStringtoArrayList(admin_all_data,"message");
			    admin_shape=mydata.convertStringtoArrayList(admin_all_data,"shape");
		    }
		    else
		    {
		    	mydata=null;
		    	toaster("No Internet Connection");
		    }
        }

	@SuppressWarnings("deprecation")
	protected void onStart()
	{
		super.onStart();
		if(new File(MAPFILE).exists())
		{
			loadMap();
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do You Want To Download Files Now?")
			.setTitle("Download Files");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) 
				{
					DownloadZipfile mew = new DownloadZipfile();
					mew.execute(file_url);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
					finish();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();	
		}
	}
	
	private void loadMap() 
	{
		mv.getModel().mapViewPosition.setZoomLevel((byte) 18);
		mv.getModel().mapViewPosition.setCenter(new LatLong(27.627172,85.523339));
        this.tileRendererLayer = new TileRendererLayer(tileCache, mv.getModel().mapViewPosition,
        		true, AndroidGraphicFactory.INSTANCE);
        ((TileRendererLayer) this.tileRendererLayer).setMapFile(getMapFile());
        ((TileRendererLayer) this.tileRendererLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        mv.getLayerManager().getLayers().add(tileRendererLayer);
		gps_conn=new GPSTracker(geofences.this);
		if(gps_conn.canGetLocation())
		{
				mv.getModel().mapViewPosition.setCenter(new LatLong(gps_conn.getLatitude(), gps_conn.getLongitude()));
				createUserMarker(gps_conn.getLatitude(), gps_conn.getLongitude());
		}
		else
		{
			//mv.getModel().mapViewPosition.setCenter(new LatLong(85,27));
			showSettingsAlert();
		}

        if(mydata!=null)
    	{
        	Log.i("mydata", "here");
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
		    				//admin_centre.add(new TappableMarker(R.drawable.circle_icon, centre));
		    			}
		    			else
		    			{
		    				j=i;
		    				List<LatLong> myPolygonPoints=getpolygonPoints(admin_shape.get(i));
		    				admin_polygon.add(drawPolygon(myPolygonPoints,"admin"));
		    				//admin_centre.add(new TappableMarker(R.drawable.polygon_icon,new LatLong(Double.valueOf(admin_latitude.get(i)),Double.valueOf(admin_longitude.get(i)))));
		    				//this.mv.getLayerManager().getLayers().add(new TappableMarker(R.drawable.polygon_icon,centre));
		    			}
		    		}	
	        	}
	    	}	    	
    	}
    	loadGraphStorage();

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
	private void user_on()
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
					AndroidGraphicFactory.convertToBitmap(geofences.this.getResources().getDrawable(user)),0,0);
		}
		
		public TappableMarker(int user, LatLong localLatLong,String markertype)
		{
			super(localLatLong,
					AndroidGraphicFactory.convertToBitmap(geofences.this.getResources().getDrawable(user)),0,-1*AndroidGraphicFactory.convertToBitmap(geofences.this.getResources().getDrawable(user)).getHeight()/2);
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
	
	private List<LatLong> getpolygonPointsfromJSON(String shape)
	{
		List<LatLong> my_points=new ArrayList<LatLong>();
		String vertices=shape.substring(3,shape.length()-3);
		String part="\\],\\[";
		String[] points=vertices.split(part);
		for(int j=0;j<points.length-1;j++)
		{
			String[] seperate=points[j].split(",");
        	LatLong point = new LatLong(Double.valueOf(seperate[1]),Double.valueOf(seperate[0]));
        	my_points.add(point);
		}
		return my_points;
	}
	
	private List<LatLong> getmultipolygonPointsfromJSON(String string)
	{
		return null;
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
		POI[] localPOI = new POI[15];
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
		POI[] localObject = new POI[15];
		double[] distances=new double[15];
		for(int i=0;i<15;i++)
		{
			localObject[i]=paramArrayList.get(i);
		    double distance = Math.sqrt(Math.pow(paramDouble1 - localObject[i].getLat(), 2.0D) + Math.pow(paramDouble2 - localObject[i].getLng(), 2.0D));
		    distances[i]=distance;
		}
		//Sorting nearest to farthest acc to distance
		for(int i=0;i<14;i++)
		{
			for(int j=i+1;j<15;j++)
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
		
		for(int i=15;i<paramArrayList.size();i++)
		{
		    POI localPOI = paramArrayList.get(i);
		    double d2 = Math.sqrt(Math.pow(paramDouble1 - localPOI.getLat(), 2.0D) + Math.pow(paramDouble2 - localPOI.getLng(), 2.0D));
		    if(d2<distances[14])
		    {
		    	localObject[14]=localPOI;
		    	distances[14]=d2;
		    	
				//Re Sorting nearest to farthest acc to distance
				for(int i1=0;i1<14;i1++)
				{
					for(int j=i1+1;j<15;j++)
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
	        if(localJSONObject.getJSONObject("geometry").getString("type").equals("Point"))
	        {
	        	localArrayList.add
	        	(
	        			new POI(Double.parseDouble(localJSONObject.getJSONObject("geometry").getJSONArray("coordinates").getString(1)),
	        				Double.parseDouble(localJSONObject.getJSONObject("geometry").getJSONArray("coordinates").getString(0)),
	        				str1,
	        				str2,
	        				id));
	        }
	        else if((!localJSONObject.getJSONObject("properties").has("type"))
	        	&&(localJSONObject.getJSONObject("geometry").getString("type").equals("Polygon")))
	        	{
	        		JSONArray poygon = localJSONObject.getJSONObject("geometry").getJSONArray("coordinates");
	        		List<LatLong> myPolygonPoints = getpolygonPointsfromJSON(poygon.toString());
	        		LatLong centre=calculateCentroid(myPolygonPoints);
	        		localArrayList.add(new POI(centre.latitude,centre.longitude, str1,str2,id));
	        }
	        else if((!localJSONObject.getJSONObject("properties").has("type"))
	        		&&localJSONObject.getJSONObject("properties").getString("type").equals("multipolygon"))
	        {
        		JSONArray poygon = localJSONObject.getJSONObject("geometry").getJSONArray("coordinates");
        		List<LatLong> myPolygonPoints = getpolygonPointsfromJSON(poygon.getString(0).toString());
        		LatLong centre=calculateCentroid(myPolygonPoints);
        		localArrayList.add(new POI(centre.latitude,centre.longitude, str1,str2,id));
	        }
	        
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


	  
	  public LatLong calculateCentroid(List<LatLong> coordinates) {
		    double x = 0.;
		    double y = 0.;
		    int pointCount = coordinates.size();
		    for (int i = 0;i<pointCount;i++){
		        x += coordinates.get(i).latitude;
		        y += coordinates.get(i).longitude;
		    }
		    x = x/pointCount;
		    y = y/pointCount;

		    return new LatLong(x, y);
		}
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
      //-This is method is used for Download Zip file from server and store in Desire location.
      class DownloadZipfile extends AsyncTask<String, String, String>
      {
    	  String result ="";
    	  @Override
    	  protected void onPreExecute()
    	  {
    		  super.onPreExecute();
	          mProgressDialog = new ProgressDialog(geofences.this);
	          mProgressDialog.setMessage("Downloading...");
	          mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	          mProgressDialog.setCancelable(false);
	          mProgressDialog.show();
    	  }
    	  
    	  @Override
    	  protected String doInBackground(String... aurl)
    	  {
    		  int count;
    		  try
    		  {
    			  URL url = new URL(aurl[0]);
    			  URLConnection conexion = url.openConnection();
    			  conexion.connect();
    			  int lenghtOfFile = conexion.getContentLength();
    			  InputStream input = new BufferedInputStream(url.openStream());
    			  OutputStream output = new FileOutputStream(StorezipFileLocation);
    			  byte data[] = new byte[1024];
    			  long total = 0;
    			  while ((count = input.read(data)) != -1)
    			  {
    				  total += count;
    				  publishProgress(""+(int)((total*100)/lenghtOfFile));
    				  output.write(data, 0, count);
    			  }
    			  output.close();
    			  input.close();
    			  result = "true";
    		  }
    		  catch (Exception e) 
    		  {
    			  result = "false";
    		  }
    		  return null;
    	  }
    	  
    	  protected void onProgressUpdate(String... progress)
    	  {
    		  Log.d("ANDRO_ASYNC",progress[0]);
    		  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    	  }

    	  @Override
    	  protected void onPostExecute(String unused)
    	  {
    		  mProgressDialog.dismiss();
    		  if(result.equalsIgnoreCase("true"))
    		  {
    			  try
    			  {
    				  unzip();
    			  }
    			  catch (IOException e)
    			  {
    				  // TODO Auto-generated catch block
    				  e.printStackTrace();
    			  }
    		  }
    		  else
    		  {
    		  }
    	  }
      }
      
      //This is the method for unzip file which is store your location. And unzip folder will                 store as per your desire location.
      public void unzip() throws IOException 
      {
    	  mProgressDialog = new ProgressDialog(geofences.this);
          mProgressDialog.setMessage("Please Wait...");
          mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          mProgressDialog.setCancelable(false);
          mProgressDialog.show();
          new UnZipTask().execute(StorezipFileLocation, DirectoryName);
      }

      private class UnZipTask extends AsyncTask<String, Void, Boolean> 
      {
    	  @SuppressWarnings("rawtypes")
    	  @Override
    	  protected Boolean doInBackground(String... params) 
    	  {
    		  String filePath = params[0];
    		  String destinationPath = params[1];
    		  File archive = new File(filePath);
    		  try 
    		  {
    			  ZipFile zipfile = new ZipFile(archive);
    			  for (Enumeration e = zipfile.entries(); e.hasMoreElements();) 
    			  {
    				  ZipEntry entry = (ZipEntry) e.nextElement();
    				  unzipEntry(zipfile, entry, destinationPath);
    			  }
    			  UnzipUtil d = new UnzipUtil(StorezipFileLocation, DirectoryName); 
    			  d.unzip();
    		  } 
    		  catch (Exception e) 
    		  {
    			  return false;
    		  }
    		  return true;
    	  }
    	  @Override
    	  protected void onPostExecute(Boolean result) 
    	  {
    		  mProgressDialog.dismiss(); 
    		  loadMap();

    	  }

    	  private void unzipEntry(ZipFile zipfile, ZipEntry entry,String outputDir) throws IOException 
    	  {
    		  if (entry.isDirectory()) 
    		  {
    			  createDir(new File(outputDir, entry.getName()));
    			  return;
    		  }
    		  File outputFile = new File(outputDir, entry.getName());
    		  if (!outputFile.getParentFile().exists())
    		  {
    			  createDir(outputFile.getParentFile());
    		  }
    		  // Log.v("", "Extracting: " + entry);
    		  BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
    		  BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
    		  try 
    		  {
    		  }
    		  finally 
    		  {
    			  outputStream.flush();
    			  outputStream.close();
    			  inputStream.close();
    		  }
    	  }

    	  private void createDir(File dir) 
    	  {
    		  if (dir.exists()) 
    		  {
    			  return;
    		  }
    		  if (!dir.mkdirs()) 
    		  {
    			  throw new RuntimeException("Can not create dir " + dir);
    		  }
    	  }
      }
      
      public boolean haveNetworkConnection() 
      {
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

	@Override
	public void onLocationChanged(Location location) 
	{
		createUserMarker(location.getLatitude(), location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                dialog.cancel();
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
        alertDialog.setCancelable(false);
        // Showing Alert Message
        alertDialog.show();
    }

}
