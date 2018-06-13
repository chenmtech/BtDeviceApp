package com.cmtech.android.btdevice.temphumid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;

import java.text.DateFormat;
import java.util.List;

public class TempHumidHistoryDataAdapter extends RecyclerView.Adapter<TempHumidHistoryDataAdapter.ViewHolder> {
    List<TempHumidData> dataList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView historyTime;
        TextView historyTemp;
        TextView historyHumid;


        public ViewHolder(View itemView) {
            super(itemView);
            historyTime = itemView.findViewById(R.id.tv_temphumid_historytime);
            historyTemp = itemView.findViewById(R.id.tv_temphumid_historytemp);
            historyHumid = itemView.findViewById(R.id.tv_temphumid_historyhumid);
        }
    }

    public TempHumidHistoryDataAdapter(List<TempHumidData> dataList) {
        this.dataList = dataList;
    }



    @Override
    public TempHumidHistoryDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_history_temphumid, parent, false);
        final TempHumidHistoryDataAdapter.ViewHolder holder = new TempHumidHistoryDataAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(TempHumidHistoryDataAdapter.ViewHolder holder, final int position) {
        TempHumidData data = (TempHumidData)dataList.get(position);

        holder.historyTime.setText(DateFormat.getDateTimeInstance().format(data.getTime().getTime()));
        holder.historyTemp.setText(data.getTemp()+"");
        holder.historyHumid.setText(data.getHumid()+"");
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}
