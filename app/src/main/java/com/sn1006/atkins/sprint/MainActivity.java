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
    protected String mLatitudeLabel = "x";
    protected String mLongitudeLabel = "y";
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

    //GPS Location averaging counter and calculation variables
    protected double mAvgGpsCounter = 0;
    protected double mBaseLatitude = 0;
    protected double mBaseLongitude = 0;
    protected double mCurrentLatitude = 0;
    protected double mCurrentLongitude = 0;
    protected double mLatitudeDiff = 0;
    protected double mLongitudeDiff = 0;
    protected double mLatitudeSum = 0;
    protected double mLongitudeSum = 0;
    protected double mLatitudeAverage = 0;
    protected double mLongitudeAverage = 0;
    protected double mLatitudeSumDiff = 0;
    protected double mLongitudeSumDiff = 0;
    protected double mLatitudeAvgDiff = 0;
    protected double mLongitudeAvgDiff = 0;
    protected double mLatitudeMaxDiff = 0;
    protected double mLongitudeMaxDiff = 0;
    //set size of zone for testing waypoint arrival/departure
    protected double mZoneSize = 5.0; //meters
    protected boolean mIsInZone = false;
    protected double mWaypointBearing = 0.0; //degrees
    protected boolean mHasLeftZone = false;

    protected TextView mLatitudeDiffText;
    protected TextView mLongitudeDiffText;
    protected TextView mLatMaxDiffText;
    protected TextView mLongMaxDiffText;
    protected TextView mLatAvgDiffText;
    protected TextView mLongAvgDiffText;
    protected TextView mLatAvgText;
    protected TextView mLongAvgText;
    protected TextView mGpsCounterText;
    protected TextView mZoneStatusText;
    protected TextView mTimerText;
    protected TextView mBearingToWaypointText;

    protected DistanceCalc distanceCalc = new DistanceCalc();
    protected double mDistanceTravelled;
    protected double mDistanceFromWaypoint;
    protected TextView mDistanceTravelledText;
    Location mWaypoint = new Location("waypoint");
    protected TextView mDistanceFromWaypointText;

    //timer
    Timer t = new Timer();
    //handler for timer
    Handler handler;

    //Fires when the system first creates the Main Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById(R.id.latitude);
        mLongitudeText = (TextView) findViewById(R.id.longitude);
        mLatitudeDiffText = (TextView) findViewById(R.id.latitudeDifference);
        mLongitudeDiffText = (TextView) findViewById(R.id.longitudeDifference);
        mLatMaxDiffText = (TextView) findViewById(R.id.latMaxDiff);
        mLongMaxDiffText = (TextView) findViewById(R.id.LongMaxDiff);
        mLatAvgDiffText = (TextView) findViewById(R.id.LatAvgDiff);
        mLongAvgDiffText = (TextView) findViewById(R.id.LongAvgDiff);
        mLatAvgText = (TextView) findViewById(R.id.AvgLat);
        mLongAvgText = (TextView) findViewById(R.id.AvgLong);
        mGpsCounterText = (TextView) findViewById(R.id.gpsCounter);
        mDistanceTravelledText = (TextView) findViewById(R.id.distTravelled);
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

        //create Location object for start/stop point
        //mWaypoint.setLatitude(kevX);
        //mWaypoint.setLongitude(kevY);

        //creat Location object for jon's house start/stop
        mWaypoint.setLatitude(jonX);
        mWaypoint.setLongitude(jonY);

        //let's see if we can get the timer working continuously....
        //human eye can register only as fast as every 30ms... so that's how often we will update
        //use an event handler to schedule the posting of the time at delayed intervals (30ms)
        //implement runnable interface to set the text
        handler = new Handler();

        final Runnable updater = new Runnable() {
            @Override
            public void run() {
                if (t.getRunning()) {
                    //set text to the elapsed time managed by timer class
                    mTimerText.setText(t.getElapsedTime());
                    //update every 30 milliseconds
                }
                handler.postDelayed(this,30);
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
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
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
            public void onResult(@NonNull Status status) {
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

            mLatitudeDiffText.setText(String.format("%s: %f", mLatitudeLabel, mLatitudeDiff));
            mLongitudeDiffText.setText(String.format("%s: %f", mLongitudeLabel, mLongitudeDiff));
            mLatMaxDiffText.setText(String.format("%s: %f", mLatitudeLabel, mLatitudeMaxDiff));
            mLongMaxDiffText.setText(String.format("%s: %f", mLatitudeLabel, mLongitudeMaxDiff));
            mLatAvgDiffText.setText(String.format("%s: %f", mLatitudeLabel, mLatitudeAvgDiff));
            mLongAvgDiffText.setText(String.format("%s: %f", mLatitudeLabel, mLongitudeAvgDiff));
            mLatAvgText.setText((String.format("%s: %f", mLatitudeLabel, mLatitudeAverage)));
            mLongAvgText.setText(String.format("%s: %f", mLatitudeLabel, mLongitudeAverage));
            mGpsCounterText.setText(String.format("%s: %f", mLatitudeLabel, mAvgGpsCounter));
            mDistanceTravelledText.setText(String.format("%s: %f", mLatitudeLabel, mDistanceTravelled));
            mDistanceFromWaypointText.setText(String.format("%s: %f", mLatitudeLabel, mDistanceFromWaypoint));
            mZoneStatusText.setText("IN THE ZONE? " + mIsInZone);
            mBearingToWaypointText.setText("Bearing to WP: " + mWaypointBearing);
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
            //If permission has been denied, lets the user know that the app cannot be operated
            //without the location permissions enabled
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(MainActivity.this,
                        "Location permissions required to operate Sprint", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
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
                }
            }
            // Include any other permission requests after this point
        }
    }

    public void locationCoordinateDifference() {
        //Increment counter to ignore initial GPS location
        if (mAvgGpsCounter <= 10) {
            mAvgGpsCounter++;
            return;
        }

        //On the eleventh GPS coordinate, set the base values for latitude and longitude calculations
        if (mAvgGpsCounter == 11) {
            //Pull baseline latitude and longitude from location services
            mBaseLatitude = mCurrentLocation.getLatitude();
            mBaseLongitude = mCurrentLocation.getLongitude();
            //Pull baseline latitude and longitude for average calulation
            mLatitudeSum += mBaseLatitude;
            mLongitudeSum += mBaseLongitude;

            mAvgGpsCounter++;
            return;
        }
        //On each subsequent GPS coordinate, calculate the difference to the base
        if (mAvgGpsCounter > 11) {
            //Pull current latitude and longitude from location services
            mCurrentLatitude = mCurrentLocation.getLatitude();
            mCurrentLongitude = mCurrentLocation.getLongitude();

            //Calculate latitude and longitude differences from baselinepo[/.
            mLatitudeDiff = mBaseLatitude - mCurrentLatitude;
            mLongitudeDiff = mBaseLongitude - mCurrentLongitude;

            //Calculation of Average latitude and longitude
            mLatitudeSum += mCurrentLatitude;
            mLongitudeSum += mCurrentLongitude;
            mLatitudeAverage = mLatitudeSum / (mAvgGpsCounter - 10);
            mLongitudeAverage = mLongitudeSum / (mAvgGpsCounter - 10);

            //Calculation of Average latitude and longitude differences from initial value
            mLatitudeSumDiff += mLatitudeDiff;
            mLongitudeSumDiff += mLongitudeDiff;
            mLatitudeAvgDiff = mLatitudeSumDiff / (mAvgGpsCounter - 10);
            mLongitudeAvgDiff = mLongitudeSumDiff / (mAvgGpsCounter - 10);

            //Determine max deviation from baseline latitude
            if (abs(mLatitudeMaxDiff) < abs(mLatitudeDiff)) {
                mLatitudeMaxDiff = mLatitudeDiff;
            }

            //Determine max deviation from baseline longitude
            if (abs(mLongitudeMaxDiff) < abs(mLongitudeDiff)) {
                mLongitudeMaxDiff = mLongitudeDiff;
            }

            mAvgGpsCounter++;
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
            //is this second permissionCheck necessary? Investigate
            permissionCheck();
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
        //When at least one instance of the GPS coordinates exist, set the previous location
        //This will allow for distance calculations between the last and current gps coordinates
        mPreviousLocation = mCurrentLocation;
        mCurrentLocation = location;
        mDistanceTravelled = mCurrentLocation.distanceTo(mPreviousLocation);
        mDistanceFromWaypoint = mCurrentLocation.distanceTo(mWaypoint);

        if (mDistanceFromWaypoint < mZoneSize) {
            //we are in the zone
            mIsInZone = true;
            mWaypointBearing = distanceCalc.getDegreesToWaypoint(mCurrentLocation, mWaypoint);

            if(!t.getRunning()){
                t.start();
            }

            if(t.getRunning() && mHasLeftZone){
                t.stop();
                mHasLeftZone = false;
                //timer will be reset here, and first lap saved
            }

        } else {
            mIsInZone = false;
            mHasLeftZone = true;
        }

        locationCoordinateDifference();
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

