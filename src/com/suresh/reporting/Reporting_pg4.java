package com.suresh.reporting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.suresh.extras.GPSTracker;
import com.suresh.form.R;
import com.suresh.menus.BaseActivity;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Reporting_pg4 extends BaseActivity 
{
	private ListView lvSummry;
	private List<String> file_names,file_header=new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private FileCache filecache;
	private File[] allFiles;
	ArrayList<File> unuploaded_files = new ArrayList<File>();
	private Button upload_all;
	private Button new_report;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.reporting_pg4, frameLayout);

		mDrawerList.setItemChecked(position, true);
		
		setTitle(navMenuTitles[position]);
		
		lvSummry=(ListView)findViewById(R.id.report_listview);
		
		filecache=new FileCache(Reporting_pg4.this);
		allFiles=filecache.getallFiles("txt");
		for(int i=0;i<allFiles.length;i++)
		{
			unuploaded_files.add(allFiles[i]);
		}
		file_names=getallFileNames(unuploaded_files);

		for(int i=0;i<unuploaded_files.size();i++)
		{
			file_header.add(filecache.getFileHeader(unuploaded_files.get(i)));
		}
		adapter=new ArrayAdapter<String>(this,R.layout.reporting_pg4_sublist,R.id.fileheader,file_header)
		{
        	public View getView(final int position, View convertView, ViewGroup parent)
        	{
        		final View row =  super.getView(position, convertView, parent);	
        		
        		View uploads = row.findViewById(R.id.rep_upload);
        		uploads.setTag(position);
        		uploads.setOnClickListener(new OnClickListener() 
        		{
					public void onClick(View arg0) 
					{
						if(new GPSTracker(getApplicationContext()).haveNetworkConnection())
						{
							boolean upload_status=filecache.upload(allFiles[position]);
							if(upload_status)
							{
								String name = file_names.get(position);
								file_header.remove(position);
								file_names.remove(position);
								unuploaded_files.remove(position);
								filecache.deletefile(name);
								adapter.notifyDataSetChanged();
								//row.setAlpha(1);
								Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
							}
							else
							{
								toaster("Upload Failed");
							}
						}
						else
						{
							toaster("No Internet Connection/n Please try again later");
						}
						}

        		});
        		
        		View delete = row.findViewById(R.id.rep_delete);
        		delete.setTag(position);
        		
        		delete.setOnClickListener(new OnClickListener() 
        		{
					public void onClick(View arg0) 
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(Reporting_pg4.this);
						builder.setMessage("Confirm Delete")
							.setTitle("Are you sure you want to delete?");
						builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								String name = file_names.get(position);
								file_header.remove(position);
								file_names.remove(position);
								unuploaded_files.remove(position);
								filecache.deletefile(name);
								adapter.notifyDataSetChanged();
							}
						}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() 	
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								
							}
						});
						AlertDialog dialog = builder.create();
						dialog.show();	
					}
				});
        		return row;
        	}			
		};
		
		lvSummry.setAdapter(adapter);
	//	lvSummry.setOnItemClickListener(this);
		adapter.setNotifyOnChange(true);

		upload_all=(Button)findViewById(R.id.delete_all);
		upload_all.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				if(filecache.uploadall())
				{
					file_header.clear();
					file_names.clear();
					unuploaded_files.clear();
					adapter.notifyDataSetChanged();
				}
				else
				{
					
				}
			}
		});
		
		new_report=(Button)findViewById(R.id.add_report);
		new_report.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Intent intent=new Intent(Reporting_pg4.this, Reporting_pg1.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	public void toaster(String string) 
	{
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
	}

	private List<String> getallFileNames(List<File> unuploaded_files2)
	{
		List<String> file_name=new ArrayList<String>();
		for(int i=0;i<unuploaded_files2.size();i++)
		{
			file_name.add(unuploaded_files2.get(i).getName());
		}
		return file_name;
	}

}
