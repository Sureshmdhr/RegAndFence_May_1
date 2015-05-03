package com.suresh.geofence;

import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.suresh.form.R;
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)

public class polygon extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.polygon);

            // create the TabHost that will contain the Tabs
            TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);


            TabSpec tab1 = tabHost.newTabSpec("First Tab");
            TabSpec tab2 = tabHost.newTabSpec("Second Tab");

           // Set the Tab name and Activity
           // that will be opened when particular Tab will be selected
            tab1.setIndicator("Circle");
            tab1.setContent(new Intent(this,circlefragment.class));
            
            tab2.setIndicator("polygon");
            tab2.setContent(new Intent(this,polygonfragment.class));

            
            /** Add the tabs  to the TabHost to display. */
            tabHost.addTab(tab1);
            tabHost.addTab(tab2);
            tabHost.setCurrentTab(0);

    }
    
}