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
    private static double radiusOfEarth =6371000; //meters
    private double currentLatitude; //degrees, current GPS coordinate (latitude)
    private double currentLongitude; //degrees, current GPS coordinate (longitude)
    private double previousLatitude; //degrees, last GPS coordinate (latitude)
    private double previousLongitude; //degrees, last GPS coordinate (longitude)
    private double a; //Haversine Formula, circular distance between two points on a sphere
    private double c; //Angular Distance, in radians
    private double distanceTravelled; //meters
    private double degreesToWaypoint; //degrees

    //See comment about access level modifier for the class
    DistanceCalc (){
        distanceTravelled = 0.0; //meters
        currentLatitude = 0.0; //radians
        currentLongitude = 0.0; //radians
        previousLatitude = 0.0; //radians
        previousLongitude = 0.0; //radians
        degreesToWaypoint = 0.0; //degrees

    }

    //See comment about access level modifier for the class
    double coordinatesToDistance (Location currentLocation, Location previousLocation){
        //Converts current and previous gps coordinates from degrees to radians
        currentLatitude = toRadians(currentLocation.getLatitude());
        currentLongitude = toRadians(currentLocation.getLongitude());
        previousLatitude = toRadians(previousLocation.getLatitude());
        previousLongitude = toRadians(previousLocation.getLongitude());


        //Calculates the Haversine Formula to determine the ciruclar distance between two points on a sphere
        //Did not use Math.pow as doing x * x multiplication yields better performance
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

    double getDegreesToWaypoint (Location currentLocation, Location waypointLocation){
        return currentLocation.bearingTo(waypointLocation);
    }
}
