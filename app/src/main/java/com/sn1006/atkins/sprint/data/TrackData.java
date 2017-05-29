package com.sn1006.atkins.sprint.data;

/**
 * Created by jonathanbrooks on 2017-05-24.
 */
//this is a brute force class to return the wp coordinates depending on track name
    //in future, this should be a preferences file that is smarter than this... or a db

public class TrackData {

    public static double getLat(String track) {
        if(track.equals("Watkins Glen International")) {
            return 42.341043;
        }
        else if(track.equals("test")) {
            return 45.293715;
        }
        else {
            return 0.00;
        }
    }

    public static double getLon(String track) {
        if(track.equals("Watkins Glen International")) {
            return -76.928877;
        }
        else if(track.equals("test")) {
            return -75.856780;
        }
        else {
            return 0.00;
        }
    }
}
