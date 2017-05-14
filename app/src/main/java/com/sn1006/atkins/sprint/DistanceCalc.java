package com.sn1006.atkins.sprint;

import android.location.Location;

import static java.lang.Math.*;


/**
 * Created by Atkins on 5/6/2017.
 * This class is responsible for converting the current and previous GPS coordinates of the user
 * and converting it to a distance as the crow flies(in meters)
 */
//There is no access level (i.e. public) as the default is package-private which makes it so this
    //class is only visible within the package
class DistanceCalc {
    private double distanceTravelled; //meters
    private double initialSpeed; // m/s
    private double finalSpeed; //m/s
    private double averageAcceleration; //m/s^2
    private double finishTime; //seconds

    //See comment about access level modifier for the class
    DistanceCalc (){
        distanceTravelled = 0.0;
        finishTime = 0.0;
        initialSpeed = 0.0;
        finalSpeed = 0.0;
        averageAcceleration = 0.0;

    }
    //Calculates the time at the finish point using the acceleration, initial velocity and
    //distance to the final waypoint using the quadratic equation
    double timeAtFinish (Location currentLocation, Location previousLocation){
        initialSpeed = previousLocation.getSpeed();
        finalSpeed = currentLocation.getSpeed();
        //Acceleration is approximated using parameters from Location Services.
        //Time is approximated to 1 second until accelerometer is used
        averageAcceleration = (finalSpeed - initialSpeed) / 1.0;
        distanceTravelled = previousLocation.distanceTo(currentLocation);

        finishTime = (-initialSpeed + Math.sqrt((initialSpeed*initialSpeed)
                -(2*averageAcceleration*(-distanceTravelled)))/averageAcceleration);

        return finishTime;
    }

}
