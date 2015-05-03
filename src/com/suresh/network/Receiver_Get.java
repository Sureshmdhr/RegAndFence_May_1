package com.suresh.network;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class Receiver_Get extends AsyncTask<Void, Void, String>
{
	private String host = "http://116.90.239.21/polygon1/";
//	private String host = "http://192.168.19.1/polygon/";
	private String path;

	public Receiver_Get()
	{
		
	}
	
	protected String doInBackground(Void[] paramArrayOfVoid)
	{
		try
		{
			String str1 = this.host + this.path;
			DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
			HttpGet localHttpGet = new HttpGet(str1);
			String str2 = EntityUtils.toString(localDefaultHttpClient.execute(localHttpGet).getEntity());
			return str2;
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
		}
		return null;
	}

	public String getHost()
	{
		return this.host;
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
}