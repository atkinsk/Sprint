package com.sn1006.atkins.sprint;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
    private String driver; //for future use
    private long bestLap;
    public ArrayList<Long> laptimes;

    //for date-time stamping session
    Calendar c;
    private Date dateStamp;

    //session needs an id (date/timestamp) to differentiate it
    public Session() {
        this.trackname = "@string/pref_track_default";
        this.laptimes = new ArrayList<Long>();
        this.dateStamp = c.getInstance().getTime();
        this.driver = "@string/pref_driver_default";
    }

    public String getTrackName() {
        return this.trackname;
    }

    public String getDriver() {
        return this.driver;
    }

    public void setDriver(String driverName) {
        this.driver = driverName;
    }

    public void setTrack(String track) {
        this.trackname = track;
    }

    public String getLaptimesAsString() {

        String strSeparator = ",";
        String lapTimesAsString = "";

        for (long x : this.laptimes) {
            lapTimesAsString = lapTimesAsString + String.valueOf(x);

            if(this.laptimes.indexOf(x)<this.laptimes.size()-1){
                lapTimesAsString = lapTimesAsString + strSeparator;
            }
        }
        return lapTimesAsString;
    }

    //add a lap to the session
    public void addLap(Long laptime) {
        if(laptimes.isEmpty()) {
            this.bestLap = laptime;
        }
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

    public String getNumberOfLaps(){
        return "" + this.laptimes.size();
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
                + String.format("%03d",millis));
    }

/*    //prints out list of all laptimes in the session, with the best lap in bold
    @Override
    public String toString() {
        String laptimesAsString = "";

        for(long x : this.laptimes) {
            //if best lap, bold it
            if(x == this.bestLap) {
                laptimesAsString += "<b>" + formatLaptime(x) + "</b>" + "\n";
            }
            else {
                laptimesAsString += formatLaptime(x) + "\n";
            }

        }

        return laptimesAsString;
    }*/

    public void convertStringToArray(String str) {
        this.laptimes.clear();
        if(!str.equals("")) {
            for (String s : str.split(",")) {
                this.laptimes.add(Long.parseLong(s));
            }
        }
    }

    public void setBestLap (String bestLap){
        this.bestLap = Long.parseLong(bestLap);
    }

    public Long getLastLapLong(){
        return this.laptimes.get(this.laptimes.size()-1);
    }

    public String getBestLapString() {
        return "" + this.bestLap;
    }

    public Long getBestLapLong(){
        return this.bestLap;
    }
}