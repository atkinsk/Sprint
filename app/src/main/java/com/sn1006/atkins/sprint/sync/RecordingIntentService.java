package com.sn1006.atkins.sprint.sync;

import android.app.IntentService;
import android.content.Intent;

import com.sn1006.atkins.sprint.Session;

/**
 * Created by jonathanbrooks on 2017-06-27.
 *
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */

public class RecordingIntentService extends IntentService {

    public RecordingIntentService() {
        super("RecordingIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the action from the Intent that started this Service
        String action = intent.getAction();
        String driver = intent.getStringExtra("session_driver");
        String track = intent.getStringExtra("session_track");
        String bestLap = intent.getStringExtra("session_bestLap");
        String laptimes = intent.getStringExtra("session_laptimes");
        String numLaps = intent.getStringExtra("session_numLaps");

        //call RecordLapTasks.executeTask and pass in the action to be performed
        RecordLapTasks.executeTask(this, action, driver, track, bestLap, laptimes, numLaps);
    }
}