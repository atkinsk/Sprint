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

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;

public class SessionListActivity extends AppCompatActivity {

    private SessionListAdapter mAdapter;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        RecyclerView sessionRecyclerView;

        sessionRecyclerView = (RecyclerView) this.findViewById(R.id.all_session_list_view);
        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SessionDbHelper dbHelper = new SessionDbHelper(this);

        mDb = dbHelper.getReadableDatabase();

        Cursor cursor = getSessionList();

        mAdapter = new SessionListAdapter(this, cursor);

        sessionRecyclerView.setAdapter(mAdapter);
    }

    protected Cursor getSessionList() {
        return mDb.query(
                SessionContract.SessionEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                SessionContract.SessionEntry.COLUMN_DATE_TIME
        );
    }

    protected void recordLap(View view){
        Context context = this;
        Class destinationClass = RecordLapActivity.class;
        Intent intentToStartDetailActivity = new Intent (context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }
}
