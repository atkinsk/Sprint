package com.sn1006.atkins.sprint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.data.SessionDbHelper;


//Implements interface from SessionListAdapter to handle clicks on the recyclerview views
public class SessionListActivity extends AppCompatActivity implements SessionListAdapter.SessionAdapterOnClickHandler {

    private SessionListAdapter mAdapter;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);


        //Initiation of the recyclerview for the session list
        RecyclerView sessionRecyclerView;
        sessionRecyclerView = (RecyclerView) this.findViewById(R.id.all_session_list_view);
        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SessionDbHelper dbHelper = new SessionDbHelper(this);

        mDb = dbHelper.getReadableDatabase();

        //Pulls the session list from the database (readable)
        Cursor cursor = getSessionList();

        //Initiates and sets the SessionListAdapter for the recyclerview
        mAdapter = new SessionListAdapter(this, cursor, this);
        sessionRecyclerView.setAdapter(mAdapter);
    }

    //Query for retrieving the sessionlist
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

    //Intent for the floating action button to begin recording a lap
    protected void recordLap(View view){
        Context context = this;
        Class destinationClass = RecordLapActivity.class;
        Intent intentToStartDetailActivity = new Intent (context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }

    //When an item from the recyclerview is clicked, an intent is sent with data on which
    //item index was selected. This allows the LapListActivity to show the laps
    //for the session in question
    //NOTE: If ordering is applied to SessionListActivity, an DB ID will need to be used instead
    @Override
    public void onClick(int clickedItemIndex) {
        Context context = this;
        Class destinationClass = LapListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        String s = String.valueOf(clickedItemIndex);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, s);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sprint_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
