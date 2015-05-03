package com.suresh.menus;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.suresh.extras.SessionManager;
import com.suresh.form.R;

public class Usersetting extends Activity
{
	CheckBox earthquake,landslide,flood,accident,other;
	static Boolean[] catagory_checked= new Boolean[5]; 
	private SessionManager session;
	Boolean[] checkbox_choice=new Boolean[5];
	Button save,cancel;

	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.user_setting);
	    earthquake=(CheckBox)findViewById(R.id.earthquake);
	    landslide=(CheckBox)findViewById(R.id.landslide);
	    flood=(CheckBox)findViewById(R.id.flood);
	    accident=(CheckBox)findViewById(R.id.accident);
	    other=(CheckBox)findViewById(R.id.other);
	    
	    session= new SessionManager(getApplicationContext());
	    HashMap<String, Boolean>checkbox_status=session.getCatagoryDetails();
        checkbox_choice[0]=checkbox_status.get(SessionManager.earthquake); 
        checkbox_choice[1]=checkbox_status.get(SessionManager.landslide); 
        checkbox_choice[2]=checkbox_status.get(SessionManager.flood); 
        checkbox_choice[3]=checkbox_status.get(SessionManager.accident); 
        checkbox_choice[4]=checkbox_status.get(SessionManager.other);
        catagory_checked=checkbox_choice;
        if(checkbox_choice[0])
        	earthquake.setChecked(true);
        else
        	earthquake.setChecked(false);
        
        if(checkbox_choice[1])
        	landslide.setChecked(true);
        else
        	landslide.setChecked(false);
        
        if(checkbox_choice[2])
        	flood.setChecked(true);
        else
        	flood.setChecked(false);
        
        if(checkbox_choice[3])
        	accident.setChecked(true);
        else
        	accident.setChecked(false);
        
        if(checkbox_choice[4])
        	other.setChecked(true);
        else
        	other.setChecked(false);
        
        cancel=(Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() 
        {
			public void onClick(View arg0) 
			{
				Intent i=new Intent(getApplicationContext(),BaseActivity.class);
				BaseActivity.isLaunch=true;
				BaseActivity.position = 1;
				startActivity(i);
				finish();
			}
		});
        
        save=(Button)findViewById(R.id.save);
        save.setOnClickListener(new OnClickListener() 
        {
			public void onClick(View v) 
			{
				Intent i=new Intent(getApplicationContext(),BaseActivity.class);
				BaseActivity.isLaunch=true;
				BaseActivity.position = 1;
				startActivity(i);
				finish();
			    session.create_catagory_session(catagory_checked);
			}
		});
	}
	
	public void onCheckBoxClicked(View v)
	{
		if(v.getId()==R.id.earthquake)
		{
			if(earthquake.isChecked())
				catagory_checked[0]=true;
			else
				catagory_checked[0]=false;
		}
		else if(v.getId()==R.id.landslide)
		{
			if(landslide.isChecked())
				catagory_checked[1]=true;
			else
				catagory_checked[1]=false;
		}
		else if(v.getId()==R.id.flood)
		{
			if(flood.isChecked())
				catagory_checked[2]=true;
			else
				catagory_checked[2]=false;
		}
		else if(v.getId()==R.id.accident)
		{
			if(accident.isChecked())
				catagory_checked[3]=true;
			else
				catagory_checked[3]=false;
		}	       
		else if(v.getId()==R.id.other)
		{
			if(other.isChecked())
				catagory_checked[4]=true;
			else
				catagory_checked[4]=false;
		}
	}
}

