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
    private ArrayList<Long> mListOfLaps = new ArrayList<Long>();


    //uses db cursor
    public LapListAdapter(Context context, ArrayList<Long> laps) {
        this.mContext = context;
        this.mListOfLaps = laps;
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

        long singleLap = mListOfLaps.get(position);

        if(singleLap > 0) {
            holder.lapTimeTextView.setText(String.valueOf(formatLaptime(singleLap)));
            holder.lapNumberTextView.setText(String.valueOf(position+1) + ". ");
        }else{
            holder.lapTimeTextView.setText("No laps to show");
        }

    }

    @Override
    public int getItemCount() {
        return mListOfLaps.size();
    }

    /**
     * Inner class to hold the views needed to display a single item in the recycler-view
     */
    class LapViewHolder extends RecyclerView.ViewHolder {

        TextView lapTimeTextView;
        TextView lapNumberTextView;

        public LapViewHolder(View itemView) {
            super(itemView);
            lapTimeTextView = (TextView) itemView.findViewById(R.id.lapTime);
            lapNumberTextView = (TextView) itemView.findViewById(R.id.lapNumber);

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