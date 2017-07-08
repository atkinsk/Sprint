package com.sn1006.atkins.sprint.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.preference.PreferenceManager;

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;

/**
 * Created by jonathanbrooks on 2017-07-08.
 * This class contains utility methods which updates the session database
 */

public class SessionDbUtility {

    protected SQLiteDatabase mDb;

    //variables to be loaded from shared preferences... may not need these if can access existing session object
    protected String driverName;
    protected String track;

    private long saveSession(Context context) {
        SessionDbHelper dbHelper = new SessionDbHelper(context);
        mDb = dbHelper.getReadableDatabase();

        //call the method that will ultiamtely save the session to the db
        //need to get all the info required to save it
        //on initial put, should create new entry
        //on subsequent put, if same session, should just overwrite
        ContentValues cv = new ContentValues();

        cv.put(SessionContract.SessionEntry.COLUMN_TRACKNAME, mySession.getTrackName());
        cv.put(SessionContract.SessionEntry.COLUMN_DRIVER, mySession.getDriver());
        cv.put(SessionContract.SessionEntry.COLUMN_BESTLAP, mySession.getBestLapString());
        cv.put(SessionContract.SessionEntry.COLUMN_LAPTIMES, mySession.getLaptimesAsString());
        cv.put(SessionContract.SessionEntry.COLUMN_NUMBEROFLAPS, mySession.getNumberOfLaps());

        //insert query
        return mDb.insert(SessionContract.SessionEntry.TABLE_NAME, null, cv);
    }
}
