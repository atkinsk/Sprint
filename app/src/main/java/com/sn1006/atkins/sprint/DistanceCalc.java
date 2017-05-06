package com.sn1006.atkins.sprint;

import android.location.Location;

import static java.lang.Math.*;


/**
 * Created by Atkins on 5/6/2017.
 */

public class DistanceCalc {
    private static double radiusOfEarth =6371000; //meters
    private double currentLatitude;
    private double currentLongitude;
    private double previousLatitude;
    private double previousLongitude;
    private double a; //Haversine Formula, circular distance between two points on a sphere
    private double c; //Angular Distance, in radians
    private double distanceTravelled;


    public DistanceCalc (){
        distanceTravelled = 0.0; //meters
        currentLatitude = 0.0; //radians
        currentLongitude = 0.0; //radians
        previousLatitude = 0.0; //radians
        previousLongitude = 0.0; //radians

    }

    public double coordinatesToDistance (Location currentLocation, Location previousLocation){
        //Converts current and previous gps coordinates from degrees to radians
        currentLatitude = toRadians(currentLocation.getLatitude());
        currentLongitude = toRadians(currentLocation.getLongitude());
        previousLatitude = toRadians(previousLocation.getLatitude());
        previousLongitude = toRadians(previousLocation.getLongitude());


        //Calculates the Haversine Formula to determine the ciruclar distance between two points
        //on a sphere
        a = (sin(currentLatitude - previousLatitude) * sin(currentLatitude - previousLatitude))
                + (cos(currentLatitude) * cos(previousLatitude)
                * sin(currentLongitude - previousLongitude) * sin(currentLongitude - previousLongitude));

        //Calculates the angular distance using the Haversine Formula
        c = 2* atan2(sqrt(a), sqrt(1-a));

        //Calculates the distance travelled as the crow flies based on the angular distance and the
        //radius of the Earth
        distanceTravelled =  radiusOfEarth * c;

        return distanceTravelled;
    }
}
