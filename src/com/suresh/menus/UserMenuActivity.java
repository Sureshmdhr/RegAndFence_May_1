package com.suresh.menus;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.suresh.extras.SessionManager;
import com.suresh.form.R;

public class UserMenuActivity extends Activity
{
    /* Initiating Menu XML file (menu.xml) */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.usermenu, menu);
        return true;
    }
    
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.menu_setting:
            Intent i=new Intent(getApplicationContext(),Usersetting.class);
            startActivity(i);
            return true;
        case R.id.menu_logout:
        	Toast.makeText(UserMenuActivity.this, "Logging Out", Toast.LENGTH_SHORT).show();
        	SessionManager session = new SessionManager(getApplicationContext());
			session.logoutUser();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}