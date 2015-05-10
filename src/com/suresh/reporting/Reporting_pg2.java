package com.suresh.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.suresh.extras.GPSTracker;
import com.suresh.extras.GridAdapter;
import com.suresh.form.R;
import com.suresh.geofence.AllNotification;

public class Reporting_pg2 extends Activity
{
	private GridView impact_gridview;
	private GridAdapter adapter;
	private ImageButton img_next;
	private ImageButton img_prev;
	private TextView skip;
	private AlertDialog alertDialog;
	private ArrayList<Integer> positions=new ArrayList<Integer>();
	private JSONArray impact_Array=new JSONArray();
	private String photo_name;
	private String parent_name;
	private ArrayList<String> impact_array;
	private String latitude;
	private String longitude;
	private ListView my_selected_list;
	private ArrayList<String> list_array=new ArrayList<String>();
	List<HashMap<String,Object>> aList = new ArrayList<HashMap<String,Object>>();
	private HashMap<String, Object> hm;
	private SimpleAdapter list_adapter;
	private String user_id;
	private JSONObject quetionMark;
	private Iterator keys;
	private String disaster_event;
	public static JSONObject reporting;
	
	public ArrayList<String> impact_names=new ArrayList<String>();
	public ArrayList<String> impact_counts=new ArrayList<String>();
	private ArrayList<String> impact_ids=new ArrayList<String>();
	
