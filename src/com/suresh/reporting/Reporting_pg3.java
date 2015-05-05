package com.suresh.reporting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.view.ViewGroup;
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

import com.suresh.extras.GPSTracker;
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
	private ArrayList<Integer> positions=new ArrayList<Integer>();
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
			Log.i("rep_2", reporting.toString());
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
									if(positions.contains(pos))
									{
										List<NameValuePair> needs = adapter.getKeyvalue(positions.get(pos));
										et_count.setText(needs.get(0).getValue());
										et_supplied.setText(needs.get(2).getValue());
									}
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
								//	EditText et_describe =(EditText)view.findViewById(R.id.describe);
									String count = et_count.getText().toString();
									String supplied = et_supplied.getText().toString();
									//String unit = et_unit.getText().toString();
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
										adapter.setKeyvalue(pos, "count", count);
										adapter.setKeyvalue(pos,"name",adapter.getItem(pos));
										adapter.setKeyvalue(pos, "supplied", supplied);
									//	adapter.setKeyvalue(pos, "describe", describe);
										if(!positions.contains(pos))
											positions.add(pos);
										dialog.dismiss();
										
										aList.clear();
										for(int i=0;i<positions.size();i++)
										{
											List<NameValuePair> needs = adapter.getKeyvalue(positions.get(i));
											hm = new HashMap<String,Object>();
								            hm.put("title", needs.get(1).getValue());
								            hm.put("message","Need:For "+needs.get(0).getValue()+"Person\nSupplied: For "+needs.get(2).getValue()+"Person");
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
		for(int i=0;i<positions.size();i++)
		{
			List<NameValuePair> needs = adapter.getKeyvalue(positions.get(i));
			hm = new HashMap<String,Object>();
            hm.put("title", needs.get(1).getValue());
            hm.put("message","Need:"+needs.get(0).getValue()+"\nSupplied"+needs.get(2).getValue());
            aList.add(hm);
        }
        String[] from = {"title","message" };
        int[] to = { R.id.noti_title, R.id.noti_message};

        list_adapter = new SimpleAdapter(this, aList, R.layout.all_noti_sublist, from, to)
        {
			public View getView(final int position, View convertView, ViewGroup parent)
			{
        		final View row =  super.getView(position, convertView, parent);	
        		View delete = row.findViewById(R.id.noti_delete);
        		delete.setVisibility(View.GONE);
				return row;
			}
        };
   		my_selected_list.setAdapter(list_adapter);

		img_next=(ImageButton)findViewById(R.id.imageButton2);
		img_next.setOnClickListener(new OnClickListener()
		{
			private JSONObject need_Object;

			public void onClick(View v) 
			{
				if(positions.size()!=0)
					 need_Object=new JSONObject();
				for(int i=0;i<positions.size();i++)
				{
					List<NameValuePair> needs = adapter.getKeyvalue(positions.get(i));
					JSONObject jsonObject=new JSONObject();
				
					try 
					{
						GPSTracker gps=new GPSTracker(Reporting_pg3.this);
						//jsonObject.put("id",i+1);
						jsonObject.put("item_name", needs.get(1).getValue());
						jsonObject.put("magnitude", needs.get(0).getValue());
						jsonObject.put("supplied_per_person", needs.get(1).getValue());
						jsonObject.put("timestamp_occurance",new FileCache(Reporting_pg3.this).getDate());
						jsonObject.put("latitude", latitude);
						jsonObject.put("longitude",longitude);
				//		jsonObject.put("description", needs.get(1).getValue());
						need_Object.put(String.valueOf(i+1),jsonObject);
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
			public void onClick(View arg0)
			{
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
}
