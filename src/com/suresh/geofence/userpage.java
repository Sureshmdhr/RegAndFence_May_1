package com.suresh.geofence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.suresh.extras.GPSTracker;
import com.suresh.extras.SessionManager;
import com.suresh.extras.mymapview;
import com.suresh.form.R;
import com.suresh.menus.BaseActivity;
import com.suresh.network.Receiver;

@SuppressLint("NewApi")
public class userpage extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener
{
	SessionManager session;
	private String uname;
	public static ArrayList<String> fencename=new ArrayList<String>();
	public static ArrayList<String> status_db=new ArrayList<String>();
	public static ArrayList<Boolean> status=new ArrayList<Boolean>();
	public static ArrayList<String> type,lat,lgt,rad,catagory,shape,message=new ArrayList<String>();
	public static ArrayList<String> admin_fencename,admin_type,admin_lat,admin_lgt,admin_rad,admin_catagory,admin_shape,admin_message=new ArrayList<String>();
	Button create,view,report,myfences;
	mymapview mv;
	private ImageView icon;
	List<HashMap<String,Object>> aList = new ArrayList<HashMap<String,Object>>();
	private SimpleAdapter adapter;
	HashMap<String, Object> hm;
	private Getdata mydata;
	private ArrayList<String> report_lat;
	private ArrayList<String> report_lgt;
	private ArrayList<String> report_id;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		getLayoutInflater().inflate(R.layout.userpage, frameLayout);

		mDrawerList.setItemChecked(position, true);
		
		setTitle(navMenuTitles[position]);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

		session = new SessionManager(getApplicationContext());
		HashMap<String, String> user = session.getUserDetails();
	    uname = user.get(SessionManager.KEY_EMAIL);
        ListView list = (ListView)findViewById(R.id.lvGeofence);
        mydata = new Getdata(uname,this);
        
