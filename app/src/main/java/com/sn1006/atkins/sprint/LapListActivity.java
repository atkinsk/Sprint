package com.sn1006.atkins.sprint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;

import java.util.ArrayList;

public class LapListActivity extends AppCompatActivity {

    private LapListAdapter mAdapter;
    private SQLiteDatabase mDb;
    private ArrayList<Long> mListOfLaps = new ArrayList<Long>();
    protected TextView mSessionNameText;
    protected TextView mNumberOfLapsText;
    private int mIndexSelected = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_list);

        Intent intentFromSessionActivity = getIntent();

        RecyclerView lapRecyclerView;

        lapRecyclerView = (RecyclerView) this.findViewById(R.id.all_lap_list_view);
        lapRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SessionDbHelper dbHelper = new SessionDbHelper(this);

        mDb = dbHelper.getReadableDatabase();

        Cursor cursor;

        //Determine whether or not the intent passed to open LapListActivity was instigated by
        //clicking on the SessionListActivity or by finishing a session.
        //If the SessionListActivity was clicked on, a DB will be pulled based on the specified
        //index that was clicked on
        //If a session recording was finished, the DB will be pulled based on the highest ID in the DB
        if (intentFromSessionActivity.hasExtra(Intent.EXTRA_TEXT)) {
            String extra = intentFromSessionActivity.getStringExtra(Intent.EXTRA_TEXT);
            mIndexSelected = Integer.parseInt(extra) + 1;
            cursor = getSpecificSessionList();
        } else {
            cursor = getLastSessionList();
        }

        //Moves the cursor from -1 to 0 (first value in the DB)
        cursor.moveToFirst();

        String laps = cursor.getString(cursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_LAPTIMES));

        mSessionNameText = (TextView) findViewById(R.id.lapListHeader);
        mNumberOfLapsText = (TextView) findViewById(R.id.numberOfLaps);

        //NOTE: convertStringToArray will crash if there were no laps. This was a problem caused
        //when sessions with no laps would be saved to DB. Now that this cannot happen,
        //this should not be a problem... commented out if statement for now.
        //if (!laps.equals("")) {
        convertStringToArray(laps);

        //Determines whether to use singular or plural in number of laps header
        if (mListOfLaps.size() == 1) {
            mNumberOfLapsText.setText(String.valueOf(mListOfLaps.size()) + " LAP");
        } else {
            mNumberOfLapsText.setText(String.valueOf(mListOfLaps.size()) + " LAPS");
        }
        //Send in ArrayList to Adapter instead of a Cursor. Could not cycle through values in
        //ArrayList otherwise.
        mAdapter = new LapListAdapter(this, mListOfLaps);

        lapRecyclerView.setAdapter(mAdapter);
        /*} else {
            returnToSessionList();
        }*/
    }
    //Query to pull the last session in the DB. Used when showing laplist for recently completed
    //recording
    protected Cursor getLastSessionList() {
        String query = "SELECT * FROM "
                + SessionContract.SessionEntry.TABLE_NAME + " WHERE "
                + SessionContract.SessionEntry._ID + " = (SELECT MAX("
                + SessionContract.SessionEntry._ID + ") FROM "
                + SessionContract.SessionEntry.TABLE_NAME + ")";
        return mDb.rawQuery(query, null);
    }

    //Query to pull a specific session in the DB. Used when clicking on specific session in
    //SessionListActivity
    protected Cursor getSpecificSessionList() {
        String query = "SELECT * FROM "
                + SessionContract.SessionEntry.TABLE_NAME + " WHERE "
                + SessionContract.SessionEntry._ID + " = ("
                + mIndexSelected + ")";
        return mDb.rawQuery(query, null);
    }

    //DB must hold string values for lap times. Must convert string back to an ArrayList to
    //separate lap times
    public void convertStringToArray(String str) {
        for (String s : str.split(",")) {
            mListOfLaps.add(Long.parseLong(s));
        }
    }
    //No longer necessary. See comment on convertStringToArray in onCreate method
/*    protected void returnToSessionList() {
        Context context = this;
        Class destinationClass = SessionListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }*/

}


