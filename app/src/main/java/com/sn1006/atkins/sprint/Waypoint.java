package com.sn1006.atkins.sprint;

/**
 * Created by jonathanbrooks on 2017-05-07.
 * This class is used to create a waypoint object, which will store information about the x and y coordinates
 * of a point of interest, such as a start point or a stop point for a sprint, or a start/stop point on a circuit.
 *
 * For now, the waypoint only needs an x and y coord, and a name.
 * The name allows the app to distinguish which waypoint we are interested in, when multiple waypoints exist.
 *
 * In future versions, perhaps an auto-assigned ID makes more sense and waypoints get stored in an arrayList of
 * waypoint objects.... although each spring will have multiple waypoints and other info. So in future versions
 * we will need a sprint object to store all the info associated with a sprint.
 *
 * ...to be thought on further when we get to that point.
 */

public class Waypoint {

    public int xCoord;
    public int yCoord;
    public String name;

    public Waypoint(int initXCoord, int initYCoord, String initName) {
        xCoord = initXCoord;
        yCoord = initYCoord;
        name = initName;
    }

    public int getXCoord() {
        return xCoord;
    }

    public int getYCoord() {
        return yCoord;
    }

    public String getName() {
        return name;
    }
}