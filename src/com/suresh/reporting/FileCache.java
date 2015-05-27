package com.suresh.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.suresh.menus.UserMenuActivity;
import com.suresh.network.StringReceiver;

@SuppressLint("NewApi")
public class FileCache extends UserMenuActivity {
	private File cacheDir;
	private String file_name="notification";
	String photoname;
	Context mContext;
	private List<? extends NameValuePair> nameValuePairs;
	public FileCache(Context context){
		   this.mContext=context;
	       if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
	          cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),
	          "girc/.Cache");
	       else
	           cacheDir=context.getCacheDir();
	       if(!cacheDir.exists())
	           cacheDir.mkdirs();
	   }
	   
	   public File getFile(String filename)
	   {
	       File f = new File(cacheDir, filename);
	       return f;
	   }
	   public File getdir()
	   {
		   return cacheDir;
	   }
	   
	   public File[] getallFiles(final String type)
	   {
	       File[] allfiles;
	       File dir = cacheDir;
			FileFilter filter = new FileFilter() 
			{
			    public boolean accept(File file) 
			    {
			        return file.getAbsolutePath().matches(".*\\."+type);
			    }
			};
			allfiles = dir.listFiles(filter);
			return allfiles;
	   }
	   
	   public static String convertStreamToString(InputStream is) throws Exception {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		      sb.append(line).append("\n");
		    }
		    reader.close();
		    return sb.toString();
		}

	   public static String getStringFromFile (String filePath)
		{
		    File fl = new File(filePath);
		    String ret = null;
			try {
				FileInputStream fin = new FileInputStream(fl);
				ret = convertStreamToString(fin);
				//Make sure you close all streams.
				fin.close();
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}        
		    return ret;
		}
	 
		public String uploadphotousingmultipart(File file) throws Exception 
		{
	        String url = StringReceiver.host+"/girc/dmis/api/file_management/upload/file";
	        HttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(url);
	       // post.addHeader("Content-Type", "application/json");
	        MultipartEntity mpEntity = new MultipartEntity();
	        ContentBody cbFile = new FileBody(file,"jpg");         
	        //Add the data to the multipart entity
	        mpEntity.addPart("file", cbFile);
			ExifInterface exif = new ExifInterface(file.getAbsolutePath());
			String photo_lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
			String photo_lgt = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
	        mpEntity.addPart("latitude",new StringBody(getNumfromString(photo_lat)));
	        mpEntity.addPart("longitude",new StringBody(getNumfromString(photo_lgt)));
	        post.setEntity(mpEntity);
	        //Execute the post request
	        HttpResponse response1 = client.execute(post);
	        //Get the response from the server
	        HttpEntity resEntity = response1.getEntity();
	        String Response=EntityUtils.toString(resEntity);
	        Log.d("Response:", Response);
	        Log.d("Path:", file.getAbsolutePath());
	        client.getConnectionManager().shutdown();
	        return Response;
		}

	private String getNumfromString(String photo_lat)
	{
		//27/1,37/1,10901/1000
		String[] nums=photo_lat.split(",");
		Double deg=Double.valueOf(nums[0].substring(0,nums[0].length()-2));
		Double min=Double.valueOf(nums[1].substring(0,nums[1].length()-2));
		Double sec=Double.valueOf(nums[2].substring(0,nums[2].length()-5));
		Log.i("num", photo_lat+"");
		Log.i("num_d", deg+"");
		Log.i("num_m", min+"");
		Log.i("num_s", sec+"");
		Double my_num=deg+min/60+sec/3600000;
		return String.valueOf(my_num);
	}

	public String getFileHeader(File file)
	   {
		   String header=null;
		   String json=getStringFromFile(file.getAbsolutePath());
		   try
		   {
			   header=new JSONObject(json).getJSONObject("ReportItemIncident").getString("item_name");
		   }
		   catch (JSONException e) 
		   {
			   e.printStackTrace();
		   }
		   return header;
	   }
	   
	   public void clear(){
	       File[] files=cacheDir.listFiles();
	       for(File f:files)
	           f.delete();
	   }
	   
	   public void deletefile(String file_name)
	   {
	       File[] files=cacheDir.listFiles();
	       for(File f:files)
	       {
	           if(f.getName().equals(file_name))
	           {
	        	   String photo_name="";
				   try
				   {
					   JSONObject json=new JSONObject(getStringFromFile(f.getAbsolutePath()));
					   JSONObject photo=json.getJSONObject("Photo");
					   if(photo.getBoolean("photo_taken"))
					   {
						   deletefile(photo_name);
					   }
					   f.delete();
				   }
				   catch(JSONException e)
				   {
					   e.printStackTrace();
				   }
	           }
	       }
	   }

	   public void storeFile(String json) throws FileNotFoundException, IOException
	   {
		      if (android.os.Environment.getExternalStorageState().equals(
			           android.os.Environment.MEDIA_MOUNTED)) 
			  {
				  try
				  {
					  int i=1;
					  JSONObject report=new JSONObject(json);
					  file_name=(report.getJSONObject("ReportItemIncident")).getString("item_name");
					  File root=new File(cacheDir, file_name+".txt");
					  while(root.exists())
					  {
						 // root.renameTo(new File(cacheDir,file_name+i+".txt"));
						  root=new File(cacheDir,file_name+i+".txt");
						  i=i+1;
					  }
					  FileWriter writer = new FileWriter(root);
					  writer.append(json);
					  writer.flush();
					  writer.close();
				  }
				  catch (JSONException e) 
				  {
					  e.printStackTrace();
				  }
			  }
			  else 
			  {
			  }

	   }
	   
	   public void toaster(String s)
		{
			Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
		}
	   public boolean upload_status;
	private ProgressDialog mProgressDialog;
	   
	   public boolean upload(File file)
	   {
		   mProgressDialog=new ProgressDialog(mContext);
		   mProgressDialog.setMessage("Uploading File");
		   mProgressDialog.setProgressStyle(mProgressDialog.STYLE_SPINNER);
		   mProgressDialog.setCancelable(false);
		   mProgressDialog.show();
		   String photo_name="";
		   try
		   {
			   JSONObject json=new JSONObject(getStringFromFile(file.getAbsolutePath()));
			   JSONObject photo=json.getJSONObject("Photo");
			   if(photo.getBoolean("photo_taken"))
			   {
				   photo_name=photo.getString("name");
				   try 
				   {
					   String result=uploadphotousingmultipart(getFile(photo_name));
					   Log.i("upload_photo", result);
					   String gallery="{\"uploaded_temp\":"+result+"}";
					  // json.put("ReportItemMultimedia", result);
					   json.remove("Photo");
					   json.put("photo",result);
					   upload_status=postData(json.toString());
					   if(upload_status&&!photo_name.equals(""))
					   {
						   Log.i("post", json.toString());
						   deletefile(photo_name);
					   }
				   }
				   catch (Exception e) 
				   {
					   showerrordialog("Server Fail", "Server Under maintainance!Please Try Again Later");
					   e.printStackTrace();
					   upload_status=false;
				   }
			   }
			   else
			   {
				   Log.i("Photo", photo_name);
				   json.remove("Photo");
				   upload_status=postData(json.toString());
				   if(upload_status)
				   {
					   Log.i("post", json.toString());
					   //deletefile(photo_name);
				   }
			   }

		   }
		   catch (JSONException e) 
		   {
			   e.printStackTrace();
		   }
		   mProgressDialog.dismiss();
		   return upload_status;
	   }

	public boolean upload_success;
	
	private boolean postData(String data)
	{
		StringReceiver connect=new StringReceiver(mContext);
		Log.i("data_post", data);
		connect.setPath("/girc/dmis/api/rapid_assessment/report-items/ReportItemIncident/create");
		JSONObject my_report = null;
		try
		{
			my_report=new JSONObject(data);
		}
		catch (JSONException e1) 
		{
			e1.printStackTrace();
		}
		connect.setString(my_report.toString());
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try 
		{
			String result = output.get();
			Log.i("upload", result);
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				Log.i("res", obj.toString());
				String error=obj.getString("status");
				if(error.equals("success"))
				{
					upload_success=true;
					
				}
				else if(error.equals("server_fail"))
				{
					showerrordialog("Server Fail", obj.getString("message"));
				}
				else if(error.equals("0"))
				{
					String error_messages="";
					JSONArray obj2=new JSONArray(obj.getString("errors"));
					for(int i=0;i<obj2.length();i++)
					{
						String new_error=obj2.getJSONObject(i).getString("msg");
						error_messages=error_messages+"\n"+new_error.substring(2,new_error.length()-2);
					}
					upload_success=false;
					String title=new JSONObject(data).getJSONObject("ReportItemIncident").getString("item_name");
					showerrordialog(title,error_messages);
				}
				else
				{
					upload_success=false;
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
		return upload_success;
	}
	
	@SuppressWarnings("deprecation")
	public void showerrordialog(String title, String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		//alertDialog.setIcon(R.drawable.delete);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(final DialogInterface dialog, final int which) 
			{
			
			}
		});
		alertDialog.show();	                	
	}
	
	@SuppressWarnings("deprecation")
	public void showcreateddialog(String message){
		    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
		alertDialog.setTitle("Success");
		alertDialog.setMessage(message);
//		alertDialog.setIcon(R.drawable.tick);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(final DialogInterface dialog, final int which) 
			{
/*				Intent intent=new Intent(getApplicationContext(),Reporting.class);
				startActivity(intent);
				finish();
*/			}
		});
		alertDialog.show();	                	
	}

	public boolean uploadall()
	{
		File[] files = getallFiles("txt");
		boolean success=true;
		for(int i=0;i<files.length;i++)
		{
			success=upload(files[i]);
			if(!success)
			{			
				deletefile(files[i].getName());
				break;
			}
		}
		if(success)
		{
			showcreateddialog("Upload Successful");
			return true;
		}
		else
		{
			return false;
		}

	}

	public JSONArray getNotifications()
	{
		  Log.i("notify1","1");
		  File notifyDir=new File(cacheDir+"/notification");
		  if(!notifyDir.exists())
			  notifyDir.mkdirs();
		  File root=new File(notifyDir,file_name+".txt");
		  String allNotification=getStringFromFile(root.getPath());
		  Log.i("notify",allNotification+"");
		  JSONArray notificationArray = null;
		  if(allNotification==null)
		  {
			  notificationArray=new JSONArray();
		  }
		  else
		  {
			  try 
			  {
				  notificationArray=new JSONArray(allNotification);
			  }
			  catch (JSONException e) 
			  {
				  e.printStackTrace();
			  }
		  }	
		  
		  return notificationArray;
	}
	
	public void storeNotification(String title, String message, int j, String state)
	{
	      if (android.os.Environment.getExternalStorageState().equals(
		           android.os.Environment.MEDIA_MOUNTED)) 
		  {
			  try
			  {
				  Log.i("notify", "called");
				  String currentdate=getDate();
				  File notifyDir=new File(cacheDir+"/notification");
				  if(!notifyDir.exists())
					  notifyDir.mkdirs();
				  File root=new File(notifyDir,file_name+".txt");
				  String allNotification=getStringFromFile(root.getPath());
				  Log.i("notify",allNotification+"");
				  JSONArray notificationArray;
				  if(allNotification==null)
				  {
					  notificationArray=new JSONArray();
					  JSONObject notification=new JSONObject();
					  notification.put("id", j);
					  notification.put("title",title);
					  notification.put("message", message);
					  notification.put("date", currentdate);
					  notification.put("state", state);
					  notificationArray.put(notification);
				  }
				  else
				  {
					  notificationArray=new JSONArray(allNotification);
					  int i;
					  for(i=0;i<notificationArray.length();i++)
					  {
						  if(notificationArray.getJSONObject(i).getInt("id")==j)
						  {
							  if(!notificationArray.getJSONObject(i).getString("state").equals(state))
							  {
								  notificationArray.getJSONObject(i).put("id", j);
								  notificationArray.getJSONObject(i).put("title",title);
								  notificationArray.getJSONObject(i).put("message", message);
								  notificationArray.getJSONObject(i).put("date", currentdate);
								  notificationArray.getJSONObject(i).put("state", state);
								  notificationArray.getJSONObject(i).put("opened", false);
							  }
						  }
					  }

					  if(i==notificationArray.length())
					  {
						  JSONObject notification=new JSONObject();
						  notification.put("id", j);
						  notification.put("title",title);
						  notification.put("message", message);
						  notification.put("date", currentdate);
						  notification.put("state", state);
						  notification.put("opened", false);
						  notificationArray.put(notification);
					  }
				  }
				  String newAllNotification=notificationArray.toString();
				  try 
				  {
					  FileWriter writer = new FileWriter(root);
					  writer.append(newAllNotification);
					  writer.flush();
					  writer.close();
				  }
				  catch (IOException e) 
				  {
					  Log.e("error",e.toString());
				  }
			  }
			  catch (Exception e) 
			  {
				  Log.e("error",e.toString());
				  e.printStackTrace();
			  }
		  }
		  else 
		  {
			  Log.e("error", "error");
		  }


	}

	public String getDate() 
	{


		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

		String currentDate = dateFormatGmt.format(new Date());
		return currentDate.toString();
	}
	public void replacenotification(JSONArray notesJSONArray) 
	{
		PrintWriter pw = null;
		File notifyDir=new File(cacheDir+"/notification");
		if(!notifyDir.exists())
			notifyDir.mkdirs();
		File root=new File(notifyDir,file_name+".txt");
		try
		{
			pw = new PrintWriter(root);
			pw.write(notesJSONArray.toString());
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		pw.close();
	}

	public void deletenotification(String title, String message) 
	{
		try
		{
			JSONArray jsonArray =getNotifications();
			JSONArray list = new JSONArray();     
			for(int i=0;i<jsonArray.length();i++)
			{
				if((jsonArray.getJSONObject(i).getString("title").equals(title))
						&&
						(jsonArray.getJSONObject(i).getString("message").equals(message)))
				{
				}
				else
				{
					list.put(jsonArray.getJSONObject(i));
				}

			}
			replacenotification(list);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}

	public void deleteallnotification()
	{
		JSONArray jsonArray =getNotifications();
		JSONArray list = new JSONArray();     
		replacenotification(list);
	}
	
	public ArrayList<String> getFromFile(String parent_name,String parent_type,String child_type)
	{
		ArrayList<String> child_name=new ArrayList<String>();
		File file1=new File(cacheDir,"/reportitems_config/item-children.json");
		String fileData=getStringFromFile(file1.getAbsolutePath());
		try 
		{
			JSONArray FileJsonArray = new JSONArray(fileData);
			for(int i=0;i<FileJsonArray.length();i++)
			{
				if(FileJsonArray.getJSONObject(i).getString("parent_name").equals(parent_name)
						&&FileJsonArray.getJSONObject(i).getString("parent_type").equals(parent_type)
						&&FileJsonArray.getJSONObject(i).getString("child_type").equals(child_type))
				{
					child_name.add(FileJsonArray.getJSONObject(i).getString("child_name"));
				}
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		for(int i=0;i<child_name.size();i++)
			Log.i("incident", child_name.get(i));
		return child_name;
	}
	
	public ArrayList<String> getNeedFromFile(String child_type)
	{
		ArrayList<String> child_name=new ArrayList<String>();
		File file1=new File(cacheDir,"/reportitems_config/item-types.json");
		String fileData=getStringFromFile(file1.getAbsolutePath());
		try 
		{
			JSONArray FileJsonArray = new JSONArray(fileData);
			for(int i=0;i<FileJsonArray.length();i++)
			{
				if(FileJsonArray.getJSONObject(i).getString("type").equals(child_type))
				{
					child_name.add(FileJsonArray.getJSONObject(i).getString("item_name"));
				}
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		for(int i=0;i<child_name.size();i++)
			Log.i("incident", child_name.get(i));
		return child_name;
	}
	
	public ArrayList<String> getTypesFromFile(String item_name,String basis)
	{
		ArrayList<String> child_name=new ArrayList<String>();
		File file1=new File(cacheDir,"/reportitems_config/item-classes.json");
		String fileData=getStringFromFile(file1.getAbsolutePath());
		Log.i("JSON", fileData);
		try 
		{
			JSONArray FileJsonArray = new JSONArray(fileData);
			for(int i=0;i<FileJsonArray.length();i++)
			{
				if(FileJsonArray.getJSONObject(i).getString("item_name").equals(item_name)
						&&FileJsonArray.getJSONObject(i).getString("basis").equals(basis))
				{
					child_name.add(FileJsonArray.getJSONObject(i).getString("name"));
				}
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return child_name;
	}
	
	public ArrayList<String> getIncidentFromFile()
	{
		ArrayList<String> incident_array=new ArrayList<String>();
		File file=new File(cacheDir,"/reportitems_config/item-types.json");
		String fileData=getStringFromFile(file.getAbsolutePath());
		try 
		{
			JSONArray FileJsonArray = new JSONArray(fileData);
			for(int i=0;i<FileJsonArray.length();i++)
			{
				if(FileJsonArray.getJSONObject(i).getString("type").equals("incident"))
				{
					incident_array.add(FileJsonArray.getJSONObject(i).getString("item_name"));
				}
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		for(int i=0;i<incident_array.size();i++)
			Log.i("incident", incident_array.get(i));
		return incident_array;
	}

		public void addImpacttoFile(String child_name, String parent_name, String parent_type, String child_type)
		{
			File file=new File(cacheDir,"/reportitems_config/item-children.json");
			String fileData=getStringFromFile(file.getAbsolutePath());
			try
			{
				JSONArray jsonArray=new JSONArray(fileData);
				int new_id = jsonArray.length()+3;
				JSONObject new_data = new JSONObject()
					.put("id", new_id)
					.put("parent_name", parent_name)
					.put("parent_type",parent_type)
					.put("child_name", child_name)
					.put("child_type", child_type);
				jsonArray.put(new_data);
				PrintWriter pw = new PrintWriter(file);
				pw.write(jsonArray.toString());
				pw.close();
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}

		public void addNeedtoFile(String child_name, String parent_name,
				String parent_type, String child_type) {
			File file=new File(cacheDir,"/reportitems_config/item-types.json");
			String fileData=getStringFromFile(file.getAbsolutePath());
			try
			{
				JSONArray jsonArray=new JSONArray(fileData);
				int new_id = jsonArray.length()+1;
				JSONObject new_data = new JSONObject()
					.put("id", new_id)
					.put("is_verified",false)
					.put("item_name", child_name)
					.put("type", child_type);
				jsonArray.put(new_data);
				PrintWriter pw = new PrintWriter(file);
				pw.write(jsonArray.toString());
				pw.close();
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
	
}