package com.suresh.geofence;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suresh.form.R;
import com.suresh.menus.AndroidMenusActivity;

public class message extends AndroidMenusActivity
{
	TextView msgtitle,msg;
	Button viewmap;
	//MapView mv=geofences.mv;
	String ms_lat,ms_lgt,title,message;
	double lat,lgt;
	int no;
	TextView[] value;
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.message);
        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {    
        	ms_lat = extras.getString("lat");
            ms_lgt = extras.getString("lgt");
            title = extras.getString("title");
            message = extras.getString("message");
            no=extras.getInt("no");
        }
        if(no==1)
        {
        	View linearLayout =  findViewById(R.id.info);
        	value[no].setText(message);
        	value[no].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        	((LinearLayout) linearLayout).addView(value[no]);
        }
        lat=Double.parseDouble(ms_lat);
        lgt=Double.parseDouble(ms_lgt);
	    msgtitle=(TextView)findViewById(R.id.title);
	    msg=(TextView)findViewById(R.id.msg);
	    viewmap=(Button)findViewById(R.id.viewmap);
	    msgtitle.setText(title);
	    msg.setText(message);
	    Log.i("lat", ms_lat);
	    viewmap.setOnClickListener(new OnClickListener() 
	    {
			public void onClick(View arg0) 
			{
				Intent i=new Intent(getApplicationContext(),geofences.class);
				i.putExtra("latitude", Double.valueOf(ms_lat));
				i.putExtra("longitude", Double.valueOf(ms_lgt));
				startActivity(i);
				finish();
			}
		});
	}
}