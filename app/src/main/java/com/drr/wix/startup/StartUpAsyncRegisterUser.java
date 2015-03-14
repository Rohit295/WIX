package com.drr.wix.startup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.drr.wix.AppSettingsInfo;
import com.drr.wix.api.ApiClient;
import com.drr.wix.tracker.Tracker;
import com.wix.common.model.RouteExecutionDTO;
import com.wix.common.model.UserDTO;

import java.util.List;


/**
 * Created by rohitman on 11/9/2014.
 */
public class StartUpAsyncRegisterUser extends AsyncTask<String, Integer, UserDTO> {

    private Context mContext;

    public StartUpAsyncRegisterUser(Context contextToUse) {
        mContext = contextToUse;
    }

    @Override
    protected UserDTO doInBackground(String... str) {

        AccountManager manager = AccountManager.get(mContext);
        Account[] listOfAccounts = manager.getAccountsByType("com.google");
        String googleAccount = listOfAccounts[0].name;

        // TODO registeredUser Null check needs to behave differently. Currently set this way
        // because of a bug in login
        UserDTO registeredUser = ApiClient.getInstance().login(googleAccount);
        if (registeredUser == null) {
            registeredUser = new UserDTO();
            registeredUser.setId("455l");
        }

        AppSettingsInfo.saveUserId(mContext, registeredUser.getId());

        List<RouteExecutionDTO> routeExecutions = ApiClient.getInstance().getAssignedRoutExecutions(registeredUser.getId());
        if (routeExecutions == null || routeExecutions.isEmpty()) {
            // TODO throw a proper error???
            throw new RuntimeException("no assigned routes");
        }

        AppSettingsInfo.saveCurrentRouteExecutionId(mContext, routeExecutions.get(0).getId());

        return registeredUser;

    }

    @Override
    protected void onPostExecute(UserDTO userInfo) {

        super.onPostExecute(userInfo);

        // All set. Start the main Tracker Activity and finish the StartUp Activity
        mContext.startActivity(new Intent(mContext, Tracker.class));

        // Todo should we override pause and call Finish there OR this is enough
        if (mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }

    }

}
