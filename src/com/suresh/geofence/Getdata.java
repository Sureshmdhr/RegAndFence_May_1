package com.suresh.geofence;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.suresh.network.Receiver;

public class Getdata {
	String uname;
	private Context mContext;
	
	public Getdata(String username,Context context)
	{
		this.uname=username;
		this.mContext=context;
	}

	public String getData()
	{
		String result = null;
		Receiver connect=new Receiver();
		connect.setPath("/polygon1/extdb3.php");
		connect.addNameValuePairs("value1", this.uname);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try 
		{
			Log.i("put", output.get());
			 result=output.get();//convertStringtoArrayList(output.get(),columnname);
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
	
	public ArrayList<String> convertStringtoArrayList(String result,String column) 
	{
	    ArrayList<String> name = new ArrayList<String>();
	    try 
	    {
	    	if(result!=null)
	    	{
	    	JSONArray myjArray = new JSONArray(result);
	    	JSONObject myjson=null;
	    	for(int i=0; i<myjArray.length();i++){
	    		myjson = (JSONObject)myjArray.getJSONObject(i);
	    		name.add(myjson.getString(column));
	    	}
	    	}
	    } 
	    catch (Exception e) 
	    {
	    	Log.e("log_tag11", "Error Parsing Data "+e.toString());
	    }
	    return name;
	}

	public String getAdminData()
	{
		String result = null;
		Receiver connect=new Receiver();
		connect.setPath("/polygon1/activegeofence.php");
		connect.addNameValuePairs("value1", "admin");
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try 
		{
			 result=output.get();
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
	
	protected String getMyData() 
	{
		Receiver connect=new Receiver(mContext);
		connect.setPath("/polygon1/activegeofence.php");
		connect.addNameValuePairs("value1", uname);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		String result=null;
		try 
		{
			result=output.get();
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

	public void showerrordialog(){
	    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
	// Setting Dialog Title
	alertDialog.setTitle("Error");
	// Setting Dialog Message
	alertDialog.setMessage("Server Under Maintainance.Please TryAgain Later");
	// Setting Icon to Dialog
	//alertDialog.setIcon(R.drawable.delete);
	// Setting OK Button
	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(final DialogInterface dialog, final int which) {
	        }
	});
	alertDialog.show();	                	

	}

	public Boolean checkServer()
	{
		ArrayList<String> result = null;
		Receiver connect=new Receiver();
		connect.setPath("/polygon1/check.php");
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try 
		{
			Log.i("out", output.get());
			String check_message =new JSONObject(new JSONArray(output.get()).getString(0)).getString("status");
			if(check_message.equals("server_fail"))
			{
				Log.i("server", "fail");
				return false;
			}
			else
			{
				Log.i("server", "ok");
				return true;
			}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("server", "ok");
			return true;
	}


}
