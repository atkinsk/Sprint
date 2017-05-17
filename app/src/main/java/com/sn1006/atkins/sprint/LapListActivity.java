package com.sn1006.atkins.sprint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LapListActivity extends AppCompatActivity {

    protected TextView mLaptimesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_list);

        mLaptimesText = (TextView) findViewById(R.id.showLaptimes);
        //mLaptimesText.setText(mySession.toString());
    }
}
