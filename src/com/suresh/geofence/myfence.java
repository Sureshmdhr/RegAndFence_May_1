package com.suresh.geofence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

import com.suresh.extras.ExpandableListAdapter;
import com.suresh.extras.SessionManager;
import com.suresh.form.R;
import com.suresh.menus.UserMenuActivity;
import com.suresh.network.Receiver;

public class myfence extends UserMenuActivity 
{
	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;
	HashMap<String, List<String>> listChildDate;
	public ArrayList<String> my_fences;
	private int pos;
	public int previousItem;
	private SessionManager session;
	private String uname;

	protected void onCreate(Bundle savedInstanceState) 
	{	    
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.myfence);
        session = new SessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        uname= user.get(SessionManager.KEY_EMAIL);
        my_fences=getMyData("name");
        Bundle extras = getIntent().getExtras();
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
 
        // preparing list data
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, listChildDate);
 
        // setting list adapter
        expListView.setAdapter(listAdapter);
        if(extras!=null)
        {    
            pos=extras.getInt("pos");
            expListView.expandGroup(pos);
		    previousItem = -1;
		    if(pos != previousItem )
		    {
		    	expListView.collapseGroup(previousItem );
		        previousItem = pos;
		    }

        }
		// Listview Group click listener
		expListView.setOnGroupClickListener(new OnGroupClickListener() 
		{
			public boolean onGroupClick(ExpandableListView parent, View v,int groupPosition, long id) 
			{
				return false;
			}
		});

		// Listview Group expanded listener
		expListView.setOnGroupExpandListener(new OnGroupExpandListener() 
		{
		    public void onGroupExpand(int groupPosition) 
		    {
		        if(groupPosition != previousItem )
		        	expListView.collapseGroup(previousItem );
		        previousItem = groupPosition;
		    }
		});
		// Listview Group collasped listener
		expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() 
		{
			public void onGroupCollapse(int groupPosition) 
			{

			}
		});

		// Listview on child click listener
		expListView.setOnChildClickListener(new OnChildClickListener() 
		{
			public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) 
			{
				return false;
			}
		});
	}
	
	private ArrayList<String> getMyData(String string) {
		Receiver connect = new Receiver(this);
		connect.setPath("activegeofence.php");
		connect.addNameValuePairs("value1", uname);
		AsyncTask<Void, Void, String> output=connect.execute(new Void[0]);
	    String result=null;
	    try 
	    {
			result=output.get();
		}
	    catch (InterruptedException e1) 
	    {
	    	e1.printStackTrace();
		} 
	    catch (ExecutionException e1) 
		{
			e1.printStackTrace();
		}
	    ArrayList<String> name = new ArrayList<String>();
	    name=convertStringtoArrayList(result,string);
	  return name;
	}

	private ArrayList<String> convertStringtoArrayList(String result,String string) 
	{
	    ArrayList<String> name = new ArrayList<String>();
	    try 
	    {
	    	JSONArray myjArray = new JSONArray(result);
	    	JSONObject myjson=null;
	    	for(int i=0; i<myjArray.length();i++){
	    		myjson = (JSONObject)myjArray.getJSONObject(i);
	    		name.add(myjson.getString(string));
	    	}
	    } 
	    catch (Exception e) 
	    {
	    	Log.e("log_tag", "Error Parsing Data "+e.toString());
	    }
	    return name;
	}

	@SuppressWarnings("unused")
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
	{
		  ExpandableListView listView = (ExpandableListView) parent;
		  long pos = listView.getExpandableListPosition(position);

		  // get type and correct positions
		  int itemType = ExpandableListView.getPackedPositionType(pos);
		  int groupPos = ExpandableListView.getPackedPositionGroup(pos);
		  int childPos = ExpandableListView.getPackedPositionChild(pos);

		  // if child is long-clicked
		  if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) 
		  {
			  Toast.makeText(getApplicationContext(), "click Long", Toast.LENGTH_LONG).show();
		  }
		  return false;
	}
    /*
     * Preparing the list data
     */
    private void prepareListData() 
    {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listChildDate = new HashMap<String, List<String>>();
        ArrayList<ArrayList<String>> fence_data = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> fence_date = new ArrayList<ArrayList<String>>();
        // Adding child data
        for(int i=0;i<my_fences.size();i++)
        {
        	listDataHeader.add(my_fences.get(i));
        	ArrayList<String> intersects=new ArrayList<String>();
        	ArrayList<String> getDate=new ArrayList<String>();
        	intersects=checkinsidepolygon(my_fences.get(i),"name");
        	getDate=checkinsidepolygon(my_fences.get(i),"mydate");
        	fence_data.add(intersects);
        	fence_date.add(getDate);
        	for(int j=0;j<fence_date.size();j++)
        	{
        		//Log.i("check "+j,getDate.get(j));
        	}
        }
        // Adding child data
        for(int i=0;i<listDataHeader.size();i++)
        {
        	listDataChild.put(listDataHeader.get(i), fence_data.get(i));
        	listChildDate.put(listDataHeader.get(i), fence_date.get(i));
        }
    }
	
	private ArrayList<String> checkinsidepolygon(String string,String row)
	{
		Receiver connect = new Receiver(this);
		connect.setPath("check_my_polygon_inside_polygon.php");
		connect.addNameValuePairs("value1", string);
		AsyncTask<Void, Void, String> output=connect.execute(new Void[0]);
	    String result=null;
	    try 
	    {
			result=output.get();
		}
	    catch (InterruptedException e1) 
	    {
	    	e1.printStackTrace();
		} 
	    catch (ExecutionException e1) 
		{
			e1.printStackTrace();
		}
	    ArrayList<String> name = new ArrayList<String>();
	    name=convertStringtoArrayList(result,row);	
	    return name;
	}
}

