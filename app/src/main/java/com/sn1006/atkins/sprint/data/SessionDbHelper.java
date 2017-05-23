package com.sn1006.atkins.sprint.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sn1006.atkins.sprint.data.SessionContract.SessionEntry;

public class SessionDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "session.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;

    public SessionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * cache our weather data.
         */
        final String SQL_CREATE_SESSION_TABLE =

                "CREATE TABLE " + SessionEntry.TABLE_NAME + " (" +

                /*
                 * WeatherEntry did not explicitly declare a column called "_ID". However,
                 * WeatherEntry implements the interface, "BaseColumns", which does have a field
                 * named "_ID". We use that here to designate our table's primary key.
                 */
                        SessionEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SessionEntry.COLUMN_DATE_TIME  + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        SessionEntry.COLUMN_TRACKNAME  + " TEXT, " +
                        SessionEntry.COLUMN_DRIVER     + " TEXT, " +
                        SessionEntry.COLUMN_LAPTIMES   + " TEXT, " +
                        SessionEntry.COLUMN_NUMBEROFLAPS + " TEXT, " +
                        SessionEntry.COLUMN_BESTLAP    + " TEXT" + ");";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_SESSION_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}