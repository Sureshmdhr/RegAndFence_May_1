package com.suresh.reporting;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

import com.suresh.extras.GridAdapter;
import com.suresh.form.R;

public class Reporting_pg3 extends Activity
{
	private GridView need_gridview;
	private GridAdapter adapter;
	private ImageButton img_next;
	private ImageButton img_prev;
	private TextView skip,need_title;
	private AlertDialog alertDialog;
	protected JSONArray need_Array;
	private String parent_name;
	private ArrayList<String> impact_array=new ArrayList<String>();
	List<HashMap<String,Object>> aList = new ArrayList<HashMap<String,Object>>();
	private String latitude;
	private String longitude;
	private ListView my_selected_list;
	private SimpleAdapter list_adapter;
	public static JSONObject reporting;
	private HashMap<String, Object> hm;
	private String user_id;
	private String disaster_event;
	public ArrayList<String> impact_names=new ArrayList<String>();
	public ArrayList<String> impact_counts=new ArrayList<String>();
	private ArrayList<String> impact_ids=new ArrayList<String>();
	private ArrayList<String> impact_supplied=new ArrayList<String>();

	@SuppressLint("InflateParams")
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.reporting_pg2);
		super.onCreate(savedInstanceState);
		need_title=(TextView)findViewById(R.id.tv1);
		need_title.setText("Needs");
		
		Bundle extra = getIntent().getExtras();
		
		try 
		{
			reporting=new JSONObject(extra.getString("reporting"));
			parent_name=reporting.getJSONObject("ReportItemIncident").getString("item_name");
			latitude=reporting.getJSONObject("ReportItemIncident").getString("latitude");
			longitude=reporting.getJSONObject("ReportItemIncident").getString("longitude");
			user_id=reporting.getJSONObject("ReportItemIncident").getString("user_id");
			disaster_event=reporting.getJSONObject("ReportItemIncident").getString("event");
		}
		catch (JSONException e1) 
		{
			e1.printStackTrace();
		}

		TextView tv_eventtitle=(TextView)findViewById(R.id.event_title);
		tv_eventtitle.setText(Reporting_pg1.disaster_incident);
		
		need_gridview=(GridView)findViewById(R.id.impact_gridView);
		FileCache fileCache = new FileCache(Reporting_pg3.this);
		impact_array = fileCache.getNeedFromFile("need");
		impact_array.add("ADD NEW");
		for(int i=0;i<impact_array.size();i++)
		{
			Log.i("need", impact_array.get(i));
		}
		adapter=new GridAdapter(Reporting_pg3.this,impact_array);
		if(reporting.has("ReportItemNeed"))
		{
			Log.i("Impact", "yes");
			try {
				JSONObject quetionMark = reporting.getJSONObject("ReportItemNeed");
				Iterator keys = quetionMark.keys();
				while(keys.hasNext())
				{
				    String currentDynamicKey = (String)keys.next();
				    int old_pos = Integer.valueOf(currentDynamicKey);
				    JSONObject currentDynamicValue = quetionMark.getJSONObject(currentDynamicKey);
				    impact_ids.add(currentDynamicKey);
				    impact_names.add(currentDynamicValue.getString("item_name"));
				    impact_counts.add(currentDynamicValue.getString("magnitude"));
				    impact_supplied.add(currentDynamicValue.getString("supplied_per_person"));
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		need_gridview.setAdapter(adapter);
		need_gridview.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, final int pos,
					long arg3) 
			{
				if(pos+1==impact_array.size())
				{
				    AlertDialog.Builder builder = new AlertDialog.Builder(Reporting_pg3.this);
				    LayoutInflater inflater = Reporting_pg3.this.getLayoutInflater();
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
								    LayoutInflater inflater = Reporting_pg3.this.getLayoutInflater();
								    final View view1 = inflater.inflate(R.layout.need_choose, null);
									EditText et_count = (EditText)view1.findViewById(R.id.count);
									EditText et_supplied =(EditText)view1.findViewById(R.id.supplied);
								    if(new_item_string.equals(""))
										Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_SHORT).show();
									else
									{
										new FileCache(Reporting_pg3.this).addNeedtoFile(new_item_string,parent_name,"incident","need");
										impact_array.remove(pos);
										impact_array.add(new_item_string);
										impact_array.add("ADD NEW");
										adapter=new GridAdapter(Reporting_pg3.this, impact_array);
										adapter.notifyDataSetChanged();
										need_gridview.setAdapter(adapter);
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
				    AlertDialog.Builder builder = new AlertDialog.Builder(Reporting_pg3.this);
	
				    LayoutInflater inflater = Reporting_pg3.this.getLayoutInflater();
				    final View view = inflater.inflate(R.layout.need_choose, null);
				    builder.setView(view);
	
				    builder.setTitle("Information: "+adapter.getItem(pos));
				   // builder.setMessage("");
	
				    builder.setPositiveButton("OK", null);
				    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
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
									EditText et_supplied =(EditText)view.findViewById(R.id.supplied);
									String count = et_count.getText().toString();
									String supplied = et_supplied.getText().toString();
									if(count.equals("")&&supplied.equals(""))
									{
										Toast.makeText(getApplicationContext(), "Empty Fields", Toast.LENGTH_SHORT).show();
									}
									else
									{
										if(count.equals(""))
											count="0";
										else if(supplied.equals(""))
											supplied="0";

										if(impact_ids.contains(String.valueOf(pos)))
										{
											for(int j=0;j<impact_ids.size();j++)
											{
												if(impact_ids.get(j).equals(String.valueOf(pos)))
												{
													impact_names.set(j, adapter.getItem(pos));
													impact_counts.set(j, count);
													impact_supplied.set(j, supplied);
													break;
												}
											}
										}
										else
										{
											impact_names.add(adapter.getItem(pos));
											impact_ids.add(String.valueOf(pos));
											impact_counts.add(count);
											impact_supplied.add(supplied);
										}
										
										dialog.dismiss();
										
										aList.clear();
										for(int i=0;i<impact_ids.size();i++)
										{
											Log.i("needs___", impact_ids.get(i)+"\t"+impact_names.get(i));
											hm = new HashMap<String,Object>();
								            hm.put("title", impact_names.get(i));
								            hm.put("message","\tNeed:For "+impact_counts.get(i)+" Person\n\tSupplied: For "+impact_supplied.get(i)+" Person");
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
			hm = new HashMap<String,Object>();
            hm.put("title", impact_names.get(i));
            hm.put("message","\tNeed:For "+impact_counts.get(i)+" Person\n\tSupplied: For "+impact_supplied.get(i)+" Person");
            aList.add(hm);
        }
		
        String[] from = {"title","message" };
        int[] to = { R.id.repo_title, R.id.repo_message};

        list_adapter = new SimpleAdapter(this, aList, R.layout.report_noti_sublist, from, to);
   		my_selected_list.setAdapter(list_adapter);
		list_adapter.notifyDataSetChanged();

		img_next=(ImageButton)findViewById(R.id.imageButton2);
		img_next.setOnClickListener(new OnClickListener()
		{
			private JSONObject need_Object=null;

			public void onClick(View v) 
			{
			 	 need_Object=new JSONObject();
				for(int i=0;i<impact_ids.size();i++)
				{
					JSONObject jsonObject=new JSONObject();
				
					try 
					{
						jsonObject.put("item_name", impact_names.get(i));
						jsonObject.put("magnitude",impact_counts.get(i));
						jsonObject.put("supplied_per_person", impact_supplied.get(i));
						jsonObject.put("timestamp_occurance",new FileCache(Reporting_pg3.this).getDate());
						jsonObject.put("event", disaster_event);
						jsonObject.put("latitude", latitude);
						jsonObject.put("longitude",longitude);
						jsonObject.put("user_id",user_id);
						need_Object.put(impact_ids.get(i),jsonObject);
					}
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
				}
				try 
				{
					reporting.put("ReportItemNeed",need_Object);
				}
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
				finish();
				Intent intent=new Intent(getApplicationContext(), Reporting_pg4.class);
				FileCache fc=new FileCache(getApplicationContext());
				try 
				{
					fc.storeFile(reporting.toString());
				}
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
					Log.e("error",e.toString());
				}
				catch (IOException e) 
				{
					e.printStackTrace();
					Log.e("error",e.toString());
				}
				intent.putExtra("reporting", reporting.toString());
				startActivity(intent);
				finish();
			}
		});
		img_prev=(ImageButton)findViewById(R.id.rep_upload);
		img_prev.setOnClickListener(new OnClickListener() 
		{
			private JSONObject need_Object=null;

			public void onClick(View arg0)
			{
			 	 need_Object=new JSONObject();
				Log.i("size", impact_ids.size()+"");
			for(int i=0;i<impact_ids.size();i++)
				{
					Log.i("size", impact_ids.size()+"");
					JSONObject jsonObject=new JSONObject();
				
					try 
					{
						jsonObject.put("item_name", impact_names.get(i));
						jsonObject.put("magnitude",impact_counts.get(i));
						jsonObject.put("supplied_per_person", impact_supplied.get(i));
						jsonObject.put("timestamp_occurance",new FileCache(Reporting_pg3.this).getDate());
						jsonObject.put("event", disaster_event);
						jsonObject.put("latitude", latitude);
						jsonObject.put("longitude",longitude);
						jsonObject.put("user_id",user_id);
						Log.i("need111", jsonObject.toString());
						need_Object.put(impact_ids.get(i),jsonObject);
					}
					catch (JSONException e) 
					{
						Log.e("error", e.toString());
						e.printStackTrace();
					}
				}
				try 
				{
					reporting.put("ReportItemNeed",need_Object);
				}
				catch (JSONException e) 
				{
					e.printStackTrace();
				}

				Intent intent=new Intent(getApplicationContext(),Reporting_pg2.class);
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
				if(reporting.has("ReportItemNeed"))
				{
					reporting.remove("ReportItemNeed");
				}
				Intent intent=new Intent(Reporting_pg3.this,Reporting_pg4.class);
				FileCache fc=new FileCache(getApplicationContext());
				try 
				{
					fc.storeFile(reporting.toString());
				}
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
					Log.e("error",e.toString());
				}
				catch (IOException e) 
				{
					e.printStackTrace();
					Log.e("error",e.toString());
				}

				intent.putExtra("reporting", reporting.toString());
				startActivity(intent);
				finish();
			}
		});
	}
	
	public void onBackPressed() 
	{
		JSONObject need_Object = null;
		for(int i=0;i<impact_ids.size();i++)
		{
		 	 need_Object=new JSONObject();
			JSONObject jsonObject=new JSONObject();
		
			try 
			{
				jsonObject.put("item_name", impact_names.get(i));
				jsonObject.put("magnitude",impact_counts.get(i));
				jsonObject.put("supplied_per_person", impact_supplied.get(i));
				jsonObject.put("timestamp_occurance",new FileCache(Reporting_pg3.this).getDate());
				jsonObject.put("event", disaster_event);
				jsonObject.put("latitude", latitude);
				jsonObject.put("longitude",longitude);
				jsonObject.put("user_id",user_id);
				need_Object.put(impact_ids.get(i),jsonObject);
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		try 
		{
			reporting.put("ReportItemNeed",need_Object);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		finish();
		Intent intent=new Intent(getApplicationContext(), Reporting_pg2.class);
		intent.putExtra("reporting", reporting.toString());
		startActivity(intent);
	}
	


}
