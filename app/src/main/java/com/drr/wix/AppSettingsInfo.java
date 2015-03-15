package com.drr.wix;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by racastur on 14-03-2015.
 */
public class AppSettingsInfo {

    private static final String USER_ID_KEY = "userId";

    private static final String CURRENT_ROUTE_EXECUTION_ID_KEY = "currentRouteExecutionId";

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(USER_ID_KEY, null);
    }

    public static void saveUserId(Context context, String userId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(USER_ID_KEY, userId);
        editor.apply();
    }

    public static String getCurrentRouteExecutionId(Context context) {
        return getSharedPreferences(context).getString(CURRENT_ROUTE_EXECUTION_ID_KEY, null);
    }

    public static void saveCurrentRouteExecutionId(Context context, String currentRouteExecutionId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(CURRENT_ROUTE_EXECUTION_ID_KEY, currentRouteExecutionId);
        editor.apply();
    }

}
