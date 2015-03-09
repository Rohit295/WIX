package com.drr.wix.tracker;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import com.drr.wix.helper.TrackerHelper;

/**
 * Created by rohitman on 10/29/2014.
 */
public class LocationTracker implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener,
        com.google.android.gms.location.LocationListener {

    private Context mLocationTrackingContext;
    private Messenger mLocationUpdatesMessenger;
    private Messenger mTrackerLocationUpdatesMessenger;

    private String mTrackBeingCreated;
    private long mTrackBeingCreatedForUser;

    private LocationClient mLocationClient;

    private final float INITIAL_ZOOM_LEVEL = 15f;

    // created some constants to handle the Message 'whats'
    public static final int LOCATION_INITIAL_POSITION = 106;
    public static final int LOCATION_UPDATE = 317;
    public static final int LOCATION_FINAL_POSITION = 174;

    private List<LatLng> routeToTraverse;

    private boolean mLocationConnectionInitializationDone = false;

    /**
     *
     * @param locationTrackingContext
     */
    public LocationTracker(Context locationTrackingContext, Messenger locationUpdatesMessenger) {
        Log.i(this.getClass().getName(), "LocationTracker Created");
        mLocationTrackingContext = locationTrackingContext;

        // Create
        //  1. Messenger to give location updates to Client &
        //  2. Messenger to give the updates to the backend DB
        mLocationUpdatesMessenger = locationUpdatesMessenger;
        mTrackerLocationUpdatesMessenger = new Messenger(TrackerLocationUpdatesHandler.getHandler());

        // Get the User ID and set it for later use
        mTrackBeingCreatedForUser = new TrackerHelper(mLocationTrackingContext).getUserID();

        //TODO check that Google play services is available to the client
    }

    // TODO - Tracking needs to be done inside a Thread to ensure that this does not block the main UI
    public void startTrackingLocation(String trackName) {
        Log.i(this.getClass().getName(), ": about to start tracking location");
        Log.i(this.getClass().getName(), ": Track Name is  - " + trackName);

        mTrackBeingCreated = trackName;
        mLocationClient = new LocationClient(mLocationTrackingContext, this, this);
        mLocationClient.connect();
    }

    public void stopTrackingLocation() {
        Log.i(this.getClass().getName() + ": stopTrackingLocation", ": about to stop tracking location");
        endLocationUpdate();
        if (mLocationClient != null) {
            if (mLocationClient.isConnected()) {
                mLocationClient.removeLocationUpdates(this);
            }
            mLocationClient.disconnect();
        }
    }

    /**
     * Called as soon as Tracker is able to connect to the Location Management client
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(this.getClass().getName(), "Connection Made");

        // Rest of this section is required only when Connection is Initialized for first time
        if (!mLocationConnectionInitializationDone) {
            mLocationConnectionInitializationDone = true;
        } else {
            return;
        }

        // Get the last location, which in this case will be the initial location from where reporting starts
        Location location = mLocationClient.getLastLocation();
        Log.i(this.getClass().getName(), "Initial Position - Latitude: " + location.getLatitude() + "; Longitude: " +
                location.getLongitude() + "; Altitude: " + location.getAltitude());

        // send the location back to the client
        sendLocationUpdate(location, LocationTracker.LOCATION_INITIAL_POSITION);

        // Enable this drr.com.glocal.tracker to keep getting location updates
        // TODO: currently hard coded to high accuracy. How to handle coarse location updates?
        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        mLocationClient.requestLocationUpdates(request, this);
    }

    private void sendLocationUpdate(Location location, int typeOfUpdate) {
        Log.i(this.getClass().getName() + ": sendLocationUpdate", ": about to send location update for"
            + location.getLatitude() + "/" + location.getLongitude());

        try {
            // 1. send the location back to the client so that the UI is updated
            Message messageToSend = Message.obtain(null, typeOfUpdate, 0, 0);
            messageToSend.obj = location;
            mLocationUpdatesMessenger.send(messageToSend);

            // 2. send it to the DB and save as part of the current track
            Message updateLocation = Message.obtain(null, TrackerLocationUpdatesHandler.SAVE_LOCATION_UPDATE);
            Bundle dataBundle = new Bundle();
            dataBundle.putString(TrackerLocationUpdatesHandler.TRACK_NAME, mTrackBeingCreated);
            dataBundle.putLong(TrackerLocationUpdatesHandler.USER_ID, mTrackBeingCreatedForUser);
            updateLocation.setData(dataBundle);
            updateLocation.obj = location;
            mTrackerLocationUpdatesMessenger.send(updateLocation);
        } catch (RemoteException re) {
            Log.e(this.getClass().getName(), re.getMessage());
            throw new RuntimeException(re);
        }
    }

    private void endLocationUpdate() {
        Log.i(this.getClass().getName() + ": endLocationUpdate", "End Location updates");

        try {
            // 1. send the location back to the client so that the UI is updated
            Message messageToSend = Message.obtain(null,LocationTracker.LOCATION_FINAL_POSITION);
            mLocationUpdatesMessenger.send(messageToSend);

            // 2. send it to the DB and close the current track
            Message updateLocation = Message.obtain(null, TrackerLocationUpdatesHandler.CLOSE_TRACKINFO);
            Bundle dataBundle = new Bundle();
            dataBundle.putString(TrackerLocationUpdatesHandler.TRACK_NAME, mTrackBeingCreated);
            dataBundle.putLong(TrackerLocationUpdatesHandler.USER_ID, mTrackBeingCreatedForUser);
            updateLocation.setData(dataBundle);
            mTrackerLocationUpdatesMessenger.send(updateLocation);
        } catch (RemoteException re) {
            Log.e(this.getClass().getName(), re.getMessage());
            throw new RuntimeException(re);
        }

        // TODO remove this reset. Required because we fake location updates
        tempCounter = 0;
    }

    @Override
    public void onDisconnected() {
        Log.i(this.getClass().getName(), "Connection Made");
    }

    private int tempCounter = 0;
    private List<LatLng> pointsOnPathTaken = new ArrayList<LatLng>(5);

    @Override
    public void onLocationChanged(Location location) {
        Log.i(this.getClass().getName(), "LocationUpdate - Latitude: " + location.getLatitude() + "; Longitude: " +
                location.getLongitude() + "; Altitude: " + location.getAltitude());

        // TODO- test code to simulate movement. check that this is commented out
        location.setLatitude(location.getLatitude() + (double)(5*tempCounter)/10000);
        location.setLongitude(location.getLongitude() + (double)(5*tempCounter++)/10000);

        // send the location back to the client
        sendLocationUpdate(location, LocationTracker.LOCATION_UPDATE);

        LatLng locationToAdd = new LatLng(location.getLatitude(), location.getLongitude());
        pointsOnPathTaken.add(locationToAdd);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
