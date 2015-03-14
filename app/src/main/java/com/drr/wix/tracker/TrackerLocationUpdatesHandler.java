package com.drr.wix.tracker;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.drr.wix.api.ApiClient;
import com.wix.common.model.RouteExecutionDTO;
import com.wix.common.model.RouteExecutionStatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rohitman on 11/5/2014.
 */
public class TrackerLocationUpdatesHandler extends Handler {

    public static final int CREATE_TRACKINFO = 609;
    public static final int SAVE_LOCATION_UPDATE = 158;
    public static final int CLOSE_TRACKINFO = 402;

    public static final String TRACK_NAME = "Track_Name";
    public static final String USER_ID = "User_ID";

    private Map<String, RouteExecutionDTO> mMapTrackToInfo = new HashMap<String, RouteExecutionDTO>();

    // Make the Constructor Private so that it wont be invoked and instead use the Singleton
    // retrieve
    // TODO verify that this is truly singleton across all the using Processes
    private static TrackerLocationUpdatesHandler mHandler = new TrackerLocationUpdatesHandler();

    private TrackerLocationUpdatesHandler() {
    }

    public static TrackerLocationUpdatesHandler getHandler() {
        return mHandler;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.i(this.getClass().getName(), "About to Process a location related update - " + whatIsBeingProcessed(msg.what));

        // Extract the Name of the Track & the current User ID. All callers are expected to pass
        // that as a Bundle in the message Object
        String trackName = "";
        String userID = "";
        if (msg.getData() != null) {
            trackName = msg.getData().getString(TRACK_NAME);
            userID = msg.getData().getString(USER_ID);
            if (trackName == null) {
                Log.e(this.getClass().getName(), "Track Name cannot be null");
                throw new RuntimeException("TrackName cannot be null");
            } else if (userID == null) {
                Log.e(this.getClass().getName(), "USerID cannot be 0");
                throw new RuntimeException("UserID cannot be 0");
            }
        }

        switch (msg.what) {

            case CREATE_TRACKINFO:
                new TrackerLocationCreateTrackInfoAsyncTask(this).execute(new TrackObject(trackName, userID));
                break;

            case SAVE_LOCATION_UPDATE:
                Location locationToUpdate = (Location) msg.obj;
                new TrackerLocationSaveLocationDataAsyncTask(new TrackObject(trackName, userID)).
                        execute(locationToUpdate);
                break;

            case CLOSE_TRACKINFO:
                mMapTrackToInfo.remove(trackName);
                // TODO include code to close the track on the server
                break;
        }

    }

    private String whatIsBeingProcessed(int what) {
        switch (what) {
            case CREATE_TRACKINFO:
                return "CREATE_TRACKINFO";
            case SAVE_LOCATION_UPDATE:
                return "SAVE_LOCATION_UPDATE";
            case CLOSE_TRACKINFO:
                return "CLOSE_TRACKINFO";
            default:
                // should never come here. But if it does, ERROR
                throw new RuntimeException(this.getClass().getName() + ": cannot process this message. Value = " + what);
        }
    }

    public void updateTrackInfo(String trackName, RouteExecutionDTO trackInfo) {
        mMapTrackToInfo.put(trackName, trackInfo);
    }

    /**
     * Use this Static Method to create the Name of every Track. Name is 'Track_formatted date'. Blanks
     * screw up the HTTP exchange with the server and since it is not required, convert the " " to "_"
     *
     * @return
     */
    public static String getNewTrackName() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm:ss a");
        String formattedDate = "Track_" + dateFormatter.format(Calendar.getInstance().getTime());
        return formattedDate.replace(" ", "_");
    }

    // TODO: this is a poor definition. Replace at the earliest
    class TrackObject {
        public String trackName;
        public String userID;

        public TrackObject(String trackName, String userID) {
            this.trackName = trackName;
            this.userID = userID;
        }
    }

    class TrackerLocationCreateTrackInfoAsyncTask extends AsyncTask<TrackObject, Integer, RouteExecutionDTO> {

        private String mTrackName;
        private String mUserID;
        private TrackerLocationUpdatesHandler mUpdatesHandler;

        public TrackerLocationCreateTrackInfoAsyncTask(TrackerLocationUpdatesHandler updatesHandler) {
            mUpdatesHandler = updatesHandler;
        }

        @Override
        protected RouteExecutionDTO doInBackground(TrackObject... trackObjects) {

            Log.i(this.getClass().getName(), "About to create track - " + trackObjects[0].trackName);

            mTrackName = trackObjects[0].trackName;
            mUserID = trackObjects[0].userID;

            return ApiClient.getInstance().updateRouteExecutionStatus(mUserID, mTrackName, RouteExecutionStatus.Started);
        }

        @Override
        protected void onPostExecute(RouteExecutionDTO trackInfo) {
            super.onPostExecute(trackInfo);

            mUpdatesHandler.updateTrackInfo(mTrackName, trackInfo);
        }
    }

    class TrackerLocationSaveLocationDataAsyncTask extends AsyncTask<Location, Integer, Boolean> {

        private String mTrackName;

        private String mUserID;

        public TrackerLocationSaveLocationDataAsyncTask(TrackObject trackObject) {
            mTrackName = trackObject.trackName;
            mUserID = trackObject.userID;
        }

        @Override
        protected Boolean doInBackground(Location... locations) {
            Log.i(this.getClass().getName(), "About to save this location into " +
                    mTrackName + " - " + locations[0].getLatitude() + "/" + locations[0].getLongitude());

            // It is possible that we try to update a location, even before the Track for storing these
            // locations has been created. Loop till we know the Track is created OR till 50 loops (50s)
            // is finished
            // Todo is there a better way to wait for the TrackInfo to get updated from createnewtrack??
            RouteExecutionDTO trackInfo = mMapTrackToInfo.get(mTrackName);
            int tryCounter;
            for (tryCounter = 0; ((trackInfo == null) && (tryCounter < 50)); tryCounter++) {
                try {
                    Thread.sleep(1000L);
                    trackInfo = mMapTrackToInfo.get(mTrackName);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(this.getClass().getName() + " - Sleep Interrupted");
                }
            }
            if (tryCounter < 50) {
                ApiClient.getInstance().saveLocation(mUserID, trackInfo.getId(), Calendar.getInstance().getTimeInMillis(),
                        locations[0].getLatitude(), locations[0].getLongitude());
                return true;
            } else {
                throw new RuntimeException(this.getClass().getName() + " TrackInfo for " + mTrackName +
                        " still not updated");
            }
        }
    }
}
