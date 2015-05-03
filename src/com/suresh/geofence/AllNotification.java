package com.suresh.geofence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.suresh.form.R;
import com.suresh.menus.BaseActivity;
import com.suresh.reporting.FileCache;

public class AllNotification extends BaseActivity 
{
	private Button uploadall;
	private Button add;
	private FileCache fileCache;
	private JSONArray notesJSONArray;
	private List<String> fence_names=new ArrayList<String>();
	private ListView lv;
	private SimpleAdapter adapter;
	private HashMap<String, Object> hm;
	private ArrayList<String> fence_message=new ArrayList<String>();
	private ArrayList<String> fence_date=new ArrayList<String>();
	List<HashMap<String,Object>> aList = new ArrayList<HashMap<String,Object>>();
	private ImageButton delete_all;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		getLayoutInflater().inflate(R.layout.all_notification, frameLayout);
		mDrawerList.setItemChecked(position, true);
		setTitle(navMenuTitles[position]);
		
		
		fileCache=new FileCache(this);
		notesJSONArray=fileCache.getNotifications();
		Log.i("noti", notesJSONArray.toString());
 		for(int i=0;i<notesJSONArray.length();i++)
		{
			try
			{
				fence_names.add(notesJSONArray.getJSONObject(i).getString("title"));
				fence_date.add(notesJSONArray.getJSONObject(i).getString("date"));
				fence_message.add(notesJSONArray.getJSONObject(i).getString("message"));
				notesJSONArray.getJSONObject(i).put("opened", true);
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		new FileCache(this).replacenotification(notesJSONArray);
		lv=(ListView)findViewById(R.id.report_listview);
		
		for(int i=0;i<fence_names.size();i++)
		{
            hm = new HashMap<String,Object>();
            hm.put("title", fence_names.get(i));
            hm.put("message",fence_message.get(i));
            hm.put("date","Date: "+fence_date.get(i));
            aList.add(hm);
        }
        String[] from = {"title","message","date" };
        int[] to = { R.id.noti_title, R.id.noti_message,R.id.noti_date};

        adapter = new SimpleAdapter(this, aList, R.layout.all_noti_sublist, from, to)
        {
			public View getView(final int position, View convertView, ViewGroup parent)
			{
        		final View row =  super.getView(position, convertView, parent);	
        		
        		View delete = row.findViewById(R.id.noti_delete);
        		delete.setTag(position);
        		delete.setOnClickListener(new OnClickListener() 
        		{
					public void onClick(View arg0) 
					{
						new FileCache(AllNotification.this).deletenotification(fence_names.get(position),fence_message.get(position));
						aList.remove(position);
						adapter.notifyDataSetChanged();
					}
        		});
        		return row;
			}
        };
   		lv.setAdapter(adapter);
   		delete_all=(ImageButton)findViewById(R.id.delete_all);
   		delete_all.setOnClickListener(new OnClickListener()
   		{
			public void onClick(View arg0)
			{
				if(aList.size()>0)
				{
					conformdeletedialog();
				}
			}
		});

	}
	
	private void conformdeletedialog() 
	{
  		AlertDialog.Builder builder = new AlertDialog.Builder(AllNotification.this);
  		builder.setMessage(R.string.confirmation_message)
  		.setTitle("Are You Sure You Want To Delete All Notifications");
  		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
  			public void onClick(DialogInterface dialog, int which) {
  				dialog.cancel();
  			}
  		}).setPositiveButton("Yes", new DialogInterface.OnClickListener() 
  		{
  			public void onClick(DialogInterface dialog, int which)
  			{
  				new FileCache(getApplicationContext()).deleteallnotification();
  				aList.clear();
  				adapter.notifyDataSetChanged();
  			}
  		});
  		AlertDialog dialog = builder.create();
  		dialog.show();
	}		

}
