package com.suresh.extras;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.suresh.form.Form;
import com.suresh.form.R;
import com.suresh.reporting.Reporting_pg1;

public class MyWidgetProvider extends AppWidgetProvider
{
	private SessionManager session;



	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

		session = new SessionManager(context);

        Log.i("check",String.valueOf(session.checkLogin()));
        if(session.isLoggedIn())
        {
	        Intent configIntent = new Intent(context, Reporting_pg1.class);
		    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
		    remoteViews.setOnClickPendingIntent(R.id.widgetlayout, configPendingIntent);
		    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
        else
        {
	        Intent configIntent = new Intent(context, Form.class);
		    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
		    remoteViews.setOnClickPendingIntent(R.id.widgetlayout, configPendingIntent);
		    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
	}
	
	

	public void onDeleted(Context context, int[] appWidgetIds) 
	{
		super.onDeleted(context, appWidgetIds);
		Toast.makeText(context, "See you", Toast.LENGTH_SHORT).show();
	}

}