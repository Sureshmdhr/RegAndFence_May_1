package com.suresh.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class StringReceiver extends AsyncTask<Void, Void, String>
{
	private String host = "http://116.90.239.21/polygon1/";
	private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	private String path;
	private Context mContext=null;
	private String data;

	public StringReceiver()
	{
		
	}
	
	public StringReceiver(Context context)
	{
		this.mContext=context;
	}
	public void setString(String data)
	{
		this.data=data;
	}

	protected String doInBackground(Void[] paramArrayOfVoid)
	{
		try
		{
			String str1 = this.host + this.path;
			DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
			HttpPost localHttpPost = new HttpPost(str1);
			localHttpPost.setHeader("Content-Type","application/json");
			localHttpPost.setEntity(new StringEntity(data));
			String str2 = EntityUtils.toString(localDefaultHttpClient.execute(localHttpPost).getEntity());
			return str2;
		}
		catch (Exception localException)
		{
			Log.i("server", localException.toString());
			String server_error = "Server Under Maintainance.Please TryAgain Later";
			String str3 = null;
			try {
				str3 = new JSONObject().put("status", "server_fail").put("message", server_error).toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return str3;
		}
	}

	public String getHost()
	{
		return this.host;
	}

	public List<NameValuePair> getNameValuePairs()
	{
		return this.nameValuePairs;
	}

	public String getPath()
	{
		return this.path;
	}	

	protected void onPostExecute(String paramString)
	{
		super.onPostExecute(paramString);
	}	

	protected void onProgressUpdate(Void... values)
	{
		super.onProgressUpdate(values);
	}

	protected void onPreExecute()
	{
		super.onPreExecute();
	}

	public void setHost(String paramString)
	{
		this.host = paramString;
	}

	public void setPath(String paramString)
	{
		this.path = paramString;
	}
	
	public boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;
	    ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}


}
