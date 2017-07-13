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

    public double getLat(String track, Context context) {
        //need to get latitude from csv file using given track name
        //name is in col 1
        //latitude is in col 3
        //**note will be a string! need to parseInt

        InputStream inputStream = context.getResources().openRawResource(R.raw.track_data);
        CSVFile csvFile = new CSVFile(inputStream);
        List<String[]> trackList = csvFile.read();

        //now we have the list of string arrays representing the entire track table, time to return appropriate row for this track!
        //go through and check col 1 for track match, store that row
        int numEntries = trackList.size();
        int row = -1;
        double lat = 0.00;

        for(int i = 0; i < numEntries; i++) {
            if(track.equals(trackList.get(i)[1])) {
                row = i;
            }
        }

        if(row != -1) {
            lat = Double.parseDouble(trackList.get(row)[3]);
        }

        return lat;
    }

    public double getLon(String track, Context context) {
        //need to get longitude from csv file using given track name
        //name is in col 1
        //latitude is in col 4

        InputStream inputStream = context.getResources().openRawResource(R.raw.track_data);
        CSVFile csvFile = new CSVFile(inputStream);
        List<String[]> trackList = csvFile.read();

        //now we have the list of string arrays representing the entire track table, time to return appropriate row for this track!
        //go through and check col 1 for track match, store that row
        int numEntries = trackList.size();
        int row = -1;
        double lon = 5.00;

        for(int i = 0; i < numEntries; i++) {
            if(track.equals(trackList.get(i)[1])) {
                row = i;
            }
        }

        if(row != -1) {
            lon = Double.parseDouble(trackList.get(row)[4]);
        }

        return lon;
    }
}
