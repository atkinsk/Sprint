package com.sn1006.atkins.sprint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LapListActivity extends AppCompatActivity {

    private LapListAdapter mAdapter;
    private SQLiteDatabase mDb;
    private ArrayList<Long> mListOfLaps = new ArrayList<Long>();
    protected TextView mSessionNameText;
    protected TextView mNumberOfLapsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_list);

        RecyclerView lapRecyclerView;

        lapRecyclerView = (RecyclerView) this.findViewById(R.id.all_lap_list_view);
        lapRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SessionDbHelper dbHelper = new SessionDbHelper(this);

        mDb = dbHelper.getReadableDatabase();

        Cursor cursor = getSessionList();

        cursor.moveToFirst();

        String laps = cursor.getString(cursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_LAPTIMES));

        mSessionNameText = (TextView) findViewById(R.id.lapListHeader);
        mNumberOfLapsText = (TextView) findViewById(R.id.numberOfLaps);

        mSessionNameText.setText("Session Results");

        if (!laps.equals("")) {

            convertStringToArray(laps);

            if (mListOfLaps.size() == 1) {
                mNumberOfLapsText.setText(String.valueOf(mListOfLaps.size()) + " LAP");
            } else {
                mNumberOfLapsText.setText(String.valueOf(mListOfLaps.size()) + " LAPS");
            }

            mAdapter = new LapListAdapter(this, mListOfLaps);

            lapRecyclerView.setAdapter(mAdapter);
        } else {
            returnToSessionList();
        }
    }

    protected Cursor getSessionList() {
        String query = "SELECT * FROM "
                + SessionContract.SessionEntry.TABLE_NAME + " WHERE "
                + SessionContract.SessionEntry._ID + " = (SELECT MAX("
                + SessionContract.SessionEntry._ID + ") FROM "
                + SessionContract.SessionEntry.TABLE_NAME + ")";
        return mDb.rawQuery(query, null);
    }

    public void convertStringToArray(String str) {
        for (String s : str.split(",")) {
            mListOfLaps.add(Long.parseLong(s));
        }
    }

    protected void returnToSessionList() {
        Context context = this;
        Class destinationClass = SessionListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }
}


