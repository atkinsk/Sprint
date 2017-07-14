package com.sn1006.atkins.sprint;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.sn1006.atkins.sprint.sync.RecordLapTasks;
import com.sn1006.atkins.sprint.sync.RecordingIntentService;

import android.support.v7.app.AppCompatActivity;

public class RecordLapActivity extends AppCompatActivity implements
        OnConnectionFailedListener,
        ConnectionCallbacks,
        LocationListener,
        Runnable,
        SharedPreferences.OnSharedPreferenceChangeListener {


    protected GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "MainActivity";

    protected TextView mDistanceFromWaypointText;
    protected TextView mCurrentLapTimeText;
    protected TextView mPreviousLapTimeText;
    protected TextView mBestLapTimeText;
    protected TextView mCurrentTrackText;
    protected TextView mNumberUpdates;
    protected int mNum ;

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
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";

    //Request code used for requestPermissions. Must be >=0
    protected final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 23;

    //set size of zone for testing waypoint arrival/departure
    protected double mZoneSize = 150; //meters
    protected boolean mIsInZone = false; //User is in the above listed radius in relation to start zone
    protected boolean mHasLeftZone = false; //User has left the start zone after triggering the timer

    protected double mDistanceFromWaypoint; //meters
    protected double mWaypointBearing; //degrees
    protected double mPreviousWaypointBearing; //degrees
    protected long mStartTimeMod;//ms

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

    //Saved instance state keys
    private static final String LAPTIMES_TEXT_KEY = "laptimes";
    private static final String STARTTIME_TEXT_KEY = "callbackstimer";
    private static final String BESTLAP_TEXT_KEY = "bestlaptime";

    private static final int mRecordingNotificationID = 911;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_record);

        //load shared preferences (driver name and track)
        setupSharedPreferences();

        //now that we have the shared preferences, apply them to the session created
        mySession.setDriver(driverName);
        mySession.setTrack(track);

        /* COMMENT OUT mZoneStatusText AND mDistanceFromWaypointText FOR PRODUCTION **
        *  NOTE: ALSO COMMENT OUT SETTING THESE TEXT FIELDS IN updateLocationUI()   */

        //mZoneStatusText = (TextView) findViewById(R.id.zoneStatus);
        //mDistanceFromWaypointText = (TextView) findViewById(R.id.distWaypoint);
        //mNumberUpdates = (TextView) findViewById(R.id.numUpdates);

        //Production UI Layout
        mCurrentLapTimeText = (TextView) findViewById(R.id.currentLapTime);
        mPreviousLapTimeText= (TextView) findViewById(R.id.previousLapTime);
        mBestLapTimeText = (TextView) findViewById(R.id.bestLapTime);
        mCurrentTrackText = (TextView) findViewById(R.id.trackName);

        mCurrentTrackText.setText(mySession.getTrackName());

        mRequestingLocationUpdates = false;

        updateValuesFromBundle(savedInstanceState);

        //create Location object for start/stop point
        mWaypoint.setLatitude(TrackData.getLat(track));
        mWaypoint.setLongitude(TrackData.getLon(track));

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey(LAPTIMES_TEXT_KEY)) {
                mySession.convertStringToArray(savedInstanceState.getString(LAPTIMES_TEXT_KEY));
                mPreviousLapTimeText.setText(mySession.formatLaptime(mySession.getLastLapLong()));
                mySession.setBestLap(savedInstanceState.getString(BESTLAP_TEXT_KEY));
                mBestLapTimeText.setText(mySession.formatLaptime(mySession.getBestLapLong()));
            }
            if (savedInstanceState.containsKey(STARTTIME_TEXT_KEY)) {
                t.start();
                t.setStartTime(savedInstanceState.getLong(STARTTIME_TEXT_KEY));
                mCurrentLapTimeText.setText(t.getElapsedTime());
                handler.postDelayed(updater, 30);
            }
        }
    }

    //implement continuously updating timer
    //human eye can register only as fast as every 30ms... so that's how often we will update
    //use an event handler to schedule the posting of the time at delayed intervals (30ms)
    //implement runnable interface to set the text

    Runnable updater = new Runnable() {

        @Override
        public void run() {
            if (t.getRunning()) {
                //set text to the elapsed time managed by timer class
                mCurrentLapTimeText.setText(t.getElapsedTime());
                //update every 30 milliseconds
                handler.postDelayed(updater, 30);
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!mySession.getLaptimesAsString().equals("")){
            outState.putString(BESTLAP_TEXT_KEY, mySession.getBestLapString());
            outState.putString(LAPTIMES_TEXT_KEY, mySession.getLaptimesAsString());
        }
        if (t.getRunning()) {
            outState.putLong(STARTTIME_TEXT_KEY, t.getStartTime());
        }
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
           //mDistanceFromWaypointText.setText(String.format("%s: %f", "Dist from WP", mDistanceFromWaypoint));
           //mZoneStatusText.setText("IN THE ZONE? " + mIsInZone);
           //mNumberUpdates.setText(String.valueOf(mNum));
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


    /*---------------------------------------------------------------------------------------
    * THIS IS A PRODUCTION FUNCTION FOR THE TIMING FUNCTIONALITY. IF THE TIMING MODIFIER CALCULATIONS
    * ARE INCLUDED WHILE FINISHING LAPS WITHOUT CALLING ISUSERPASTSTARTPOINT, THE DATA WILL
    * BE GARBAGE. GENERIC TESTS FOR APP FUNCTIONALITY SHOULD USE THE OTHER ISUSERINSTARTZONE
     * METHOD UNLESS USING A CAR
    * ---------------------------------------------------------------------------------------
    * */
    protected void isUserInStartZone() {

        //Checks to see if the user is in the specified radius near the start / end point
        if (mDistanceFromWaypoint < mZoneSize) {
            //The user is in the zone
            mIsInZone = true;

            //Calculates the bearings of the user's current location relative to the start point
            mWaypointBearing = mCurrentLocation.bearingTo(mWaypoint);
            mPreviousWaypointBearing = mPreviousLocation.bearingTo(mWaypoint);
            //When the timer is not running, start the timer. This will only trigger on the first lap
            if (!t.getRunning() && isUserPastStartPoint()) {
                //Calculates the time between the current location which triggered the timer to start
                //and the approximate time the user would have crossed the start line
/*                mStartTimeMod = t.getTimeBetweenGpsPing(mCurrentLocation, mPreviousLocation)
                        - t.finishTimeEstimate(mCurrentLocation, mPreviousLocation);*/
                t.start();
                handler.postDelayed(updater, 30);
            }
            //When the timer is running the timer will be stopped if and only if the user has
            //already left the start zone and returned to it. This keeps the timer from stopping
            //if the GPS coordinates of the user are in the start zone for two GPS pings
            //Also checks to see if the user has crossed the start point via bearings delta
            if (t.getRunning() && mHasLeftZone && isUserPastStartPoint()) {
                //Calculates the time between the current location which triggered the timer to stop
                //and the approximate time the user would have crossed the finish line
/*                long finishTimeMod = t.getTimeBetweenGpsPing(mCurrentLocation, mPreviousLocation)
                        - t.finishTimeEstimate(mCurrentLocation, mPreviousLocation);*/
                //stops the timer for this lap
                t.stop();
                //Resets the logic that the user has left the zone
                mHasLeftZone = false;

                //Modifies the lap time to subtract both the modifiers from the lap start
                //and lap finish
                mySession.addLap(t.getLaptime() /*- mStartTimeMod - finishTimeMod*/);

                //update laptimes textview with a list of the session's laptimes
                mPreviousLapTimeText.setText(mySession.formatLaptime(mySession.getLastLapLong()));
                mBestLapTimeText.setText(mySession.formatLaptime(mySession.getBestLapLong()));

                //Sets the modifier for the lap start to the previous lap's lap finish modifier
 /*               mStartTimeMod = finishTimeMod;*/

                //Restarts the timer for the next lap
                t.start();
                handler.postDelayed(updater, 30);
            }
        } else {
            mIsInZone = false;
            //The user has left the zone while the timer is running
            if (t.getRunning()) {
                mHasLeftZone = true;
            }
        }
    }


    /*---------------------------------------------------------------------------------------
    * THIS IS A TESTING FUNCTION FOR THE TIMING FUNCTIONALITY. IF THE TIMING MODIFIER CALCULATIONS
    * ARE INCLUDED WHILE FINISHING LAPS WITHOUT CALLING ISUSERPASTSTARTPOINT, THE DATA WILL
    * BE GARBAGE. GENERIC TESTS FOR APP FUNCTIONALITY SHOULD USE THIS METHOD UNLESS USING A CAR
    * ---------------------------------------------------------------------------------------
    * */
   /* protected void isUserInStartZone() {
        //test code
        if (!t.getRunning()) {
            t.start();
            handler.postDelayed(updater, 30);
        }

        if((System.currentTimeMillis() - t.getStartTime())>5000){
            t.stop();
            mHasLeftZone = false;
            mySession.addLap(t.getLaptime());
            //update laptimes textview with a list of the session's laptimes
            mPreviousLapTimeText.setText(mySession.formatLaptime(t.getLaptime()));
            mBestLapTimeText.setText(mySession.formatLaptime(mySession.getBestLapLong()));
        }

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
                handler.postDelayed(updater, 30);
            }
            //When the timer is running the timer will be stopped if and only if the user has
            //already left the start zone and returned to it. This keeps the timer from stopping
            //if the GPS coordinates of the user are in the start zone for two GPS pings

            //Also checks to see if the user has crossed the start point via bearings delta
            if (t.getRunning() && mHasLeftZone) {
                double finishTimeMod = t.finishTimeEstimate(mCurrentLocation, mPreviousLocation);
                //finishTimeEstimate must be known before startTimeEstimate can be called
                t.stop();
                mHasLeftZone = false;
                mySession.addLap(t.getLaptime());
                //update laptimes textview with a list of the session's laptimes
                mPreviousLapTimeText.setText(mySession.formatLaptime(t.getLaptime()));
                mBestLapTimeText.setText(mySession.formatLaptime(mySession.getBestLapLong()));
            }
        } else {
            //The user has left the zone
            mIsInZone = false;
            mHasLeftZone = true;
        }
    }*/

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
        sendRecordingNotification();
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
        mNum++;

        //Controls lap timer functionality based on location relative to the user and the waypoint
        isUserInStartZone();

        //mNumberUpdates++;
        updateLocationUI();

    }

    //Connects to our google Api client onStart
    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
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
    protected void onDestroy() {
        super.onDestroy();
        cancelRecordingNotification();
    }

    @Override
    public void run() {
        mCurrentLapTimeText.setText("RUNNING");
    }


    //"Stop Session" Button. Changes activity to LapListActivity for the current session. Saves
    //the current session to the database if a lap exists. Returns to main menu if no lap exists
    protected void viewLapTimes(View view) {
        //Checks if a lap exists for the current recording
        if (!mySession.getLaptimesAsString().equals("")) {
            //Stops timer when stop session button is clicked
            t.stop();
            mGoogleApiClient.disconnect();
            //Adds the new session to the database
            addNewSession();
            cancelRecordingNotification();
            //takes user to laplist
            Context context = this;
            Class destinationClass = LapListActivity.class;
            Intent intentToStartDetailActivity = new Intent(context, destinationClass);
            startActivity(intentToStartDetailActivity);
        } else {
            //Brings user to sessionListActivity and returns a toast to say no laps were recorded
            //and nothing has been saved to the database
            returnToSessionList();
            cancelRecordingNotification();

            Toast toast = Toast.makeText(this, "Session not saved - No laps recorded", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    //creates a background task in which to add the session to the db
    public void addNewSession() {
        //Create an explicit intent for RecordingIntentService
        Intent saveSessionIntent = new Intent(this, RecordingIntentService.class);
        //Set the action of the intent to ACTION_SAVE_SESSION
        saveSessionIntent.setAction(RecordLapTasks.ACTION_SAVE_SESSION);

        //add the current session object info to the intent so it can be retrieved
        saveSessionIntent.putExtra("session_driver", mySession.getDriver());
        saveSessionIntent.putExtra("session_track", mySession.getTrackName());
        saveSessionIntent.putExtra("session_bestLap", mySession.getBestLapString());
        saveSessionIntent.putExtra("session_laptimes", mySession.getLaptimesAsString());
        saveSessionIntent.putExtra("session_numLaps", mySession.getNumberOfLaps());

        //Call startService and pass the explicit intent
        startService(saveSessionIntent);
    }

    //Intent to return the user to the SessionlistActivity
    public void returnToSessionList() {
        Context context = this;
        Class destinationClass = SessionListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }

    public void sendRecordingNotification(){

        /*Pending intent omitted until we get better data persistence in place
        Intent intent = new Intent (this, RecordLapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);*/

        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.road)
                .setContentTitle("Sprint LT is recording your session")
                .setContentText(mySession.getTrackName());
               // .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mRecordingNotificationID, mBuilder.build());

    }

    public void cancelRecordingNotification(){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mRecordingNotificationID);
    }

    /******************************************************************
    TEST BUTTONS TO FAKE DATA FOR STARTING SESSION AND COMPLETING LAPS
     ******************************************************************/

    public void testStart(View view) {
        //Toast.makeText(RecordLapActivity.this, "TESTING START BUTTON", Toast.LENGTH_LONG).show();
        mIsInZone = true;
        t.start();
        handler.postDelayed(updater, 30);
    }

    public void testEndLap(View view) {
        t.stop();
        mySession.addLap(t.getLaptime());
        mPreviousLapTimeText.setText(mySession.formatLaptime(mySession.getLastLapLong()));
        mBestLapTimeText.setText(mySession.formatLaptime(mySession.getBestLapLong()));
        t.start();
        handler.postDelayed(updater, 30);
    }
}