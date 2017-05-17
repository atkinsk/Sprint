package com.sn1006.atkins.sprint;

import android.location.Location;

/**
 * Created by jonathanbrooks on 2017-05-07.
 * Class to organize all timer logic
 */

public class Timer {

    private long startTime = 0; //seconds
    private long stopTime = 0; //seconds
    private boolean running = false;
    private double distanceTravelled; //meters
    private double initialSpeed; // m/s
    private double finalSpeed; //m/s
    private double averageAcceleration; //m/s^2
    private double timeBetweenGpsPing; //seconds
    private double finishTimeModifier; //seconds
    private double startTimeModifier; //seconds



    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
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

        if(running) {
            elapsed = System.currentTimeMillis() - startTime;
        }
        else {
            elapsed = stopTime - startTime;
        }

        mins = (int) (elapsed/60000);
        secs = (int) (elapsed - mins*60000)/1000;
        millis = (int) (elapsed - mins*60000 - secs*1000);

        return ("Time: " + String.format("%02d",mins) + ":" + String.format("%02d",secs) + ":"
                + String.format("%02d",millis));
    }

    double finishTimeEstimate(Location currentLocation, Location previousLocation) {
        finishTimeModifier = gpsTimeEstimateCalc(currentLocation, previousLocation);

        return finishTimeModifier;
    }

    double startTimeEstimate (Location currentLocation, Location previousLocation) {
        startTimeModifier = timeBetweenGpsPing - gpsTimeEstimateCalc(currentLocation, previousLocation);

        return startTimeModifier;
    }

    double gpsTimeEstimateCalc (Location currentLocation, Location previousLocation){
        initialSpeed = previousLocation.getSpeed();
        finalSpeed = currentLocation.getSpeed();
        timeBetweenGpsPing = (currentLocation.getTime()
                - previousLocation.getTime()) / 1000;
        //Acceleration is approximated using parameters from Location Services.
        //Consider using API level 17 to avoid using getTime.. Not always accurate
        averageAcceleration = (finalSpeed - initialSpeed) / timeBetweenGpsPing;
        distanceTravelled = previousLocation.distanceTo(currentLocation);

        double timeModifier = (-initialSpeed + Math.sqrt((initialSpeed * initialSpeed)
                - (2 * averageAcceleration * (-distanceTravelled))) / averageAcceleration);

        return timeModifier;
    }

}
