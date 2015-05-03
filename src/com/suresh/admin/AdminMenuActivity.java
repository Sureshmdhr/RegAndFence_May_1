package com.suresh.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.suresh.extras.About;
import com.suresh.extras.SessionManager;
import com.suresh.form.R;
import com.suresh.geofence.AllNotification;
import com.suresh.geofence.geofences;
import com.suresh.geofence.userpage;
import com.suresh.menus.NavDrawerItem;
import com.suresh.menus.NavDrawerListAdapter;
import com.suresh.menus.Usersetting;
import com.suresh.reporting.FileCache;
import com.suresh.reporting.Reporting_pg4;

/**
 *
 * This activity will add Navigation Drawer for our application and all the code related to navigation drawer.
 * We are going to extend all our other activites from this BaseActivity so that every activity will have Navigation Drawer in it.
 * This activity layout contain one frame layout in which we will add our child activity layout.    
 */
public class AdminMenuActivity extends Activity {

	/**
	 *  Frame layout: Which is going to be used as parent layout for child activity layout.
	 *  This layout is protected so that child activity can access this  
	 *  */
	protected FrameLayout frameLayout;
	
	/**
	 * ListView to add navigation drawer item in it.
	 * We have made it protected to access it in child class. We will just use it in child class to make item selected according to activity opened.  
	 */
	
	protected ListView mDrawerList;

	/**
	 * Static variable for selected item position. Which can be used in child activity to know which item is selected from the list.  
	 * */
	protected static int position;
	
	/**
	 *  This flag is used just to check that launcher activity is called first time 
	 *  so that we can open appropriate Activity on launch and make list item position selected accordingly.    
	 * */
	public static boolean isLaunch = true;
	
	/**
	 *  Base layout node of this Activity.    
	 * */
	private DrawerLayout mDrawerLayout;
	
	/**
	 * Drawer listner class for drawer open, close etc.
	 */
	private ActionBarDrawerToggle actionBarDrawerToggle;

	protected String[] navMenuTitles;

	private TypedArray navMenuIcons;

	public static ArrayList<NavDrawerItem> navDrawerItems;

	private NavDrawerListAdapter adapter;

	private String username;

	private FileCache notify;

	private JSONArray notify_Array;
	
	private static int old_position=1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_drawer);
		
	    SessionManager session = new SessionManager(getApplicationContext());
	    HashMap<String, String> user = session.getUserDetails();
	    username = user.get(SessionManager.KEY_EMAIL);

	    notify=new FileCache(this);
	    notify_Array=notify.getNotifications();
	    Log.i("aray", notify_Array.toString());
	    int no=0;
	    try 
	    {
	    	int i=0;
			while (i<notify_Array.length())
			{
				if(!notify_Array.getJSONObject(i).getBoolean("opened"))
					no=no+1;
				i++;
			}
		}
	    catch (JSONException e) 
	    {
			e.printStackTrace();
		}
		
		frameLayout = (FrameLayout)findViewById(R.id.frame_container);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		
		// set a custom shadow that overlays the main content when the drawer opens
		//mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
		// set up the drawer's list view with items and click listener
		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array

		navDrawerItems.add(new NavDrawerItem(username, navMenuIcons.getResourceId(0, -1))); //

		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1))); //Home

		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1))); //Report

		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
		
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
		
		// Recycle the typed array
		navMenuIcons.recycle();
		
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		
		mDrawerList.setAdapter(adapter);
		
		mDrawerList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(old_position==position)
				{
					mDrawerLayout.closeDrawer(mDrawerList);
				}
				else
				{
					if(position!=0)
					{
						old_position=position;
						openActivity(position);
					}
				}
			}
		});
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		// ActionBarDrawerToggle ties together the the proper interactions between the sliding drawer and the action bar app icon
		actionBarDrawerToggle = new ActionBarDrawerToggle(
				this,						/* host Activity */
				mDrawerLayout, 				/* DrawerLayout object */
				R.drawable.launcher,     /* nav drawer image to replace 'Up' caret */
				R.string.app_name,       /* "open drawer" description for accessibility */
				R.string.app_name)      /* "close drawer" description for accessibility */ 
		{ 
			@Override
			public void onDrawerClosed(View drawerView) {
				getActionBar().setTitle(navMenuTitles[position]);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				super.onDrawerClosed(drawerView);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				super.onDrawerOpened(drawerView);
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
			}
		};
		mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
		

		/**
		 * As we are calling BaseActivity from manifest file and this base activity is intended just to add navigation drawer in our app.
		 * We have to open some activity with layout on launch. So we are checking if this BaseActivity is called first time then we are opening our first activity.
		 * */
		if(isLaunch){
			 /**
			  *Setting this flag false so that next time it will not open our first activity.
			  *We have to use this flag because we are using this BaseActivity as parent activity to our other activity. 
			  *In this case this base activity will always be call when any child activity will launch.
			  */
			isLaunch = false;
			openActivity(1);
		}
	}
	
	/**
	 * @param position
	 * 
	 * Launching activity when any list item is clicked. 
	 */
	protected void openActivity(int position) {
		
		/**
		 * We can set title & itemChecked here but as this BaseActivity is parent for other activity, 
		 * So whenever any activity is going to launch this BaseActivity is also going to be called and 
		 * it will reset this value because of initialization in onCreate method.
		 * So that we are setting this in child activity.    
		 */
//		mDrawerList.setItemChecked(position, true);
//		setTitle(listArray[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
		AdminMenuActivity.position = position; //Setting currently selected position in this field so that it will be available in our child activities. 
		
		switch (position) {
		case 0:					//Profile
			//startActivity(new Intent(this, Item1Activity.class));
			break;
		case 1:					//Home
			startActivity(new Intent(this, geofences.class));
			finish();
			break;
		case 2:					//Report
			startActivity(new Intent(this, Reporting_pg4.class));
			finish();
			break;
		case 3:					//Geofence
			startActivity(new Intent(this, GeofencesInList.class));
			finish();
			break;
		case 4:					// All Notification
			startActivity(new Intent(this, AllNotification.class));
			finish();
			break;
		case 5:					//Settings
            Intent i=new Intent(this,Usersetting.class);
            startActivity(i);
			break;
		case 6:					//About
            startActivity(new Intent(this,About.class));
            finish();

			break;
		case 7:					//Log Out
        	Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
        	SessionManager session = new SessionManager(getApplicationContext());
			session.logoutUser();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// The action bar home/up action should open or close the drawer. 
		// ActionBarDrawerToggle will take care of this.
		if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
    
    /* We can override onBackPressed method to toggle navigation drawer*/
	@Override
	public void onBackPressed() {
		if(mDrawerLayout.isDrawerOpen(mDrawerList)){
			mDrawerLayout.closeDrawer(mDrawerList);
		}else {
			finish();
			//mDrawerLayout.openDrawer(mDrawerList);
		}
	}
}
