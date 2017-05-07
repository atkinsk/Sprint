package com.sn1006.atkins.sprint;

/**
 * Created by jonathanbrooks on 2017-05-07.
 */

public class Timer {

    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;


    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
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
        millis = (int) (elapsed - mins*60000 - secs*60000000);

        return ("Time: " + mins + ":" + secs + ":" + millis);
    }
}
