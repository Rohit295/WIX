package com.drr.wix.tracker;

import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohitman on 3/9/2015.
 */
public class TrackerHandler extends Handler {
    private GoogleMap mMap;
    Marker mTailOfThePath, mHeadOfThePath;

    private List<LatLng> pointsOnPathTaken = new ArrayList<LatLng>(5);
    private int tempCounter = 0;
    Polyline mPathTraced;

    public TrackerHandler(GoogleMap map) {
        mMap = map;
    }

    @Override
    public void handleMessage(Message msg) {
        Location currentPosition;
        LatLng currentPositionLatLng;

        switch (msg.what) {
            case LocationTracker.LOCATION_INITIAL_POSITION:
                currentPosition = (Location)msg.obj;
                currentPositionLatLng =
                        new LatLng(currentPosition.getLatitude(), currentPosition.getLongitude());
                pointsOnPathTaken.add(currentPositionLatLng);

                // Identify the current position, at the start of tracking, which would be the last
                // position on the trail. Make a marker and add to the path polyline for later plotting
                mTailOfThePath = mMap.addMarker(new MarkerOptions().title("Start").
                        position(currentPositionLatLng));

                break;
            case LocationTracker.LOCATION_UPDATE:
                currentPosition = (Location)msg.obj;
                currentPositionLatLng =
                        new LatLng(currentPosition.getLatitude(), currentPosition.getLongitude());
                pointsOnPathTaken.add(currentPositionLatLng);

                // TODO - ensure this code below is commented
                // Test code to simulate small amounts of movement
                //currentPositionLatLng = new LatLng(currentPosition.getLatitude() + (double)(5*tempCounter)/10000,
                //                            currentPosition.getLongitude() + (double)(5*tempCounter++)/10000);

                //Now trace the path taken by the user. Draw the Polyline and set the Head Tracker
                if (mHeadOfThePath != null) {
                    mHeadOfThePath.remove();
                }
                mHeadOfThePath = mMap.addMarker(new MarkerOptions().title("Last").
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.black_car_topview)).
                        position(currentPositionLatLng).rotation(getRotation()));

                if (mPathTraced == null)
                    mPathTraced = mMap.addPolyline(new PolylineOptions().add(pointsOnPathTaken.get(0)));
                mPathTraced.setPoints(pointsOnPathTaken);
                //pointsOnPathTaken.add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));

                break;
            case LocationTracker.LOCATION_FINAL_POSITION:
                // Delete the Polyline and reset the list of lat longs
                if (mPathTraced != null) {
                    mPathTraced.remove();
                    mPathTraced = null;
                }
                pointsOnPathTaken = new ArrayList<LatLng>(5);

                if (mHeadOfThePath != null)
                    mHeadOfThePath.remove();
                if (mTailOfThePath != null)
                    mTailOfThePath.remove();

                break;
        }
    }

    /**
     * Use this to generate the alignment of the Route Marker icon
     * @return
     */
    private float getRotation() {
        // if there are no points reported, rotation is 0
        if (pointsOnPathTaken.size() < 2)
            return 0.0f;

        LatLng lp = pointsOnPathTaken.get(pointsOnPathTaken.size()-1);
        LatLng lbp = pointsOnPathTaken.get(pointsOnPathTaken.size()-2);

        // check any standard definition of how to get bearing, given 2 Lat/Long combinations
        // ATAN2(COS(lat1)*SIN(lat2)-SIN(lat1)*COS(lat2)*COS(lon2-lon1), SIN(lon2-lon1)*COS(lat2))
        // HOWEVER the implementation below did not give the correct result
        /*
        double rotation =
            Math.atan2(Math.cos(lp.latitude)*Math.sin(lbp.latitude -
                        Math.sin(lp.latitude)*Math.cos(lbp.latitude)*Math.cos(lbp.longitude-lp.longitude)),
                    Math.sin(lbp.longitude-lp.longitude)*Math.cos(lbp.latitude));
        rotation = (Math.toDegrees(rotation));// + 360) % 360;
        Log.i(this.getClass().getName(), "Calculated Rotation when going from " +
                lp.latitude + "/" + lp.longitude + " to " +
                lbp.latitude + "/" + lbp.longitude + " is " + rotation);
        */

        // Get Distance, Initial Bearing and Final Bearing using Location static methods
        float[] distanceParams = new float[3];
        Location.distanceBetween(lp.latitude, lp.longitude, lbp.latitude, lbp.longitude, distanceParams);
        Log.i(this.getClass().getName(), "Location.distanceBetween calculations " +
                "with distance=" + distanceParams[0] +
                "m; initial bearing=" + distanceParams[1] +
                "; final bearing=" + distanceParams[2] +
                "; Camera Rotation=" + mMap.getCameraPosition().bearing);

        float rotation = (distanceParams[2] - mMap.getCameraPosition().bearing)%360;
        return (float)rotation;
    }}
