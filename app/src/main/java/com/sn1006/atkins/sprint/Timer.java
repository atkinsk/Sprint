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

    long finishTimeEstimate(Location currentLocation, Location previousLocation) {
        finishTimeModifier = gpsTimeEstimateCalc(currentLocation, previousLocation);
        return finishTimeModifier;
    }

    long gpsTimeEstimateCalc(Location currentLocation, Location previousLocation) {
        initialSpeed = previousLocation.getSpeed(); //m/s
        finalSpeed = currentLocation.getSpeed();// m/s
        timeBetweenGpsPing = getTimeBetweenGpsPing(currentLocation, previousLocation); //ms
        //Acceleration is approximated using parameters from Location Services.
        //Consider using API level 17 to avoid using getTime.. Not always accurate
        averageAcceleration = (finalSpeed - initialSpeed) / timeBetweenGpsPing; //m/s^2
        distanceTravelled = previousLocation.distanceTo(currentLocation); //m

        long timeModifier = (long) (-initialSpeed + Math.sqrt((initialSpeed * initialSpeed)
                - (2 * averageAcceleration * (-distanceTravelled))) / averageAcceleration) * 1000; //ms
        return timeModifier;
    }

    long getTimeBetweenGpsPing (Location currentLocation, Location previousLocation){
        return (currentLocation.getTime() - previousLocation.getTime());
    }

}
