package com.drr.wix.startup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.drr.wix.api.ApiClient;
import com.drr.wix.helper.TrackerHelper;
import com.drr.wix.tracker.Tracker;


/**
 * Created by rohitman on 11/9/2014.
 */
public class StartUpAsyncRegisterUser extends AsyncTask<String, Integer, UserInfo> {
    private Context mContext;

    public StartUpAsyncRegisterUser(Context contextToUse) {
        mContext = contextToUse;
    }

    @Override
    protected UserInfo doInBackground(String... listOfAccountNames) {
        UserInfo registeredUser;

        // TODO registeredUser Null check needs to behave differently. Currently set this way
        // because of a bug in login
        registeredUser = ApiClient.getInstance().login(listOfAccountNames[0]);
        if (registeredUser == null) {
            registeredUser = new UserInfo();
            registeredUser.setId(455l);
        }

        SharedPreferences trackerPreferences =
                mContext.getSharedPreferences(TrackerHelper.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = trackerPreferences.edit();
        editor.putLong(listOfAccountNames[0], registeredUser.getId());
        boolean committed = editor.commit();

        return registeredUser;
    }

    @Override
    protected void onPostExecute(UserInfo userInfo) {
        super.onPostExecute(userInfo);


        // All set. Start the main Tracker Activity and finish the StartUp Activity
        mContext.startActivity(new Intent(mContext, Tracker.class));

        // Todo should we override pause and call Finish there OR this is enough
        if (mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    }
}
