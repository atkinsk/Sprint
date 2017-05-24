package com.sn1006.atkins.sprint;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LapListAdapter extends RecyclerView.Adapter<LapListAdapter.LapViewHolder> {
    private Context mContext;
    private ArrayList<Long> mListOfLaps = new ArrayList<Long>();
    private long bestLap;

    //uses db cursor
    public LapListAdapter(Context context, ArrayList<Long> laps, String bestLap) {
        this.mContext = context;
        this.mListOfLaps = laps;
        this.bestLap = Long.parseLong(bestLap);
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
        //Get the lap time at the position in the arraylist
        long singleLap = mListOfLaps.get(position);

        //If the lap time is greater than 0, returns the specific value. formatting the laptime
        //was crashing if handed a value of 0
        if(singleLap > 0) {
            holder.lapTimeTextView.setText(String.valueOf(formatLaptime(singleLap)));
            holder.lapNumberTextView.setText(String.valueOf(position+1) + ". ");

            if(singleLap == bestLap){
                holder.bestLapImageView.setVisibility(View.VISIBLE);
                holder.bestLapTextView.setVisibility(View.VISIBLE);
            }else{
                holder.bestLapImageView.setVisibility(View.INVISIBLE);
                holder.bestLapTextView.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.lapNumberTextView.setText(String.valueOf(position+1) + ". ");
            holder.lapTimeTextView.setText("00:00:00");
        }
    }
    //Since an ArrayList is required, need to know the size of the arraylist to let the adapter
    //know how many positions to cycle through in onBindViewHolder
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
        ImageView bestLapImageView;
        TextView bestLapTextView;

        public LapViewHolder(View itemView) {
            super(itemView);
            lapTimeTextView = (TextView) itemView.findViewById(R.id.lapTime);
            lapNumberTextView = (TextView) itemView.findViewById(R.id.lapNumber);
            bestLapImageView = (ImageView) itemView.findViewById(R.id.bestLapImage);
            bestLapTextView = (TextView) itemView.findViewById(R.id.bestLapText);

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