	@SuppressLint("InflateParams")
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.reporting_pg2);
		super.onCreate(savedInstanceState);
		
		Bundle extra = getIntent().getExtras();
		
		try 
		{
			reporting=new JSONObject(extra.getString("reporting"));
			parent_name=reporting.getJSONObject("ReportItemIncident").getString("item_name");
			latitude=reporting.getJSONObject("ReportItemIncident").getString("latitude");
			longitude=reporting.getJSONObject("ReportItemIncident").getString("longitude");
			user_id=reporting.getJSONObject("ReportItemIncident").getString("user_id");
			disaster_event=reporting.getJSONObject("ReportItemIncident").getString("event_name");
		}
		catch (JSONException e1) 
		{
			e1.printStackTrace();
		}
		TextView tv_eventtitle=(TextView)findViewById(R.id.event_title);
		tv_eventtitle.setText(Reporting_pg1.disaster_incident);


		impact_gridview=(GridView)findViewById(R.id.impact_gridView);
		FileCache fileCache = new FileCache(Reporting_pg2.this);
		impact_array = fileCache.getFromFile(parent_name,"incident","impact");
		impact_array.add("ADD NEW");
			
		Log.i("reporting", reporting.toString());
		adapter=new GridAdapter(Reporting_pg2.this,impact_array);
		if(reporting.has("ReportItemImpact"))
		{
			Log.i("Impact", "yes");
			try {
				quetionMark=reporting.getJSONObject("ReportItemImpact");
				keys=quetionMark.keys();
				while(keys.hasNext())
				{
				    String currentDynamicKey = (String)keys.next();
				    int old_pos = Integer.valueOf(currentDynamicKey);
				    JSONObject currentDynamicValue = quetionMark.getJSONObject(currentDynamicKey);
				    
				    impact_ids.add(currentDynamicKey);
				    impact_names.add(currentDynamicValue.getString("item_name"));
				    impact_counts.add(currentDynamicValue.getString("magnitude"));
				
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			quetionMark=new JSONObject();
		}
		impact_gridview.setAdapter(adapter);
		impact_gridview.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, final int pos,
					long arg3) 
			{
				if(pos+1==impact_array.size())
				{
				    AlertDialog.Builder builder = new AlertDialog.Builder(Reporting_pg2.this);
				    LayoutInflater inflater = Reporting_pg2.this.getLayoutInflater();
				    final View view = inflater.inflate(R.layout.add_item, null);
				    builder.setView(view);
				    builder.setTitle("Add New Impact");
				    builder.setPositiveButton("OK", null);
				    alertDialog = builder.create();
				    alertDialog.setOnShowListener(new OnShowListener() 
				    {
						public void onShow(final DialogInterface dialog)
						{
							Button ok=((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
							ok.setOnClickListener(new OnClickListener()
							{
								public void onClick(View arg0) 
								{
									EditText new_item=(EditText)view.findViewById(R.id.new_item);
									String new_item_string=new_item.getText().toString();
								    LayoutInflater inflater = Reporting_pg2.this.getLayoutInflater();
								    final View view1 = inflater.inflate(R.layout.impact_choose, null);
									EditText et_count = (EditText)view1.findViewById(R.id.count);
									if(new_item_string.equals(""))
										Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_SHORT).show();
									else
									{
										new FileCache(Reporting_pg2.this).addImpacttoFile(new_item_string,parent_name,"incident","impact");
										impact_array.remove(pos);
										impact_array.add(new_item_string);
										impact_array.add("ADD NEW");
										adapter=new GridAdapter(Reporting_pg2.this, impact_array);
										adapter.notifyDataSetChanged();
										impact_gridview.setAdapter(adapter);
										dialog.dismiss();
									}
								}
							});
						}
				    });
				    alertDialog.show();
				}
				else
				{

			    AlertDialog.Builder builder = new AlertDialog.Builder(Reporting_pg2.this);
			    LayoutInflater inflater = Reporting_pg2.this.getLayoutInflater();
			    final View view = inflater.inflate(R.layout.impact_choose, null);
			    builder.setView(view);
			    builder.setTitle("Information: "+adapter.getItem(pos));
			    builder.setPositiveButton("OK", null);
			    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			    {
			        public void onClick(DialogInterface dialog, int whichButton) 
			        {
			        	dialog.cancel();
			        }
			    });
			    alertDialog = builder.create();
			    alertDialog.setOnShowListener(new OnShowListener() 
			    {
					
					@Override
					public void onShow(final DialogInterface dialog)
					{
						Button ok=((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
						ok.setOnClickListener(new OnClickListener()
						{
							public void onClick(View arg0) 
							{
								EditText et_count = (EditText)view.findViewById(R.id.count);
								String count = et_count.getText().toString();

								if(count.equals(""))
								{
									Toast.makeText(getApplicationContext(), "Empty Fields", Toast.LENGTH_SHORT).show();
								}
								else
								{
									if(impact_ids.contains(String.valueOf(pos)))
									{
										for(int j=0;j<impact_ids.size();j++)
										{
											if(impact_ids.get(j).equals(String.valueOf(pos)))
											{
												impact_names.set(j, adapter.getItem(pos));
												impact_counts.set(j, count);
												break;
											}
										}
									}
									else
									{
										impact_names.add(adapter.getItem(pos));
										impact_ids.add(String.valueOf(pos));
										impact_counts.add(count);
									}
/*									adapter.setKeyvalue(pos, "count", count);
									adapter.setKeyvalue(pos,"name",adapter.getItem(pos));
									if(!positions.contains(pos))
										positions.add(pos);
*/									dialog.dismiss();
									
									aList.clear();
									for(int i=0;i<impact_ids.size();i++)
									{
										
										Log.i("position", impact_ids.get(i)+"");
										Log.i("name", impact_names.get(i));
										Log.i("count", impact_counts.get(i));
										/*										
							            hm.put("title", impacts.get(1).getValue());
							            hm.put("message","\tCount:"+impacts.get(0).getValue());
							            aList.add(hm);
*/								        hm = new HashMap<String,Object>();
										hm.put("title", impact_names.get(i));
										hm.put("message", "\tCount:"+impact_counts.get(i));
										aList.add(hm);
										}
									
									list_adapter.notifyDataSetChanged();

								}
							}
						});
					}
				});

			    alertDialog.show();
			}
			}				
		});
		
		my_selected_list=(ListView)findViewById(R.id.my_selected_list);
		aList.clear();
		for(int i=0;i<impact_ids.size();i++)
		{
/*										Log.i("position", positions.get(i)+"");
			List<NameValuePair> impacts = adapter.getKeyvalue(positions.get(i));
			Log.i("name", impacts.get(1).getValue());
			Log.i("count", impacts.get(0).getValue());
            hm.put("title", impacts.get(1).getValue());
            hm.put("message","\tCount:"+impacts.get(0).getValue());
            aList.add(hm);
*/							        
			hm = new HashMap<String,Object>();
			hm.put("title", impact_names.get(i));
			hm.put("message", "\tCount:"+impact_counts.get(i));
			aList.add(hm);
			}
        String[] from = {"title","message"};
        int[] to = { R.id.repo_title, R.id.repo_message};

        list_adapter = new SimpleAdapter(this, aList, R.layout.report_noti_sublist, from, to);
   		my_selected_list.setAdapter(list_adapter);
		
		img_next=(ImageButton)findViewById(R.id.imageButton2);
		img_next.setOnClickListener(new OnClickListener()
		{

			private JSONObject impact_Object=null;

			public void onClick(View arg0) 
			{
					 impact_Array=new JSONArray();
				 	 impact_Object=new JSONObject();
				 	 for(int i=0;i<impact_ids.size();i++)
					{
						JSONObject jsonObject=new JSONObject();
						try 
						{
							jsonObject.put("item_name", impact_names.get(i));
							jsonObject.put("magnitude", impact_counts.get(i));
							jsonObject.put("timestamp_occurance",new FileCache(Reporting_pg2.this).getDate());
							jsonObject.put("latitude", latitude);
							jsonObject.put("event_name", disaster_event);
							jsonObject.put("longitude",longitude);
							jsonObject.put("user_id",user_id);
							impact_Object.put(impact_ids.get(i),jsonObject);
							impact_Array.put(jsonObject);
						}
						catch (JSONException e) 
						{
							e.printStackTrace();
						}
					}
				try 
				{
					reporting.put("ReportItemImpact",impact_Object);
				}
				catch (JSONException e) 
				{
					e.printStackTrace();
				}

				Log.i("to_rep3", reporting.toString());
				Intent intent=new Intent(getApplicationContext(), Reporting_pg3.class);
				intent.putExtra("reporting", reporting.toString());
				startActivity(intent);
				finish();
			}
		});
		img_prev=(ImageButton)findViewById(R.id.rep_upload);
		img_prev.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View arg0)
			{
				Intent intent=new Intent(getApplicationContext(), Reporting_pg1.class);
				intent.putExtra("reporting", reporting.toString());
				startActivity(intent);
				finish();
			}
		});
		skip=(TextView)findViewById(R.id.skip);
		skip.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				if(reporting.has("ReportItemImpact"))
				{
					reporting.remove("ReportItemImpact");
				}
				Intent intent=new Intent(getApplicationContext(), Reporting_pg3.class);
				intent.putExtra("reporting", reporting.toString());
				startActivity(intent);
				finish();
			}
		});
	}
	
	public void onBackPressed() 
	{
		try
		{
			JSONObject photo=reporting.getJSONObject("Photo");
			if(photo.getBoolean("photo_taken"))
			{
				photo_name=photo.getString("name");
				new FileCache(Reporting_pg2.this).deletefile(photo_name);
				Log.i("Photo_deleted", "yes");
			}
			else
			{
				Log.i("Photo", "no");
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		finish();
		Intent intent=new Intent(getApplicationContext(), Reporting_pg1.class);
		startActivity(intent);
	}

}
