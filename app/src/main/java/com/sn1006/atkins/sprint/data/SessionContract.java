package com.sn1006.atkins.sprint.data;

/**
 * Created by jonathanbrooks on 2017-05-20.
 */

import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database. This class is not necessary, but keeps
 * the code organized.
 */
public class SessionContract {

    public static final class SessionEntry implements BaseColumns {

        /* Used internally as the name of our session table. */
        public static final String TABLE_NAME = "session";

        /*
         * The date column will store the UTC date that correlates to the local date for which
         * each particular weather row represents. For example, if you live in the Eastern
         * Standard Time (EST) time zone and you load weather data at 9:00 PM on September 23, 2016,
         * the UTC time stamp for that particular time would be 1474678800000 in milliseconds.
         * However, due to time zone offsets, it would already be September 24th, 2016 in the GMT
         * time zone when it is 9:00 PM on the 23rd in the EST time zone. In this example, the date
         * column would hold the date representing September 23rd at midnight in GMT time.
         * (1474588800000)
         *
         * The reason we store GMT time and not local time is because it is best practice to have a
         * "normalized", or standard when storing the date and adjust as necessary when
         * displaying the date. Normalizing the date also allows us an easy way to convert to
         * local time at midnight, as all we have to do is add a particular time zone's GMT
         * offset to this date to get local time at midnight on the appropriate date.
         */
        public static final String COLUMN_DATE_TIME = "datetime";
        public static final String COLUMN_TRACKNAME = "session_trackname";
        public static final String COLUMN_DRIVER = "session_driver";
        public static final String COLUMN_LAPTIMES = "session_laptimes";
        public static final String COLUMN_NUMBEROFLAPS = "session_numberoflaps";
        public static final String COLUMN_BESTLAP = "session_bestlap";

    }
}
