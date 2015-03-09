package com.drr.wix.tracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.drr.wix.R;
import com.drr.wix.helper.TrackerHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class Tracker extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker2);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new TrackerFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TrackerFragment extends Fragment implements View.OnClickListener {

        private final float INITIAL_ZOOM_LEVEL = 15f;

        // create a Service Connection to send messages to the Service. The Messenger extracted here
        // is the Client Side handle that will allow this activity to control the Service
        private Messenger mLocationTrackerServiceMessenger;
        private ServiceConnection mServiceConnection =
                new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        Log.i(this.getClass().getName() + ": ServiceConnection.onServiceConnected()",
                                "ServiceConnection Connected");
                        mLocationTrackerServiceMessenger = new Messenger(iBinder);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        Log.i(this.getClass().getName() + ": ServiceConnection.onServiceDisconnected()",
                                "ServiceConnection DisConnected");
                        mLocationTrackerServiceMessenger = null;
                    }
                };

        private Messenger mTrackerLocationUpdatesMessenger;
        private String mTrackName;

        @Override
        public void onStart() {
            super.onStart();

            Log.i(this.getClass().getName()  + ":onStart()", "OnStart called, about to Bind");
            getActivity().bindService(new Intent(getActivity(), LocationTrackerService.class),
                                            mServiceConnection, BIND_AUTO_CREATE);
            Log.i(this.getClass().getName()  + ":onStart()", "OnStart finished, Binding Initiated");
        }

        @Override
        public void onStop() {
            super.onStop();

            Log.i(this.getClass().getName() + ":onStop()", "OnStop called, about to UnBind");
            if (mLocationTrackerServiceMessenger != null)
                getActivity().unbindService(mServiceConnection);
            Log.i(this.getClass().getName() + ":onStop()", "OnStop finished, UnBind finished");
        }

        // create a client side Messenger to receive messages from the Service
        private Messenger mLocationUpdatesMessenger;
        private final String MAP_FRAGMENT_ID = "Interim_Map_Fragment";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.f_tracker_2, container, false);

            // Initialize our lovely Google Map View & give it its initial settings. Logic is to
            // initialize a MapFragment and add it to our container layout
            GoogleMapOptions mapOptions = new GoogleMapOptions();
            CameraPosition cp = CameraPosition.builder().target(new LatLng(17.4321496, 78.3612867)).
                    zoom(INITIAL_ZOOM_LEVEL).build();
            mapOptions.camera(cp).mapType(GoogleMap.MAP_TYPE_NORMAL).tiltGesturesEnabled(false);
            MapFragment mapFragment = MapFragment.newInstance(mapOptions);

            // However just before adding, check the height of the screen and fill best height possible
            DisplayMetrics  dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            LinearLayout mapViewContainer = (LinearLayout)rootView.findViewById(R.id.f_cont_map_fragment);
            getFragmentManager().beginTransaction().add(mapViewContainer.getId(), mapFragment,
                    MAP_FRAGMENT_ID).commit();
            mapViewContainer.getLayoutParams().height = dm.heightPixels - 450;

            // Add a Button listener to start & stop location tracking
            Button startButton = (Button) rootView.findViewById(R.id.btn_start_tracking);
            startButton.setOnClickListener(this);
            Button stopButton = (Button) rootView.findViewById(R.id.btn_stop_tracking);
            stopButton.setOnClickListener(this);

            return rootView;
        }

        /**
         * Button Click Handlers: Handle the Start and Stop Buttons
         * @param view
         */
        @Override
        public void onClick(View view) {
            // At this point create the incoming message Handler and set it up with the Map it is going
            // to manipulate
            if (mLocationUpdatesMessenger == null) {
                MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentByTag(MAP_FRAGMENT_ID);
                mLocationUpdatesMessenger = new Messenger(new TrackerHandler(mapFragment.getMap()));
            }

            // also create the Message Handler that will take care of the real updates to backend
            mTrackerLocationUpdatesMessenger = new Messenger(TrackerLocationUpdatesHandler.getHandler());

            // If the user has pressed the Start Button
            Intent locationTrackingIntent = new Intent(getActivity(), LocationTrackerService.class);
            Bundle dataBundle = new Bundle();
            if (view.getId() == R.id.btn_start_tracking) {

                // 1. Create a new TrackInfo to track all the location updates. In that include the
                // TrackName & UserID
                mTrackName = TrackerLocationUpdatesHandler.getNewTrackName();
                Message createTrackingInfo = Message.obtain(null, TrackerLocationUpdatesHandler.CREATE_TRACKINFO);
                dataBundle.putString(TrackerLocationUpdatesHandler.TRACK_NAME, mTrackName);

                Long userID = new TrackerHelper(getActivity()).getUserID();
                dataBundle.putLong(TrackerLocationUpdatesHandler.USER_ID, userID);
                createTrackingInfo.setData(dataBundle);

                try {
                    mTrackerLocationUpdatesMessenger.send(createTrackingInfo);
                } catch(RemoteException re) {
                    Log.e(this.getClass().getName() + ":Error @ creation of TrackInfo", re.getMessage());
                    throw new RuntimeException(re);
                }

                // 2. Send a message to the LocationService to start and pass along the track name to
                // use for the location updates
                Message startTracking = Message.obtain(null, LocationTrackerServiceHandler.START_TRACKING_LOCATION);
                startTracking.replyTo = mLocationUpdatesMessenger;
                startTracking.setData(dataBundle);
                try {
                    mLocationTrackerServiceMessenger.send(startTracking);
                } catch(RemoteException re) {
                    Log.e(this.getClass().getName() + ":Error @ start of Tracking", re.getMessage());
                    throw new RuntimeException(re);
                }

                // Now that start is processed, enable the Stop button and disable the Start button
                ((Button) getView().findViewById(R.id.btn_stop_tracking)).setEnabled(true);
                view.setEnabled(false);
            } else if (view.getId() == R.id.btn_stop_tracking) {

                Message stopTracking = Message.obtain(null, LocationTrackerServiceHandler.STOP_TRACKING_LOCATION);
                stopTracking.replyTo = mLocationUpdatesMessenger;
                dataBundle.putString(TrackerLocationUpdatesHandler.TRACK_NAME, mTrackName);
                stopTracking.setData(dataBundle);
                try {
                    mLocationTrackerServiceMessenger.send(stopTracking);
                } catch(RemoteException re) {
                    Log.e(this.getClass().getName() + ":Error @ stop Tracking", re.getMessage());
                    throw new RuntimeException(re);
                }

                // Now that stop is processed, enable the Start button and disable the Stop button
                ((Button) getView().findViewById(R.id.btn_start_tracking)).setEnabled(true);
                view.setEnabled(false);
            }
        }
    }
}