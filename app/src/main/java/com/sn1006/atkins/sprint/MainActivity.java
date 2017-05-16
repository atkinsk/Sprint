package com.sn1006.atkins.sprint;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static java.lang.Math.abs;


public class MainActivity extends FragmentActivity implements
        OnConnectionFailedListener,
        ConnectionCallbacks,
        LocationListener, Runnable {

    protected GoogleApiClient mGoogleApiClient;
    protected String mLatitudeLabel = "Lat";
    protected String mLongitudeLabel = "Long";
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected static final String TAG = "MainActivity";
    protected Location mCurrentLocation;
    protected Location mPreviousLocation;

    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    //Determine whether or not location updates will be completed by the specified intervals
    protected boolean mRequestingLocationUpdates;

    //Intervals at which the GPS location services will update. Preferred and fastest interval
    //The values are to be tweaked in the future. Seems minimum update interval is ~1 second
    public static final long UPDATE_INTERVAL_IN_MS = 750; //750
    public static final long FASTEST_UPDATE_INTERVAL_IN_MS = 500; //500

    // Keys for storing activity state in the Bundle.
//    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";

    //Request code used for requestPermissions. Must be >=0
    protected final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 23;

    //Number of GPS updates Sprint has collected
    protected double mNumberUpdates = 0;

    //set size of zone for testing waypoint arrival/departure

    protected double mZoneSize = 15; //meters
    protected boolean mIsInZone = false; //User is in the above listed radius in relation to start zone
    protected boolean mHasLeftZone = false; //User has left the start zone after triggering the timer

    protected TextView mNumberUpdatesText;
    protected TextView mZoneStatusText;
    protected TextView mTimerText;
    protected TextView mBearingToWaypointText;
    protected TextView mDistanceFromWaypointText;


    protected double mDistanceTravelled; //meters
    protected double mDistanceFromWaypoint; //meters
    protected double mWaypointBearing; //degrees
    protected double mPreviousWaypointBearing; //degrees

    //Waypoint location object
    protected Location mWaypoint = new Location("waypoint");
    //Timer object
    protected Timer t = new Timer();
    //handler for timer
    protected Handler handler = new Handler();

    //Fires when the system first creates the Main Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById(R.id.latitude);
        mLongitudeText = (TextView) findViewById(R.id.longitude);
        mNumberUpdatesText = (TextView) findViewById(R.id.gpsCounter);
        mDistanceFromWaypointText = (TextView) findViewById(R.id.distWaypoint);
        mZoneStatusText = (TextView) findViewById(R.id.zoneStatus);
        mTimerText = (TextView) findViewById(R.id.timer);
        mBearingToWaypointText = (TextView) findViewById(R.id.bearingToWaypoint);

        mRequestingLocationUpdates = false;

        updateValuesFromBundle(savedInstanceState);

        //pre-defined waypoint x and y coords for testing
        double kevX = 45.293531;
        double kevY = -75.856726;
        double jonX = 45.360282;
        double jonY = -75.750125;
        double watGlenX = 42.341040;
        double watGlenY = -76.928847;

        //create Location object for start/stop point
        mWaypoint.setLatitude(kevX);
        mWaypoint.setLongitude(kevY);

/*
        //create Location object for jon's house start/stop
        mWaypoint.setLatitude(jonX);
        mWaypoint.setLongitude(jonY);
*/

        //let's see if we can get the timer working continuously....
        //human eye can register only as fast as every 30ms... so that's how often we will update
        //use an event handler to schedule the posting of the time at delayed intervals (30ms)
        //implement runnable interface to set the text
        final Runnable updater = new Runnable() {
            @Override
            public void run() {
                if (t.getRunning()) {
                    //set text to the elapsed time managed by timer class
                    mTimerText.setText(t.getElapsedTime());
                    //update every 30 milliseconds
                }
                handler.postDelayed(this, 30);
            }
        };

        //initially post the updater to the handler
        handler.postDelayed(updater, 30);

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
//            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//                mRequestingLocationUpdates = savedInstanceState.getBoolean(
//                        KEY_REQUESTING_LOCATION_UPDATES);
//            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
//            }
            updateLocationUI();
        }
    }

    //Creates instance of Google Api Client allowing the app to connect to google location services
    protected synchronized void buildGoogleApiClient() {
        //Confirms no instance of GoogleApiClient has been instantiated
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateLocationUI();
                        break;
                }
                break;
        }
    }

    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, mLocationRequest, MainActivity.this);
                        } catch (SecurityException e) {
                            //Tech Debt: Provide better error log message
                            Log.e(TAG, "requestLocationUpdates securityException.");
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }
                updateLocationUI();
            }
        });
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    //Sets the UI values for the latitude and longitude
    protected void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                    mCurrentLocation.getLatitude()));
            mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                    mCurrentLocation.getLongitude()));
            mNumberUpdatesText.setText(String.format("%s: %f", "# Updates", mNumberUpdates));
            mDistanceFromWaypointText.setText(String.format("%s: %f", "Dist from WP", mDistanceFromWaypoint));
            mZoneStatusText.setText("IN THE ZONE? " + mIsInZone);
            mBearingToWaypointText.setText("Bearing to WP " + normalizeDegrees(mWaypointBearing));
        }
    }

    //Specification of the priority and update interval for the location request
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        //Sets the preferred interval for GPS location updates
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MS);
        //Sets the fastest interval for GPS location updates
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MS);
        //Sets priority of the GPS location to accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Used when checking user location settings
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

    }

    protected void permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        mRequestingLocationUpdates = true;
    }

    //When requestPermissions is called on in the permissionCheck method, this method is called automatically to process the user's input
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted by the user. Proceed with GPS location check
                    mRequestingLocationUpdates = true;
                } else {
                    // permission denied. Do not proceed with GPS location check
                    mRequestingLocationUpdates = false;
                    //If permission has been denied, lets the user know that the app cannot be operated
                    //without the location permissions enabled
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(MainActivity.this,
                                "Location permissions required to operate Sprint", Toast.LENGTH_LONG).show();
                    }
                }
                // Include any other permission requests after this point
            }
        }
    }

    protected void isUserInStartZone() {
        //Checks to see if the user is in the specified radius near the start / end point
        if (mDistanceFromWaypoint < mZoneSize) {
            //The user is in the zone
            mIsInZone = true;

            //Calculates the bearings of the user's current location relative to the start point
            mWaypointBearing = mCurrentLocation.bearingTo(mWaypoint);
            //When the timer is not running, start the timer
            if (!t.getRunning()) {
                t.start();
            }
            //When the timer is running the timer will be stopped if and only if the user has
            //already left the start zone and returned to it. This keeps the timer from stopping
            //if the GPS coordinates of the user are in the start zone for two GPS pings
            //Also checks to see if the user has crossed the start point via bearings delta
            if (t.getRunning() && mHasLeftZone && isUserPastStartPoint()) {
                double finishTimeMod = t.finishTimeEstimate(mCurrentLocation, mPreviousLocation);
                //finishTimeEstimate must be known before startTimeEstimate can be called
                double startTimeMod = t.startTimeEstimate();
                t.stop();
                mHasLeftZone = false;
                //Future implementation: timer will be reset here, and first lap saved
            }
        } else {
            //The user has left the zone
            mIsInZone = false;
            mHasLeftZone = true;
        }
        mPreviousWaypointBearing = mWaypointBearing;
    }

    //Determines if the user has past the start / end point
    protected boolean isUserPastStartPoint() {
        int minimumBearingDelta = 105; //degrees

        double bearingDifference = Math.abs(normalizeDegrees(mPreviousWaypointBearing)
                - normalizeDegrees(mWaypointBearing));

/*      If a large bearing difference occurs (greater than minimumBearingDelta), then the user has
        passed the start line and the timer should stop*/
        if (bearingDifference >= minimumBearingDelta) {
            return true;
        }
        //The user has not passed the start line even though they are in the start zone.
        return false;
    }


    /*   The bearing returns -180deg to 180deg rather than the standard 0 to 360 degrees
        Normalizing the value to provide a value east of true north*/
    protected double normalizeDegrees(double locationBearing) {
        if (locationBearing >= 0.0 && locationBearing <= 180) {
            return locationBearing;
        } else {
            return 180 + (180 + locationBearing);
        }
    }

    //Fires when the google play location services is connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        permissionCheck();
        if (mCurrentLocation == null) {
            try {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                Log.e(TAG, "getLastLocation securityException.");
            }
            updateLocationUI();
        }
        if (mRequestingLocationUpdates) {
            Log.i(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        //Set the previous location to the current GPS Location
        //This will allow for distance calculations between the last and current gps coordinates
        mPreviousLocation = mCurrentLocation;
        mCurrentLocation = location;
                    //This causes the app to crash during first permission checks
                   /* //Distance from current location to previous location
                    mDistanceTravelled = mCurrentLocation.distanceTo(mPreviousLocation);*/
        //Distance from current location to waypoint location
        mDistanceFromWaypoint = mCurrentLocation.distanceTo(mWaypoint);

        //Controls lap timer functionality based on location relative to the user and the waypoint
        isUserInStartZone();

        mNumberUpdates++;
        updateLocationUI();
    }

    //Connects to our google Api client onStart
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    //Disconnects google Api client upon app Stop
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        updateLocationUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void run() {
        mTimerText.setText("RUNNING");
    }
}

