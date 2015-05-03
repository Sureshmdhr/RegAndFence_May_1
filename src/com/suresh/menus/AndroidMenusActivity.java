package com.suresh.menus;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.suresh.extras.SessionManager;
import com.suresh.form.R;

public class AndroidMenusActivity extends Activity {
  
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        
        switch (item.getItemId())
        {
        case R.id.menu_logout:
        	Toast.makeText(AndroidMenusActivity.this, "Logging Out", Toast.LENGTH_SHORT).show();
        	SessionManager session = new SessionManager(getApplicationContext());
			session.logoutUser();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    

}