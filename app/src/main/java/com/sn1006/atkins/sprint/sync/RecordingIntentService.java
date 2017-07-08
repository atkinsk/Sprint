package com.sn1006.atkins.sprint.sync;

import android.app.IntentService;
import android.content.Intent;

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

        //call RecordLapTasks.executeTask and pass in the action to be performed
        RecordLapTasks.executeTask(this, action);
    }
}