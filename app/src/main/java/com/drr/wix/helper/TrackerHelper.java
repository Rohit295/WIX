package com.drr.wix.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.drr.wix.startup.StartUpAsyncRegisterUser;
import com.drr.wix.tracker.Tracker;


/**
 * Created by rohitman on 11/9/2014.
 */
public class TrackerHelper {

    private Context mContext;
    public static final String PREFERENCES_NAME = "TRACKER_PREFERENCE_NAME";

    // Create our constructors. Do not allow for TrackerHelper to be created without passing
    // in the context
    private TrackerHelper() {}
    public TrackerHelper(Context context) {
        mContext = context;
    }

    /**
     * Login the Current User and set up for track management
     */
    public void checkUserRegisteration() {
        // extract the Shared Preferences File for Tracker
        SharedPreferences trackerPreferences = mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        AccountManager manager = AccountManager.get(mContext);
        Account[] listOfAccounts = manager.getAccountsByType("com.google");
        String googleAccount = listOfAccounts[0].name;

        // Now check to see if this user already has an ID associated and if not, use the TrackerHelper
        // to async create a userID. When the Async Register process finishes, it will automatically
        // start the Tracker activity
        long thisUserID = trackerPreferences.getLong(googleAccount, -500l);
        if (thisUserID == -500l) {
            new StartUpAsyncRegisterUser(mContext).execute(googleAccount);
        } else {

            // All set with the User ID (who has logged in before). Start the main Tracker Activity
            mContext.startActivity(new Intent(mContext, Tracker.class));
        }
    }

    /**
     * get the ID of the user who is currently running the app. Since this helper needs to be
     * accessed from many places, it takes in the context to use
     * @return
     */
    public long getUserID() {

        SharedPreferences trackerPreferences = mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        AccountManager manager = AccountManager.get(mContext);
        Account[] listOfAccounts = manager.getAccountsByType("com.google");
        String googleAccount = listOfAccounts[0].name;

        // Now extract the UserID associated with the Email address. If nothing is returned,
        // consider catastrophic and exit
        long thisUserID = trackerPreferences.getLong(googleAccount, -500l);
        if (thisUserID == -500l) {
            Log.e("Tracker Activity - getUserID()", googleAccount + " is not logged into this Device");
            throw new RuntimeException(googleAccount + " is not logged into this Device");
        }
        return thisUserID;
    }
}