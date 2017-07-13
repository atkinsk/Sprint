package com.sn1006.atkins.sprint.preference;

import android.content.Context;
//import android.preference.ListPreference;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import com.sn1006.atkins.sprint.data.TrackDataCSVHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathanbrooks on 2017-07-12.
 */

public class TrackDataPreference extends ListPreference {

    TrackDataCSVHelper fetcher = new TrackDataCSVHelper();

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

        String[] trackList = fetcher.getTracks(getContext(), 1);

        return trackList;
    }

    //get list of track entry values from track_data.csv and return to constructor
    private String[] entryValues() {

        String[] entryList = fetcher.getTracks(getContext(), 1);  //<-- right now using col 1, we want full track name as value, not short form

        return entryList;
    }

    //set default value - retrieved from shared preferences if previously set by user
    private int initializeIndex() {
        int index = 0;

        return index;
    }
}
