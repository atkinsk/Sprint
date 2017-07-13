package com.sn1006.atkins.sprint.data;

import android.content.Context;
import android.util.Log;

import com.sn1006.atkins.sprint.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathanbrooks on 2017-05-24.
 */

public class TrackDataCSVHelper {

    public TrackDataCSVHelper() {

    }

    public final String[] getTracks(Context context, int col) {

        InputStream inputStream = context.getResources().openRawResource(R.raw.track_data);
        CSVFile csvFile = new CSVFile(inputStream);
        List<String[]> trackList = csvFile.read();

        //now we have the list of string arrays representing the entire track table, time to return appropriate col

        List<String> tempList = new ArrayList<>();
        String thisTrack;
        String[] trackEntries = null;

        //tracklist is an arraylist with an entry for each line of the csv (each track)
        //tracklist.get(x) will return row x (a String[] with each col)
        //we want to return the index 1 (track name) of EACH row, to the end of the list
        int numEntries = trackList.size();

        //put all track names in list
        for(int i = 0; i < numEntries; i++) {
            thisTrack = trackList.get(i)[col];
            tempList.add(i, thisTrack);
        }

        //turn track names list into string[]
        trackEntries = new String[numEntries];

        for(int i = 0; i < numEntries; i++) {
            trackEntries[i] = tempList.get(i);
        }

        return trackEntries;
    }

    private class CSVFile {
        InputStream inputStream;

        public CSVFile(InputStream inputStream){
            this.inputStream = inputStream;
        }

        public List<String[]> read(){
            //
            List<String[]> resultList = new ArrayList<String[]>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] row = line.split(",");
                    resultList.add(row);
                }
            }
            catch (IOException e) {
                Log.e("Main",e.getMessage());
            }
            finally {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    Log.e("Main",e.getMessage());
                }
            }
            return resultList;
        }
    }

    public static double getLat(String track) {
        if(track.equals("wgi")) {
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
        if(track.equals("wgi")) {
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
