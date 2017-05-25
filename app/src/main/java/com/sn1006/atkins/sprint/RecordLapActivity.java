package com.sn1006.atkins.sprint;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
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

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;
import com.sn1006.atkins.sprint.data.TrackData;

import android.support.v7.app.AppCompatActivity;


public class RecordLapActivity extends AppCompatActivity implements
        OnConnectionFailedListener,
        ConnectionCallbacks,
        LocationListener,
        Runnable,
        SharedPreferences.OnSharedPreferenceChangeListener {


    protected GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "MainActivity";

    protected SQLiteDatabase mDb;

    //Testing Views / Strings
    protected String mLatitudeLabel = "Lat";
    protected String mLongitudeLabel = "Long";
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected TextView mNumberUpdatesText;
    protected TextView mZoneStatusText;
    protected TextView mBearingToWaypointText;
    protected TextView mDistanceFromWaypointText;
    protected TextView mLaptimesText;
    protected double mNumberUpdates = 0;

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

    //set size of zone for testing waypoint arrival/departure
    protected double mZoneSize = 60; //meters
    protected boolean mIsInZone = false; //User is in the above listed radius in relation to start zone
    protected boolean mHasLeftZone = false; //User has left the start zone after triggering the timer


    protected TextView mTimerText;

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

    //create a session object to store laptimes ------- TO BE MODIFIED IN FUTURE WHEN MULTIPLE SESSIONS EXIST
    Session mySession = new Session();

    //variables to be loaded from shared preferences
    protected String driverName;
    protected String track;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_record);

        //load shared preferences (driver name and track)
        setupSharedPreferences();

        //now that we have the shared preferences, apply them to the session created
        mySession.setDriver(driverName);
        mySession.setTrack(track);

        mLatitudeText = (TextView) findViewById(R.id.latitude);
        mLongitudeText = (TextView) findViewById(R.id.longitude);
        mNumberUpdatesText = (TextView) findViewById(R.id.gpsCounter);
        mDistanceFromWaypointText = (TextView) findViewById(R.id.distWaypoint);
        mZoneStatusText = (TextView) findViewById(R.id.zoneStatus);
        mTimerText = (TextView) findViewById(R.id.timer);
        mBearingToWaypointText = (TextView) findViewById(R.id.bearingToWaypoint);

        //AGAIN, THIS IS A TEMP TEXT VIEW TO BE REMOVED ONCE LAPTIMES HAS ITS OWN ACTIVITY
        mLaptimesText = (TextView) findViewById(R.id.showLaptimes);

        mRequestingLocationUpdates = false;

        updateValuesFromBundle(savedInstanceState);

        //pre-defined waypoint x and y coords for testing
        double kevX = 45.293715;
        double kevY = -75.856780;
        double jonX = 45.360282;
        double jonY = -75.750125;
        double watGlenX = 42.341043;
        double watGlenY = -76.928892;

        //create Location object for start/stop point
        mWaypoint.setLatitude(TrackData.getLat(track));
        mWaypoint.setLongitude(TrackData.getLon(track));

        SessionDbHelper dbHelper = new SessionDbHelper(this);
        mDb = dbHelper.getReadableDatabase();

        //implement continuously updating timer
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

    private void setupSharedPreferences() {
        //get all the values from the SharedPreferences to use in the session
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //set the driver name and track variables using methods below, which are also called from onSharedPreferencesChanged
        setDriver(sharedPreferences);
        setTrack(sharedPreferences);
    }

    private void setDriver(SharedPreferences sharedPreferences) {
        driverName = sharedPreferences.getString(getString(R.string.pref_driver_key),
                getString(R.string.pref_driver_default));
    }

    private void setTrack(SharedPreferences sharedPreferences) {
        track = sharedPreferences.getString(getString(R.string.pref_track_key),
                getString(R.string.pref_track_default));
    }

    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_driver_key))) {
            setDriver(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_track_key))) {
            setTrack(sharedPreferences);
        }
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
                                    mGoogleApiClient, mLocationRequest, RecordLapActivity.this);
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
                            status.startResolutionForResult(RecordLapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(RecordLapActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(RecordLapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(RecordLapActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(RecordLapActivity.this,
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
                    if (ActivityCompat.shouldShowRequestPermissionRationale(RecordLapActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(RecordLapActivity.this,
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
            mPreviousWaypointBearing = mPreviousLocation.bearingTo(mWaypoint);
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
                t.stop();
                mHasLeftZone = false;
                //Lap done, record it!
                /*--------------------------------------------------------------------------
                ** CURRENTLY RECORDING LAPTIMES IN A SESSION CREATED IN ONCREATE()
                ** IN FUTURE THERE WILL BE MULTIPLE SESSIONS...
                ** SO WE WILL HAVE AN OBJECT THAT HOLDS A LIST OF SESSIONS (HASHMAP?)
                ** AND WILL HAVE TO ADD IT TO THE PROPER SESSION
                **--------------------------------------------------------------------------
                 */
                mySession.addLap(t.getLaptime());
                //update laptimes textview with a list of the session's laptimes
                mLaptimesText.setText(mySession.toString());
            }
        } else {
            //The user has left the zone
            mIsInZone = false;
            mHasLeftZone = true;
        }
    }

    //Determines if the user has past the start / end point
    protected boolean isUserPastStartPoint() {
        int minimumBearingDelta = 70; //degrees

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


    //"Stop Session" Button. Changes activity to LapListActivity for the current session. Saves
    //the current session to the database if a lap exists. Returns to main menu if no lap exists
    protected void viewLapTimes(View view) {
        //Checks if a lap exists for the current recording
        if(!mySession.getLaptimesAsString().equals("")) {
            //Adds the new session to the database
            addNewSession();
            //takes user to laplist
            Context context = this;
            Class destinationClass = LapListActivity.class;
            Intent intentToStartDetailActivity = new Intent(context, destinationClass);
            startActivity(intentToStartDetailActivity);
        } else {
            //Brings user to sessionListActivity and returns a toast to say no laps were recorded
            //and nothing has been saved to the database
            returnToSessionList();

            Toast toast = Toast.makeText(this, "Session not saved - No laps recorded", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    //Adds session to the local SQL database
    private long addNewSession() {
        ContentValues cv = new ContentValues();

        cv.put(SessionContract.SessionEntry.COLUMN_TRACKNAME, mySession.getTrackName());
        cv.put(SessionContract.SessionEntry.COLUMN_DRIVER, mySession.getDriver());
        cv.put(SessionContract.SessionEntry.COLUMN_BESTLAP, mySession.getBestLap());
        cv.put(SessionContract.SessionEntry.COLUMN_LAPTIMES, mySession.getLaptimesAsString());
        cv.put(SessionContract.SessionEntry.COLUMN_NUMBEROFLAPS, mySession.getNumberOfLaps());

        //insert query
        return mDb.insert(SessionContract.SessionEntry.TABLE_NAME, null, cv);
    }

    //Intent to return the user to the SessionlistActivity
    public void returnToSessionList() {
        Context context = this;
        Class destinationClass = SessionListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }
}