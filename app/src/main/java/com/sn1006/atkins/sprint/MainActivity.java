package com.sn1006.atkins.sprint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //Fires when the system first creates the Main Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    //method to call intent to create LapListActivity when button is clicked
    protected void viewLaptimes(View view) {
        Context context = this;
        Class destinationClass = LapListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }

    protected void viewSessions (View view){
        Context context = this;
        Class destinationClass = SessionListActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }

    protected void recordLap(View view){
        Context context = this;
        Class destinationClass = RecordLapActivity.class;
        Intent intentToStartDetailActivity = new Intent (context, destinationClass);
        startActivity(intentToStartDetailActivity);
    }
}

