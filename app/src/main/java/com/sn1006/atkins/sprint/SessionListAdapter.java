package com.sn1006.atkins.sprint;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sn1006.atkins.sprint.data.SessionContract;

import java.util.ArrayList;

public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.SessionViewHolder>{

    private Context mContext;
    private Cursor mCursor;
    final private SessionAdapterOnClickHandler mClickHandler;
    private ArrayList<Long> mListOfLaps = new ArrayList<Long>();

    //Constructor for the adapter. Depending if a item was click, mClickHandler may or may not be
    //initialized
    public SessionListAdapter(Context context, Cursor cursor, SessionAdapterOnClickHandler clickHandler){
        this.mContext = context;
        this.mCursor = cursor;
        mClickHandler = clickHandler;

    }

    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.session_list_item, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SessionViewHolder holder, int position) {
        // Move the mCursor to the position of the item to be displayed
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null

        // Update the view holder with the information needed to display
        String trackName = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_TRACKNAME));
        String dateStamp = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_DATE_TIME));
        String driverName = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_DRIVER));
        String numberOfLaps = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_NUMBEROFLAPS));
        String listOfLapsString = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_LAPTIMES));
        String bestLap = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_BESTLAP));


        convertStringToArray(listOfLapsString);
        long avgLap = totalSessionTime() / Long.parseLong(numberOfLaps);

        holder.driverNameTextView.setText(driverName);
        holder.dateTimeTextView.setText(dateStamp);
        holder.sessionNameTextView.setText(trackName);
        holder.numberOfLapsTextView.setText("Total Laps: " + numberOfLaps);
        holder.bestLapTextView.setText("Best Lap: " + formatLaptime(Long.parseLong(bestLap)));
        holder.averageLapTextView.setText("Avg. Lap: " + formatLaptime(avgLap));
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    /**
     * Inner class to hold the views needed to display a single item in the recycler-view
     */
    class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView driverNameTextView;
        TextView dateTimeTextView;
        TextView sessionNameTextView;
        TextView bestLapTextView;
        TextView averageLapTextView;
        TextView numberOfLapsTextView;

        public SessionViewHolder(View itemView) {
            super(itemView);
            driverNameTextView = (TextView) itemView.findViewById(R.id.driverName);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTime);
            sessionNameTextView = (TextView) itemView.findViewById(R.id.sessionName);
            numberOfLapsTextView = (TextView) itemView.findViewById(R.id.numOfLaps);
            averageLapTextView = (TextView) itemView.findViewById(R.id.avgLap);
            bestLapTextView = (TextView) itemView.findViewById(R.id.bestLap);
            itemView.setOnClickListener(this);

        }
        @Override
        public void onClick (View v){
            //When a session item is clicked, get its position and send it to SessionListActivity
            //to send with the Intent to open LapListActivity
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(adapterPosition);
        }
    }

    public void convertStringToArray(String str) {
        mListOfLaps.clear();
        for (String s : str.split(",")) {
            mListOfLaps.add(Long.parseLong(s));
        }
    }

    public long totalSessionTime(){
        long totalSessionTime = 0;
        for(long singleLap : mListOfLaps){
            totalSessionTime += singleLap;
        }
        return totalSessionTime;
    }

    public String formatLaptime(Long laptime) {
        int mins;
        int secs;
        int millis;

        mins = (int) (laptime / 60000);
        secs = (int) (laptime - mins * 60000) / 1000;
        millis = (int) (laptime - mins * 60000 - secs * 1000);

        return (String.format("%02d", mins) + ":" + String.format("%02d", secs) + ":"
                + String.format("%03d", millis));

    }
    //Interface used in SessionListActivity to handle the transfer information about what item was
    //clicked
    public interface SessionAdapterOnClickHandler {
        void onClick (int clickedItemIndex);
    }


}