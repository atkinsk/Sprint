package com.sn1006.atkins.sprint.sync;

import android.content.ContentValues;
import android.content.Context;

import com.sn1006.atkins.sprint.Session;
import com.sn1006.atkins.sprint.data.SessionContract;
import com.sn1006.atkins.sprint.utilities.SessionDbUtility;

/**
 * Created by jonathanbrooks on 2017-06-27.
 */

public class RecordLapTasks {

    public static final String ACTION_SAVE_SESSION = "save-session";

    public static void executeTask(Context context, String action, String driver, String track, String bestLap, String laptimes, String numLaps) {

        //if action equals ACTION_SAVE_SESSION, then save the session's info to the db
        if (ACTION_SAVE_SESSION.equals(action)) {
            SessionDbUtility.saveSession(context, driver, track, bestLap, laptimes, numLaps);
        }
    }
}
