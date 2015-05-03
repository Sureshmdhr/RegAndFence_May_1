package com.suresh.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.suresh.extras.SessionManager;
import com.suresh.extras.mymapview;
import com.suresh.form.R;
import com.suresh.geofence.geofences;
import com.suresh.geofence.polygon;
import com.suresh.geofence.updategeofence;
import com.suresh.network.Receiver;

public class GeofencesInList extends AdminMenuActivity implements AdapterView.OnItemClickListener, View.OnClickListener
{
	SessionManager session;
	private String uname;
	private ArrayList<String> fencename=new ArrayList<String>();
	private ArrayList<String> status_db=new ArrayList<String>();
	private ArrayList<Boolean> status=new ArrayList<Boolean>();
	private ArrayList<String> type,lat,lgt,rad,catagory,shape,message=new ArrayList<String>();
	private Button create,view;
	mymapview mv;
	private ImageView icon;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.geofence_listview, frameLayout);

		mDrawerList.setItemChecked(position, true);
		
		setTitle(navMenuTitles[position]);

	    SessionManager session = new SessionManager(getApplicationContext());
		
	    HashMap<String, String> user = session.getUserDetails();
        ListView list = (ListView)findViewById(R.id.lvGeofence);
	    // Session name
	    uname = user.get(SessionManager.KEY_EMAIL);
	    
	    fencename=getData("name");
	    status_db=getData("status");
	    type=getData("type");
	    lat=getData("lat");
	    lgt=getData("lgt");
	    rad=getData("radius");
	    message=getData("message");
	    catagory=getData("catagory");
	    shape=getData("shape");
	    
    	for(int i=0;i<(status_db.size());i++){
    		if (status_db.get(i).equals("1")){
	    		status.add(true);
	    	}
	    	else{
	    		status.add(false);
	    	}
    	}
        List<HashMap<String,Object>> aList = new ArrayList<HashMap<String,Object>>();

		for(int i=0;i<fencename.size();i++){
            HashMap<String, Object> hm = new HashMap<String,Object>();
            hm.put("txt", fencename.get(i));
            hm.put("stat",status.get(i));
            aList.add(hm);
        }
        // Keys used in Hashmap
        String[] from = {"txt","stat" };

        // Ids of views in listview_layout
        int[] to = { R.id.fencename, R.id.fencestatus};

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
	    //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.geofence_list_child, R.id.fencename, fencename) {
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.geofence_list_child, from, to)
        {
        	@SuppressLint("ResourceAsColor")
			@Override
        	public View getView(int position, View convertView, ViewGroup parent)
        	{
        		View row =  super.getView(position, convertView, parent);	
            	if (position % 2 == 0) 
            	{
            		row.setBackgroundResource(R.layout.list_background);
                } else {
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
        		edit.setOnClickListener(GeofencesInList.this);
        		ToggleButton changeStatus = (ToggleButton) row.findViewById(R.id.fencestatus);
        		changeStatus.setTag(position);
        		changeStatus.setOnClickListener(GeofencesInList.this);
        		View delete = row.findViewById(R.id.fence_delete);
        		delete.setTag(position);
        		delete.setOnClickListener(GeofencesInList.this);
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
				//finish();
			}
		});
        
        view=(Button)findViewById(R.id.viewfence);
        view.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				Intent intent=new Intent(getApplicationContext(),geofences.class);
	    		intent.putStringArrayListExtra("fencename",fencename);
	    		intent.putStringArrayListExtra("message",message);
	    		intent.putStringArrayListExtra("type",type);
	    		intent.putStringArrayListExtra("shape",shape);
	    		intent.putStringArrayListExtra("lat",lat);
	    		intent.putStringArrayListExtra("lgt",lgt);
	    		intent.putStringArrayListExtra("radius",rad);
	    		intent.putStringArrayListExtra("catagory",catagory);
	    		intent.putStringArrayListExtra("status", status_db);
				startActivity(intent);
				//finish();
			}
		});
	}
	
	private ArrayList<String> getData(String columnname)
	{
		ArrayList<String> result = null;
		Receiver connect=new Receiver(this);
		connect.setPath("extdb3.php");
		connect.addNameValuePairs("value1", uname);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try 
		{
			 result=convertStringToArrayList(output.get(),columnname);
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
	
	private ArrayList<String> convertStringToArrayList(String string,String columnname)
	{
		   ArrayList<String> name = new ArrayList<String>();
		   try 
		   {
			   JSONArray myjArray = new JSONArray(string);
			   JSONObject myjson=null;
			   for(int i=0; i<myjArray.length();i++)
			   {
				   myjson = (JSONObject)myjArray.getJSONObject(i);
				   name.add(myjson.getString(columnname));
			   }
		   } 
		   catch (Exception e) 
		   {
			   Log.e("log_tag", "Error Parsing Data "+e.toString());
		   }
		   return name;
		}

	protected void ChangeStatusPost(String string, String status2) {
		Receiver connect=new Receiver(this);
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
    		startActivity(intent);
/*        	if(type.get(position).equals("0"))
        	{
        		setValuesInCircleFragment(position);
        	}
        	else
        	{
        		setvaluseInPolygonFragment(position);
        	}
        		
*/        	
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
                status.set(position,false);
            }else{
				Status="0";
				status.set(position,true);
            }
			ChangeStatusPost(fencename.get(position),Status);
            break;
        default:
            break;
        }
		
	}

	
	private void conformdeletedialog(final int position) 
	{
  		AlertDialog.Builder builder = new AlertDialog.Builder(GeofencesInList.this);
  		builder.setMessage(R.string.confirmation_message)
  		.setTitle(R.string.confirmation_header).setIcon(R.drawable.delete);
  		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
  
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				dialog.cancel();
  			}
  		}).setPositiveButton("Yes", new DialogInterface.OnClickListener() 
  		{
  			public void onClick(DialogInterface dialog, int which)
  			{
	        	deletefence(fencename.get(position));
	        	//finish();
	        	Intent i=new Intent(getApplicationContext(),GeofencesInList.class);
	        	startActivity(i);
  			}
  		});
  		AlertDialog dialog = builder.create();
  		dialog.show();	
	}		
	

	private void deletefence(String fencename) {
		Receiver connect=new Receiver(this);
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Toast.makeText(this, "Item Click "+position, Toast.LENGTH_SHORT).show();
	}
	
	@SuppressLint("ShowToast")
	private void toaster(String result) {
		Toast.makeText(getApplicationContext(), result, 1500).show();
	}



}