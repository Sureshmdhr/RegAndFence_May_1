package com.suresh.extras;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;

import com.suresh.form.R;
import com.suresh.menus.BaseActivity;

public class About extends BaseActivity 
{
	private TextView about;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		getLayoutInflater().inflate(R.layout.about, frameLayout);

		mDrawerList.setItemChecked(position, true);
		
		setTitle(navMenuTitles[position]);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        about=(TextView)findViewById(R.id.about_tv);
        about.setText("This app is to get the notification whenever you enter any incident area " +
        		"i.e. geofenced area." +
        		"\nYou can also create your own geofence and get notification if " +
        		"any disaster occurs on your geofece even if you are not inside the area." +
        		"\n And you can report the disaster form the app." +
        		"\n\nThank You");
	}
}
