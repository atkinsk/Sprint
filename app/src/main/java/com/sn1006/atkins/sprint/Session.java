package com.sn1006.atkins.sprint;

import java.util.ArrayList;

/**
 * Created by jonathanbrooks on 2017-05-13.
 *
 * Session object holds a list of laptimes for a particular track and driver
 * Session object includes a date-timestamp to distinguish it from other sessions
 * that the driver may do on the same day at the same track
 *
 * in future we will add a driver id or some way of identifying unique drivers
 * for now the driver will just be "default"
 */

public class Session {

    private String trackname;
    private String driver;
    private Long bestLap;
    private ArrayList<Long> laptimes;

    //session needs an id (date/timestamp) to differentiate it
    public Session(String aTrack, String aDriver) {
        this.trackname = aTrack;
        this.driver = aDriver;
        this.laptimes = new ArrayList<Long>();
        this.bestLap = null;
    }

    //add a lap to the session
    public void addLap(Long laptime) {
        this.laptimes.add(laptime);
        checkBestLap(laptime);
    }

    //checks if the current lap, is the best yet
    //if so, save it!
    public void checkBestLap(Long laptime) {
        if (laptime < this.bestLap) {
            this.bestLap = laptime;
        }
    }

    //takes laptime from Long format and makes it mm:ss:xx
    public String formatLaptime(Long laptime) {
        int mins;
        int secs;
        int millis;

        mins = (int) (laptime/60000);
        secs = (int) (laptime - mins*60000)/1000;
        millis = (int) (laptime - mins*60000 - secs*1000);

        return (String.format("%02d",mins) + ":" + String.format("%02d",secs) + ":"
                + String.format("%02d",millis));
    }

    //prints out list of all laptimes in the session, with the best lap in bold
    @Override
    public String toString() {
        String laptimesAsString = "";

        for(Long x : this.laptimes) {
            //if best lap, bold it
            if(x == this.bestLap) {
                laptimesAsString += "<b>" + formatLaptime(x) + "</b>" + "\n";
            }
            else {
                laptimesAsString += formatLaptime(x) + "\n";
            }

        }

        return this.driver + "'s session laptimes at " + this.trackname + "\n" + laptimesAsString;
    }
}
