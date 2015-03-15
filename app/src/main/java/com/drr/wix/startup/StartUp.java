package com.drr.wix.startup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.drr.wix.AppSettingsInfo;
import com.drr.wix.R;
import com.drr.wix.api.ApiClient;
import com.drr.wix.tracker.Tracker;

public class StartUp extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_up);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new StartUpFragment()).commit();
        }

        ApiClient.createInstance(this);

        // Check to see if user has signed in before and if not store the User ID
        checkUserRegistration();

    }

    public void checkUserRegistration() {

        // Now check to see if this user already has an ID associated and if not, use the TrackerHelper
        // to async create a userID. When the Async Register process finishes, it will automatically
        // start the Tracker activity
        String thisUserID = AppSettingsInfo.getUserId(this);
        if (thisUserID == null) {
            new StartUpAsyncRegisterUser(this).execute();
        } else {
            // All set with the User ID (who has logged in before). Start the main Tracker Activity
            startActivity(new Intent(this, Tracker.class));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class StartUpFragment extends Fragment {

        public StartUpFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.f_start_up, container, false);
        }

    }

}
