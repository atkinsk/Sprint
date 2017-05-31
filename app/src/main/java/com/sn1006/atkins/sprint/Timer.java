package com.sn1006.atkins.sprint;

import android.location.Location;

import com.google.android.gms.location.LocationListener;

/**
 * Created by jonathanbrooks on 2017-05-07.
 * Class to organize all timer logic
 */

public class Timer {

    private long startTime; //ms
    private long stopTime; //ms
    private boolean running = false;
    private double distanceTravelled; //meters
    private double initialSpeed; // m/s
    private double finalSpeed; //m/s
    private double averageAcceleration; //m/s^2
    private long timeBetweenGpsPing; //ms
    private long finishTimeModifier = 0; //ms
    private double mAlpha1; //deg. previousLocation
    private double mAlpha2; //deg. currentLocaiton
    private double mBeta1; //deg. previousLocation
    private double mGamma1; //deg. previousLocation
    private double mDistanceToFinishLine; //m. previousLocation

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    public Long getStartTime (){return this.startTime;
    }

    public void setStartTime (long startTime){
        this.startTime = startTime;
    }

    public boolean getRunning() {
        return this.running;
    }

    public Long getLaptime() {
        return stopTime - startTime;
    }

    //return a string with the elapsed time in format of mins:seconds:millis
    public String getElapsedTime() {

        long elapsed;
        int mins;
        int secs;
        int millis;

        if (running) {
            elapsed = System.currentTimeMillis() - startTime;
        } else {
            elapsed = stopTime - startTime;
        }

        mins = (int) (elapsed / 60000);
        secs = (int) (elapsed - mins * 60000) / 1000;
        millis = (int) (elapsed - mins * 60000 - secs * 1000);

        //Keeps 3 digits when a second rolls over. Perhaps find a better way of doing this
        return (String.format("%02d", mins) + ":" + String.format("%02d", secs) + ":"
                + String.format("%03d", millis));
    }

    long finishTimeEstimate(Location currentLocation, Location previousLocation, Location waypointLocation) {
        finishTimeModifier = gpsTimeEstimateCalc(currentLocation, previousLocation, waypointLocation);
        return finishTimeModifier;
    }

    long gpsTimeEstimateCalc(Location currentLocation, Location previousLocation, Location waypointLocation) {
        initialSpeed = previousLocation.getSpeed(); //m/s
        finalSpeed = currentLocation.getSpeed();// m/s
        timeBetweenGpsPing = getTimeBetweenGpsPing(currentLocation, previousLocation); //ms
        //Acceleration is approximated using parameters from Location Services.
        //Consider using API level 17 to avoid using getTime.. Not always accurate
        averageAcceleration = (finalSpeed - initialSpeed) / timeBetweenGpsPing; //m/s^2
        directionOfTravelCheck(currentLocation, previousLocation);
        distanceToFinishLineCalc(currentLocation, previousLocation, waypointLocation);


        long timeModifier = (long) (-initialSpeed + Math.sqrt((initialSpeed * initialSpeed)
                - (2 * averageAcceleration * (-mDistanceToFinishLine))) / averageAcceleration) * 1000; //ms
        return timeModifier;
    }

    long getTimeBetweenGpsPing (Location currentLocation, Location previousLocation){
        return (currentLocation.getTime() - previousLocation.getTime());
    }
//This needs to be improved.
    void directionOfTravelCheck (Location currentLocation, Location previousLocation){
        double normalizedBearingPreviousLocation = normalizeDegrees(previousLocation.getBearing());
        double normalizedBearingCurrentLocation = normalizeDegrees(currentLocation.getBearing());

        //Determines cartesian quadrant of previous location relative to finish line
        if(normalizedBearingPreviousLocation<=90){
            mAlpha1 = normalizedBearingPreviousLocation;
        } else if (normalizedBearingPreviousLocation>90 && normalizedBearingPreviousLocation<=180){
            mAlpha1 = 90 - normalizedBearingPreviousLocation;
        } else if (normalizedBearingPreviousLocation>180 && normalizedBearingPreviousLocation<=270){
            mAlpha1 = 180 - normalizedBearingPreviousLocation;
        } else if (normalizedBearingPreviousLocation > 270){
            mAlpha1 = 270 - normalizedBearingPreviousLocation;
        }

        //Determines cartesian quadrant of current location relative to finish line
        if(normalizedBearingCurrentLocation<=90){
            mAlpha2 = normalizedBearingCurrentLocation;
        } else if (normalizedBearingCurrentLocation>90 && normalizedBearingCurrentLocation<=180){
            mAlpha2 = 90 - normalizedBearingCurrentLocation;
        } else if (normalizedBearingCurrentLocation>180 && normalizedBearingCurrentLocation<=270){
            mAlpha2 = 180 - normalizedBearingCurrentLocation;
        } else if (normalizedBearingCurrentLocation > 270){
            mAlpha2 = 270 - normalizedBearingCurrentLocation;
        }
    }

    void distanceToFinishLineCalc (Location currentLocation, Location previousLocation, Location waypointLocation){

        mGamma1 = ((Math.sin(mAlpha1+mAlpha2))*currentLocation.distanceTo(waypointLocation)/previousLocation.distanceTo(currentLocation));

        mBeta1 = 180 - (mGamma1 + mAlpha1);

        mDistanceToFinishLine = (previousLocation.distanceTo(waypointLocation)*Math.sin(mAlpha1)/Math.sin(mBeta1));
    }

    protected double normalizeDegrees(double locationBearing) {
        if (locationBearing >= 0.0 && locationBearing <= 180) {
            return locationBearing;
        } else {
            return 180 + (180 + locationBearing);
        }
    }

}
