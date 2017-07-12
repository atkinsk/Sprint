package com.sn1006.atkins.sprint.preference;

import android.content.Context;
//import android.preference.ListPreference;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by jonathanbrooks on 2017-07-12.
 */

public class TrackDataPreference extends ListPreference {

    public TrackDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEntries(entries());
        setEntryValues(entryValues());
        setValueIndex(initializeIndex());
    }

    public TrackDataPreference(Context context) {
        this(context, null);
    }

    //get list of track names from track_data.csv and return to constructor
    private String[] entries() {

        String[] trackEntries = {"Watkins Glen", "Mosport"};

        return trackEntries;
    }

    //get list of track entry values from track_data.csv and return to constructor
    private String[] entryValues() {

        String[] trackValues = {"wgi", "ctmp"};

        return trackValues;
    }

    //set default value - retrieved from shared preferences if previously set by user
    private int initializeIndex() {
        int index = 1;

        return index;
    }
}
