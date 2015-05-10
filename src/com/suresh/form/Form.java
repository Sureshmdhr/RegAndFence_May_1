package com.suresh.form;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.suresh.admin.AdminMenuActivity;
import com.suresh.extras.SessionManager;
import com.suresh.menus.BaseActivity;
import com.suresh.network.Receiver;
import com.suresh.network.StringReceiver;

public class Form extends Activity {

	public static EditText login_username;
	static EditText login_password;
	TextView v;
	StringBuilder sb;
	JSONObject data;
	static int user_id;
	String uname,upass;
	String username;
	SessionManager session;
	protected ProgressDialog mProgressDialog;
	//static String fence="http://192.168.144.1/polygon";
	public static String fence="http://116.90.239.21/polygon1";

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		session = new SessionManager(getApplicationContext());

        Log.i("check",String.valueOf(session.checkLogin()));
        if(!session.checkLogin())
        {
        	if(!haveNetworkConnection())
        	{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.internet_connectivity_message)
				.setTitle(R.string.internet_connectivity_header);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						finish();
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();	

        	}
        	else
        	{
	        	Button login=(Button)findViewById(R.id.btnReport);
				login_username=(EditText)findViewById(R.id.log_username);
				login_password=(EditText)findViewById(R.id.rep_place);
		        login.setOnClickListener(new OnClickListener() 
		        {
		        	public void onClick(View arg0) 
		        	{
		        		uname=login_username.getText().toString();
						upass=login_password.getText().toString();
						if((!uname.equals(""))&&(!upass.equals("")))
						{
							JSONObject formlogin=new JSONObject();
							try 
							{
								formlogin.put("username", uname);
								formlogin.put("password", upass);
							}
							catch (JSONException e) 
							{
								e.printStackTrace();
							}
							data=new JSONObject();
							try 
							{
								data.put("LoginForm", formlogin);
							}
							catch (JSONException e) 
							{
								e.printStackTrace();
							}
							if(haveNetworkConnection())
							{
								try
								{
				        		mProgressDialog=new ProgressDialog(Form.this);
				        		mProgressDialog.setMessage(" Logging in \n Please Wait");
				        		mProgressDialog.setProgressStyle(mProgressDialog.STYLE_SPINNER);
				        		mProgressDialog.show();
            	                Form.this.runOnUiThread(new Runnable() {
									public void run() 
									{
	                	                try 
	                	                {
	    				                	postData(data.toString());
		                	                mProgressDialog.dismiss();
	                	                }
	                	                catch (Exception e) 
	                	                {
	                	                	Log.e("exception", e.toString());
	                	                }
										
									}
								});
								}
								catch(Exception e)
								{
            	                	Log.e("exception", e.toString());
								}
							}
							else
							{
								toaster("No Internet Connection");
							}
						}
						else 
						{
							showerrordialog("Empty field!");
						}
		        	}
		        });
        	}
        	TextView registerScreen = (TextView) findViewById(R.id.link_to_register);
        	
	        registerScreen.setOnClickListener(new View.OnClickListener() 
	        {
	        	public void onClick(View v) 
	        	{
	        		Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
	                startActivity(i);
	                finish();
	        	}
	        });
        }
        else
        {
        	Intent intent=new Intent(getApplicationContext(),BaseActivity.class);
        	startActivity(intent);
        	finish();
        }
	}
	
	public void postData(String data) {
		StringReceiver connect=new StringReceiver(Form.this);
	//	connect.setHost("http://116.90.239.21");
		connect.setPath("/girc/dmis/api/user/users/login");
		new JSONObject();
		connect.setString(data);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try {
			String result=output.get();
            Log.i("errorlogin", result);
            String check_message = null;
            try
            {
				JSONObject check=new JSONObject(result);
				check_message=check.getString("status");
			} 
            catch (JSONException e)
            {
				e.printStackTrace();
			}
            if(check_message.equals("success"))
            {
            	String message = null;
				try 
				{
					Log.i("log_in", result);
					JSONObject check=new JSONObject(result);
					message=check.getString("msg");
					String user = check.getString("user");
					JSONObject details=new JSONObject(user);
					user_id=details.getInt("id");
					username=details.getString("username");
					Log.i("id", String.valueOf(user_id));
				}
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
				showcreateddialog(message);
            }
            else if(check_message.equals("server_fail"))
            {
				try {
					JSONObject rs = new JSONObject(result);
					String error_message = rs.getString("message");
					showerrordialog(error_message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else
            {
            	try 
            	{
    				JSONObject rs=new JSONObject(result);
    				String error_message = rs.getString("errors");
    				JSONArray error_msg1=new JSONArray(error_message);
    				String err_msg2=error_msg1.getJSONObject(0).toString();
    				showerrordialog(err_msg2);
            	}
            catch (JSONException e1) 
            {
            	Log.i("error", e1.toString());
				e1.printStackTrace();
            }
            }
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
	}

	public void postnameonLocation(String uname) {
		Receiver connect=new Receiver(this);
		connect.setPath("postnameinLocation.php");
		connect.addNameValuePairs("value1",uname);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
		try {
			output.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void toaster(String s){
			Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
			}
	public void passclear(){
    	login_username.setText("");
    	login_password.setText("");
	}
	@SuppressWarnings("deprecation")
	public void showerrordialog(String message){
	    AlertDialog alertDialog = new AlertDialog.Builder(Form.this).create();
	// Setting Dialog Title
	alertDialog.setTitle("Error");
	// Setting Dialog Message
	alertDialog.setMessage(message);
	// Setting Icon to Dialog
	alertDialog.setIcon(R.drawable.delete);
	// Setting OK Button
	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(final DialogInterface dialog, final int which) {
	        // Write your code here to execute after dialog closed
	       // Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
	       
/*		        Intent intent=new Intent(getApplicationContext(),BaseActivity.class);
		        startActivity(intent);
*/
	        }
	});

	// Showing Alert Message
	alertDialog.show();	                	

	}
	@SuppressWarnings("deprecation")
	public void showcreateddialog(String message){
	    AlertDialog alertDialog = new AlertDialog.Builder(Form.this).create();
	// Setting Dialog Title
	alertDialog.setTitle("Success");
	// Setting Dialog Message
	alertDialog.setMessage(message);
	// Setting Icon to Dialog
	//alertDialog.setIcon(R.drawable.tick);
	// Setting OK Button
	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(final DialogInterface dialog, final int which) {
	        	if(uname.equals("admin")){
	        		session.createLoginSession("admin","admin", user_id);
			        Intent intent=new Intent(getApplicationContext(),AdminMenuActivity.class);
			        startActivity(intent);
			        finish();
	        	}
	        	else{
	        		Log.i("user", uname);
		        	session.createLoginSession("user",uname, user_id);
					session.createTimesSession(1);
					if(!session.checksetting()){
						Boolean[] choices={true,true,true,true,true};
						session.create_catagory_session(choices);
					}
					postnameonLocation(uname);
			        Intent intent=new Intent(getApplicationContext(),BaseActivity.class);
			        startActivity(intent);
			        finish();
	        	}
	        }
	});

	// Showing Alert Message
	alertDialog.show();	                	

	}
	  public boolean haveNetworkConnection() 
	  {
		    boolean haveConnectedWifi = false;
		    boolean haveConnectedMobile = false;
		    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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
