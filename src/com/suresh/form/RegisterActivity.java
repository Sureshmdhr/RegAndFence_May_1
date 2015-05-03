package com.suresh.form;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.suresh.network.StringReceiver;

@SuppressLint("NewApi")
public class RegisterActivity extends Activity {
	EditText firstname,email,password,repassword; 
	Button register;
	String data;
	public static String port="http://116.90.239.21";
	String fname,eml,pass,repass;
	StringBuilder sb;
	private int mYear;
	private int mMonth;
	private int mDay;
	static final int DATE_DIALOG_ID = 0;
	private DatePickerDialog.OnDateSetListener mDateSetListener;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy);
		
		firstname=(EditText)findViewById(R.id.reg_firstname);
		email=(EditText)findViewById(R.id.reg_email);
		password=(EditText)findViewById(R.id.reg_password);
		repassword=(EditText)findViewById(R.id.reg_repassword);
		register=(Button)findViewById(R.id.btnRegister);
        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
        loginScreen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
                Intent i = new Intent(getApplicationContext(), Form.class);
                startActivity(i);
                finish();

			}
		});

		register.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("ShowToast")
			@Override
			public void onClick(View v) {
				fname=firstname.getText().toString();
				eml=email.getText().toString();
				pass=password.getText().toString();
				repass=repassword.getText().toString();
				if(eml.equals("")||fname.equals("")||pass.equals("")||repass.equals(""))
		        		{
		        			toaster("Some or all Field are EMPTY");
		        		}
		         //Password Matching Check
		        else if (!pass.equals(repass)){
        			passclear();	 //Clear Only Password EditText Fields
        			toaster("Password mismatch");
		        }
		        
		        //Email Check
		        else if(!isValidEmailAddress(eml)){
	        		email.setText("");
		        	passclear();
		        	toaster("Invalid Email");
	        	}
				
		        else if ((pass.length()<8)||(pass.length()>25))
		        {
			        	passclear();
			        	toaster("Passwod Length should be between 8 and 25");					
				}
		        else if(!pass.equals(repass))
		        {
		        	passclear();
		        	toaster("Passwod MisMatch");					
		        }
				else{
				JSONObject form = new JSONObject();
				try {
					form.put("email",eml);
					form.put("newPassword",pass);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				JSONObject profile = new JSONObject();

				try {
					profile.put("full_name",fname);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				JSONObject obj2 = new JSONObject();
				try {
					obj2.put("Profile", profile);
					obj2.put("User", form);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				data=obj2.toString();
				postData();				

			}
			}
		});
		

	}
	protected void postData() 
	{
		StringReceiver connect=new StringReceiver(this);
		connect.setHost(port);
		//connect.setPath("/emis/app/apis/v1/user/user/registration");
		connect.setPath("/girc/dmis/api/user/users/register");
		connect.setString(data);
		AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
        String result = null;
		try 
		{
			result = output.get();
			Log.i("register", result);
			String check_message = null;
			JSONObject check=new JSONObject(result);
			check_message=check.getString("status");
			if(check_message.equals("success"))
			{
				Log.i("error", "no error");
				showcreateddialog("Account created. Please Activate Your Account through browser first");
			}
			else
			{
				JSONObject rs=new JSONObject(result);
				String error_message = rs.getString("errors");
				JSONArray error_msg1=new JSONArray(error_message);
				String err_msg2=error_msg1.getJSONObject(0).toString();
				showerrordialog(err_msg2);
			}
		}
        catch (JSONException e1) 
        {
			e1.printStackTrace();
		} 
		catch (InterruptedException e2) 
		{
			e2.printStackTrace();
		} 
		catch (ExecutionException e2) 
		{
			e2.printStackTrace();
		}
	}
    
	protected Dialog onCreateDialog(int id) 
	{
	    switch (id) 
	    {
	    case DATE_DIALOG_ID:
	    	return new DatePickerDialog(this,
	    			mDateSetListener,
	    			mYear, mMonth, mDay);
	    }		
	    return null;        
	}
	
	
	public boolean isValidEmailAddress(String emailAddress) 
	{
	    String emailRegEx;
	    Pattern pattern;
	    // Regex for a valid email address
	    emailRegEx = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";
	    // Compare the regex with the email address
	    pattern = Pattern.compile(emailRegEx);
	    Matcher matcher = pattern.matcher(emailAddress);
	    if (!matcher.find()) 
	    {
	    	return false;
	    }
	    return true;
	  }
	
	public boolean isValidBirthDate(String Birthdate) 
	{
		String dateRegEx;
		Pattern pattern;
		// Regex for a valid email address
		dateRegEx = "/^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$/";
		// Compare the regex with the email address
		pattern = Pattern.compile(dateRegEx);
		Matcher matcher = pattern.matcher(Birthdate);
		if (!matcher.find()) 
		{
			return false;
		}
		return true;
	}

	protected void passclear() 
	{
		password.setText("");
		repassword.setText("");
	}

	protected void toaster(String string) 
	{
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
	}

	@SuppressWarnings("deprecation")
	public void showerrordialog(String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
		// Setting Dialog Title
		alertDialog.setTitle("Error");
		// Setting Dialog Message
		alertDialog.setMessage(message);
		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.delete);
		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(final DialogInterface dialog, final int which) 
			{
				// Write your code here to execute after dialog closed
				// Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
			}
		});
		// Showing Alert Message
		alertDialog.show();	                	
	}
	
	@SuppressWarnings("deprecation")
	public void showcreateddialog(String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
		// Setting Dialog Title
		alertDialog.setTitle("Success");
		// Setting Dialog Message
		alertDialog.setMessage(message);
		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.tick);
		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(final DialogInterface dialog, final int which) 
			{
		        Intent intent=new Intent(getApplicationContext(),Form.class);
		        startActivity(intent);
		        finish();
			}
		});
		// Showing Alert Message
		alertDialog.show();	                	
	}
}