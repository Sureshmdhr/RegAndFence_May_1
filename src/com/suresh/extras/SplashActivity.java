package com.suresh.extras;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.suresh.admin.GeofencesInList;
import com.suresh.form.Form;
import com.suresh.form.R;
import com.suresh.menus.BaseActivity;

public class SplashActivity extends Activity {

	ProgressBar p;
	int progressstatus=0;
	Handler h= new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		p= (ProgressBar) findViewById(R.id.progressBar1);
        SessionManager session = new SessionManager(getApplicationContext());
        if(session.checkLogin())
        {
        	HashMap<String, String> user = session.getUserDetails();
        	String name = user.get(SessionManager.KEY_NAME);
        	if(name.equals("admin"))
        	{
        		Thread t=new Thread(new Runnable() 
        		{
        			public void run() 
        			{
        				while (progressstatus <100)
        				{
        					progressstatus+=1;
        					h.post(new Runnable()
        					{
        						public void run() 
        						{
        							p.setProgress(progressstatus);
        						}
        					});
	    					try 
	    					{
	        					Thread.sleep(30);
	    					}
	    					catch (InterruptedException e) 
	    					{
	        					e.printStackTrace();
	        				}
	    					if (progressstatus==100)
	    					{
	    						Intent i=new Intent(getApplicationContext(),GeofencesInList.class);
	    						startActivity(i);
	    						finish();
	    					}
        				} //while
        			} //run	
        		});
        		t.start();
        	}	
        	else if(name.equals("user"))
        	{
        		Thread t=new Thread(new Runnable() 
        		{
        			public void run() 
        			{
        				// TODO Auto-generated method stub
        				while (progressstatus <100)
        				{
        					progressstatus+=1;
        					h.post(new Runnable() 
        					{
							public void run() {
								// TODO Auto-generated method stub
								p.setProgress(progressstatus);
							}
						});
    					try {
        					Thread.sleep(30);
					}
    					catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
    					
    					if (progressstatus==100){
    						Intent i=new Intent(getApplicationContext(),BaseActivity.class);
    						startActivity(i);
    						finish();
    					}
				} //while
			
				} //run	
    		} //
				);
    		t.start();
    		
 	
        }	
        }
        
        else {
    		Thread t=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (progressstatus <100){
	    				
    					progressstatus+=1;
    					h.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								p.setProgress(progressstatus);
							}
						});
    					try {
        					Thread.sleep(10);
					}
    					catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
    					
    					if (progressstatus==100){
    						Intent i=new Intent(getApplicationContext(),Form.class);
    						startActivity(i);
    						finish();
    					}
				} //while
			
				} //run	
    		} //
				);
    		t.start();
    		
 	
        }	
        
       
        

}
}
