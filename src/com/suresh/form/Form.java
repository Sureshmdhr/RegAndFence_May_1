package com.suresh.form;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.suresh.admin.AdminMenuActivity;
import com.suresh.extras.SessionManager;
import com.suresh.menus.BaseActivity;
import com.suresh.network.Receiver;
import com.suresh.network.StringReceiver;

public class Form extends FragmentActivity {
	
	public static String FbLoginName;
	public static String FbEmail;
	public static String FbUserId,FbUserBirthDay,FbUserGender;
	public static EditText login_username;
	static EditText login_password;
	TextView v;
	StringBuilder sb;
	String data;
	static int user_id;
	String uname,upass;
	String username;
	SessionManager session;
	//static String fence="http://192.168.144.1/polygon";
	public static String fence="http://116.90.239.21/polygon1";
	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_form);
			if (findViewById(R.id.fragment_container) != null) {

	           if (savedInstanceState != null) {
	                return;
	            }
	            Configuration config = getResources().getConfiguration();

	            FB_Fragment firstFragment = new FB_Fragment();
	            firstFragment.setArguments(getIntent().getExtras());
	            //ragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//	            
//	            // Add the fragment to the 'fragment_container' FrameLayout
	            getSupportFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, firstFragment).commit();
			}
		}
			
		
		
		
			public class FB_Fragment extends Fragment{
				
				
					// *A
				public Profile profile;
		        private CallbackManager mCallbackManager;
		        private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>(){
		            @Override
		            public void onSuccess(LoginResult loginResult) {
		                AccessToken accessToken = loginResult.getAccessToken();
		               profile= Profile.getCurrentProfile();

		                // App code
		                GraphRequest request = GraphRequest.newMeRequest(
		                        loginResult.getAccessToken(),
		                        new GraphRequest.GraphJSONObjectCallback() {
		                            @Override
		                            public void onCompleted(
		                                    JSONObject object,
		                                    GraphResponse response) 
		                            {
		                            	try {
											FbEmail=object.getString("email");
											FbLoginName=object.getString("name");
											FbUserId=object.getString("id");
											FbUserBirthDay=object.getString("birthday");
											FbUserGender=object.getString("gender");
											JSONObject form=new JSONObject();
											JSONObject obj2=new JSONObject();
											JSONObject profile=new JSONObject();
											try
											{
												form.put("email",FbEmail);
												profile.put("full_name",FbLoginName);
												obj2.put("Profile", profile);
												obj2.put("User", form);
											}
											catch (JSONException e) 
											{
												e.printStackTrace();
											}
											registerNewUserFromFacebook(obj2.toString());

											session.createLoginSession("user",FbLoginName, 1234);
						                    session.createTimesSession(1);
											if(!session.checksetting()){
												Boolean[] choices={true,true,true,true,true};
												session.create_catagory_session(choices);
											}
											postnameonLocation(uname);
											Log.i("login", FbLoginName);
						                    Intent intent=new Intent(getApplicationContext(),BaseActivity.class);
						                    getActivity().startActivity(intent);
								        	getActivity().finish();
								        	Log.v("LoginActivity", response.toString());
			                            	Log.v("email", object.getString("email"));
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
		                            	Log.v("LoginActivity", response.toString());
		                            }

									private void registerNewUserFromFacebook(String data) 
									{
										try 
										{
											StringReceiver connect=new StringReceiver();
											connect.setPath("girc/dmis/api/user/users/register");
											connect.setString(data);
											AsyncTask<Void, Void, String> output = connect.execute(new Void[0]);
											Log.i("FbRegisterOutput", output.get());
										}
										catch (InterruptedException e) 
										{
											e.printStackTrace();
										}
										catch (ExecutionException e) 
										{
											e.printStackTrace();
										}
									}
		                        });
		                Bundle parameters = new Bundle();
		                parameters.putString("fields", "id,name,email,gender, birthday");
		                request.setParameters(parameters);
		                request.executeAsync();

		                if(profile!=null){

		                	FbLoginName= profile.getName();
		                    SaveImage(String.valueOf(profile.getProfilePictureUri(250, 250)));
		                    
//		                    
//		                    tv.setText(profile.getName());

		                }

		            }

		            @Override
		            public void onCancel() {

		            }

		            @Override
		            public void onError(FacebookException e) {

		            }
		        };
		        
		        public void SaveImage(String link){

		            Picasso.with(getActivity())
		                    .load(link)
		                    .into(target);

		        }

		        private Target target = new Target() {
		            @Override
		            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
		                new Thread(new Runnable() {
		                    @Override
		                    public void run() {

		                        File file = new File(Environment.getExternalStorageDirectory().getPath() +"/girc/.Cache/fbpropic.jpg");
		                        try
		                        {
		                            file.createNewFile();
		                            FileOutputStream ostream = new FileOutputStream(file);
		                            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
		                            ostream.close();
		                        }
		                        catch (Exception e)
		                        {
		                            e.printStackTrace();
		                        }

		                    }
		                }).start();
		            }


		            @Override
		            public void onBitmapFailed(Drawable errorDrawable) {

		            }

		            @Override
		            public void onPrepareLoad(Drawable placeHolderDrawable) {

		            }
		        };
		        


		           
		        //Constructor
		        public FB_Fragment(){
		        	
		        }
		        
		        public void onCreate(Bundle savedInstanceState) {
		        	// *A
		        	super.onCreate(savedInstanceState);
					   FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
			            mCallbackManager = CallbackManager.Factory.create();
		        };
		        
				   @Override
				   public View onCreateView(LayoutInflater inflater,
				      ViewGroup container, Bundle savedInstanceState) {
				  
				      return inflater.inflate(
				      R.layout.activity_form_clone, container, false);
				   }
				   
				   
				   
				   @Override
				public void onViewCreated(View view,
						@Nullable Bundle savedInstanceState) {
					// TODO Auto-generated method stub
					super.onViewCreated(view, savedInstanceState);
					LoginButton loginbutton = (LoginButton)view.findViewById(R.id.login_button);
					String filePath = Environment.getExternalStorageDirectory()
		                    .getAbsolutePath() + File.separator + "actress_wallpaper.jpg";
		            Bitmap bmp = BitmapFactory.decodeFile(filePath);
		            //iv.setImageBitmap(bmp);
		            loginbutton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday,user_friends"));

		          //  loginbutton.setReadPermissions("user_friends");
		            loginbutton.setFragment(this);
		            loginbutton.registerCallback(mCallbackManager, mCallback);
		            
		            
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy);
					session = new SessionManager(getApplicationContext());

			        Log.i("check",String.valueOf(session.checkLogin()));
			        if(!session.checkLogin())
			        {
			        	if(!haveNetworkConnection())
			        	{
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
										JSONObject data=new JSONObject();
										try 
										{
											data.put("LoginForm", formlogin);
										}
										catch (JSONException e) 
										{
											e.printStackTrace();
										}
										postData(data.toString());
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
						Receiver connect=new Receiver(getActivity());
						connect.setPath("/postnameinLocation.php");
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
					  
					  @Override
				        public void onActivityResult(int requestCode, int resultCode, Intent data) {
				            super.onActivityResult(requestCode, resultCode, data);
				            mCallbackManager.onActivityResult(requestCode, resultCode, data);

				        }

			
			}
			
			
	         
			
			
}
    

