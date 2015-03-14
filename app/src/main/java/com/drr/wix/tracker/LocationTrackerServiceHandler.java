package com.drr.wix.tracker;

import android.app.Service;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by rohitman on 10/29/2014.
 */
public class LocationTrackerServiceHandler extends Handler {
    public static final int START_TRACKING_LOCATION = 0707;
    public static final int STOP_TRACKING_LOCATION = 1407;

    private Service mLocationTrackingService;
    private LocationTracker mLocationTracker;

    public LocationTrackerServiceHandler(Service locationTrackingService) {
        super();
        this.mLocationTrackingService = locationTrackingService;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.i(this.getClass().getName(), "About to " + whatIsBeingProcessed(msg.what));

        // Extract the Name of the Track. All callers are expected to pass that as a Bundle in the
        // message Object
        String trackName = "";
        if (msg.getData() != null) {
            trackName = msg.getData().getString(TrackerLocationUpdatesHandler.TRACK_NAME);
            if (trackName == null) {
                Log.e(this.getClass().getName(), "Track Name cannot be null");
                throw new RuntimeException("TrackName cannot be null");
            }
        }

        // Either Create a new LocationTracker and start tracking or simply destroy it
        switch (msg.what) {
            case START_TRACKING_LOCATION:
                if (mLocationTracker == null)
                    mLocationTracker = new LocationTracker(mLocationTrackingService, msg.replyTo);
                mLocationTracker.startTrackingLocation(trackName);
                break;

            case STOP_TRACKING_LOCATION:
                if (mLocationTracker != null) {
                    mLocationTracker.stopTrackingLocation();
                    mLocationTracker = null;
                }
                break;
        }
    }

    private String whatIsBeingProcessed(int what) {
        switch (what) {
            case START_TRACKING_LOCATION:
                return "START_TRACKING_LOCATION";
            case STOP_TRACKING_LOCATION:
                return "STOP_TRACKING_LOCATION";
            default:
                // should never come here. But if it does, ERROR
                throw new RuntimeException(this.getClass().getName() + ": cannot process this message. Value = " + what);
        }
    }

}