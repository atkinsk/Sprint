package com.sn1006.atkins.sprint;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LapListActivity extends AppCompatActivity {

    private LapListAdapter mAdapter;
    private SQLiteDatabase mDb;
    private ArrayList<Long> mListOfLaps;

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

        mAdapter = new LapListAdapter(this, cursor);

        lapRecyclerView.setAdapter(mAdapter);
    }

    protected Cursor getSessionList() {
        String query = "SELECT * FROM " + SessionContract.SessionEntry.TABLE_NAME + " WHERE "
                + SessionContract.SessionEntry._ID + " = (SELECT MAX("
                + SessionContract.SessionEntry._ID + ") FROM "
                + SessionContract.SessionEntry.TABLE_NAME + ")";
        return mDb.rawQuery(query, null);
    }
    }