	    if(new GPSTracker(this).haveNetworkConnection())
	    {
	    	if(mydata.checkServer())
	    	{
	    	String all_data=mydata.getData();
	        fencename=mydata.convertStringtoArrayList(all_data,"name");
		    status_db=mydata.convertStringtoArrayList(all_data,"status");
		    type=mydata.convertStringtoArrayList(all_data,"type");
		    lat=mydata.convertStringtoArrayList(all_data,"lat");
		    lgt=mydata.convertStringtoArrayList(all_data,"lgt");
		    rad=mydata.convertStringtoArrayList(all_data,"radius");
		    message=mydata.convertStringtoArrayList(all_data,"message");
		    catagory=mydata.convertStringtoArrayList(all_data,"catagory");
		    shape=mydata.convertStringtoArrayList(all_data,"shape");
		    
		    String all_admin_data=mydata.getAdminData();
		    admin_fencename=mydata.convertStringtoArrayList(all_admin_data,"name");
		    admin_type=mydata.convertStringtoArrayList(all_admin_data,"type");
		    admin_lat=mydata.convertStringtoArrayList(all_admin_data,"lat");
		    admin_lgt=mydata.convertStringtoArrayList(all_admin_data,"lgt");
		    admin_rad=mydata.convertStringtoArrayList(all_admin_data,"radius");
		    admin_message=mydata.convertStringtoArrayList(all_admin_data,"message");
		    admin_catagory=mydata.convertStringtoArrayList(all_admin_data,"catagory");
		    admin_shape=mydata.convertStringtoArrayList(all_admin_data,"shape");
		    
	    	for(int i=0;i<(status_db.size());i++)
	    	{
	    		if (status_db.get(i).equals("1"))
	    		{
		    		status.add(true);
		    	}
		    	else
		    	{
		    		status.add(false);
		    	}
	    	}
	    	
	
			for(int i=0;i<fencename.size();i++)
			{
	            hm = new HashMap<String,Object>();
	            hm.put("txt", fencename.get(i));
	            hm.put("stat",status.get(i));
	            aList.add(hm);
	        }
	        String[] from = {"txt","stat" };
	        int[] to = { R.id.fencename, R.id.fencestatus};
	
	        adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.geofence_list_child, from, to)
	        {
	        	@SuppressLint("ResourceAsColor")
				@Override
	        	public View getView(int position, View convertView, ViewGroup parent)
	        	{
	        		final View row =  super.getView(position, convertView, parent);	
	            	if (position % 2 == 0) 
	            	{
	            		row.setBackgroundResource(R.layout.list_background);
	                }
	            	else 
	                {
	            		row.setBackgroundResource(R.layout.list_background_alternative);
	                }
	        		icon=(ImageView) row.findViewById(R.id.imageView1);
	        		
	        		if(type.get(position).equals("0"))
	        		{
	        			icon.setImageResource(R.drawable.circle_icon);
	        		}
	        		else
	        		{
	        			icon.setImageResource(R.drawable.polygon_icon);
	        		}
	
	        		View edit = row.findViewById(R.id.editfence);
	        		edit.setTag(position);
	        		edit.setOnClickListener(userpage.this);
	        		ToggleButton changeStatus = (ToggleButton) row.findViewById(R.id.fencestatus);
	        		changeStatus.setTag(position);
	        		changeStatus.setOnClickListener(userpage.this);
	        		View delete = row.findViewById(R.id.fence_delete);
	        		delete.setTag(position);
	        		delete.setOnClickListener(userpage.this);
	        		return row;
	        	}
	        	
	        };
	            
	        list.setAdapter(adapter);
	        list.setOnItemClickListener(this);
	        
	        create=(Button)findViewById(R.id.createfence);
	        create.setOnClickListener(new OnClickListener()
	        {
				public void onClick(View arg0) 
				{
					Intent intent=new Intent(getApplicationContext(),polygon.class);
					startActivity(intent);
				}
			});
	        
	        myfences=(Button)findViewById(R.id.myfences);
	        myfences.setOnClickListener(new OnClickListener()
	        {
				public void onClick(View arg0) 
				{
					Intent intent=new Intent(getApplicationContext(),myfence.class);
					startActivity(intent);
				}
			});
	        
	        
	        
	        
/*	        Receiver_Get connect1=new Receiver_Get();
	        connect1.setHost("http://116.90.239.21/girc/dmis/api/rapid_assessment/report-items?_format=json");
	        connect1.setPath("");
	        AsyncTask<Void, Void, String> result = connect1.execute(new Void[0]);
	        try {
				String output = result.get();
				report_lat=convertStringtoArrayList(output, "latitude");
				report_lgt=convertStringtoArrayList(output, "longitude");
				report_id=convertStringtoArrayList(output, "id");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/	    }
	    	else
	    	{
	    		mydata.showerrordialog();
	    	}
	    }
	    else
	    {
	    	openNetworkConnectionDialog();
	    }
	    }
	
	public ArrayList<String> convertStringtoArrayList(String result,String column) 
	{
	    ArrayList<String> name = new ArrayList<String>();
	    try 
	    {
	    	JSONArray myjArray = new JSONArray(result);
	    	JSONObject myjson=null;
	    	for(int i=0; i<myjArray.length();i++){
	    		myjson = (JSONObject)myjArray.getJSONObject(i);
	    		name.add(myjson.getString(column));
	    	}
	    } 
	    catch (Exception e) 
	    {
	    	Log.e("log_tag11", "Error Parsing Data "+e.toString());
	    }
	    return name;
	}

	public void openNetworkConnectionDialog() 
	{
	    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	    alertDialog.setTitle("No Internet Enabled");
	    alertDialog.setMessage("Do you want to go to settings menu?");
	    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	    {
	        public void onClick(DialogInterface dialog, int which) 
	        {
	        	dialog.cancel();
	        	finish();
	        }
	    });

	    alertDialog.setPositiveButton("Wi-Fi", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog,int which) {
	            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
	            startActivity(intent);
	        }
	    });
	    
	    alertDialog.setNeutralButton("Mobile Data", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog,int which) {
	        	Intent intent = new Intent();
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	intent.setAction(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
	            startActivity(intent);
	        }
	    });
	    
	    alertDialog.show();
	}
	





	protected void ChangeStatusPost(String string, String status2) {
		Receiver connect=new Receiver(userpage.this);
		connect.setPath("geofencestatus.php");
		connect.addNameValuePairs("value1", string);
		connect.addNameValuePairs("value2", status2);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try {
			output.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
       int position=(Integer)v.getTag();
       switch(v.getId()) {
        case R.id.editfence:
        	Intent intent=new Intent(getApplicationContext(), updategeofence.class);
    		intent.putExtra("fencename",fencename.get(position));
    		intent.putExtra("message",message.get(position));
    		intent.putExtra("type",type.get(position));
    		intent.putExtra("shape",shape.get(position));
    		intent.putExtra("lat",lat.get(position));
    		intent.putExtra("lgt",lgt.get(position));
    		intent.putExtra("radius",rad.get(position));
    		intent.putExtra("catagory",catagory.get(position));
    		intent.putExtra("status", status);
    		startActivity(intent);
        	break;
        case R.id.fence_delete:
        	conformdeletedialog(position);
        	break;
        case R.id.fencestatus:
       	
		toaster(position+"");
            ToggleButton tgl=(ToggleButton)v;
			String Status;
			if(tgl.isChecked()){
                Status="1";
                status_db.set(position, "0");
                status.set(position,false);
            }else{
				Status="0";
                status_db.set(position, "1");
				status.set(position,true);
            }
			ChangeStatusPost(fencename.get(position),Status);
            break;
        default:
            break;
        }
		
	}

	boolean choosed=false;
	private boolean conformdeletedialog(final int position) 
	{
  		AlertDialog.Builder builder = new AlertDialog.Builder(userpage.this);
  		builder.setMessage(R.string.confirmation_message)
  		.setTitle(R.string.confirmation_header).setIcon(R.drawable.delete);
  		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
  			public void onClick(DialogInterface dialog, int which) {
  				dialog.cancel();
  				choosed=false;
  			}
  		}).setPositiveButton("Yes", new DialogInterface.OnClickListener() 
  		{
  			public void onClick(DialogInterface dialog, int which)
  			{
	        	deletefence(fencename.get(position));
	        	Intent i=new Intent(getApplicationContext(),userpage.class);
	        	startActivity(i);
	        	choosed=true;
  			}
  		});
  		AlertDialog dialog = builder.create();
  		dialog.show();
  		return choosed;
	}		


	private void deletefence(String fencename)
	{
		Receiver connect=new Receiver(userpage.this);
		connect.setPath("deletefence.php");
		connect.addNameValuePairs("value1", fencename);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try {
			output.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adview, View view, int position, long arg3)
	{
		
	}
	
	@SuppressLint("ShowToast")
	private void toaster(String result) {
		Toast.makeText(getApplicationContext(), result, 1500).show();
	}


	public String postmyLocation(String lat, String lgt)
	{
		Receiver connect=new Receiver(userpage.this);
		connect.setPath("postlocation.php");
		connect.addNameValuePairs("value1", lat);
		connect.addNameValuePairs("value2",lgt);
		connect.addNameValuePairs("value3", uname);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		String result=null;
		try 
		{
			result=output.get();
			return result;
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		return result;
	}
}