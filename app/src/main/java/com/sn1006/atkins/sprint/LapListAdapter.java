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

public class LapListAdapter extends RecyclerView.Adapter<LapListAdapter.LapViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private ArrayList<Long> mListOfLaps = new ArrayList<Long>();


    //uses db cursor
    public LapListAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public LapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.lap_list_item, parent, false);
        return new LapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LapListAdapter.LapViewHolder holder, int position) {
        // Move the mCursor to the position of the item to be displayed
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null

        // Update the view holder with the information needed to display

/*        String lapTimes = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_LAPTIMES));
        if(!lapTimes.equals("")){
            convertStringToArray(lapTimes);
            long lap = mListOfLaps.get(position);
            holder.lapTimeTextView.setText(String.valueOf(formatLaptime(lap)));
        }else{
            holder.lapTimeTextView.setText("No laps to show");
        }*/

        String lapTimes = mCursor.getString(mCursor.getColumnIndex(SessionContract.SessionEntry.COLUMN_LAPTIMES));
        if(!lapTimes.equals("")){
            convertStringToArray(lapTimes);
            for (long x : mListOfLaps){
                holder.lapTimeTextView.setText(String.valueOf(formatLaptime(x)));
            }
        }else{
            holder.lapTimeTextView.setText("No laps to show");
        }

    }

    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (mCursor != null) mCursor.close();
        // COMPLETED (17) Update the local mCursor to be equal to  newCursor
        mCursor = newCursor;
        // COMPLETED (18) Check if the newCursor is not null, and call this.notifyDataSetChanged() if so
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    /**
     * Inner class to hold the views needed to display a single item in the recycler-view
     */
    class LapViewHolder extends RecyclerView.ViewHolder {

        TextView lapTimeTextView;

        public LapViewHolder(View itemView) {
            super(itemView);
            lapTimeTextView = (TextView) itemView.findViewById(R.id.lapTime);

        }
    }

    public void convertStringToArray(String str){
        for(String s : str.split(",")){
            mListOfLaps.add(Long.parseLong(s));
        }
    }

    //takes laptime from Long format and makes it mm:ss:xx
    public String formatLaptime(Long laptime) {
        int mins;
        int secs;
        int millis;

        mins = (int) (laptime/60000);
        secs = (int) (laptime - mins*60000)/1000;
        millis = (int) (laptime - mins*60000 - secs*1000);

        return (String.format("%02d",mins) + ":" + String.format("%02d",secs) + ":"
                + String.format("%02d",millis));
    }
}