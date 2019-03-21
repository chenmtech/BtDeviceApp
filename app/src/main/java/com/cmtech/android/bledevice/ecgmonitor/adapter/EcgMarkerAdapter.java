package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmtech.android.bledevice.ecgmonitor.activity.EcgMonitorFragment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgAbnormal;
import com.cmtech.android.bledeviceapp.R;

import java.util.List;

public class EcgMarkerAdapter extends RecyclerView.Adapter<EcgMarkerAdapter.ViewHolder> {
    private final List<EcgAbnormal> ecgAbnormals;
    private boolean enable = false;

    public interface OnMarkerClickListener {
        void onMarkerClicked(EcgAbnormal marker);
    }
    private final OnMarkerClickListener listener;

    static class ViewHolder extends RecyclerView.ViewHolder {

        Button btnMarker;

        ViewHolder(View itemView) {
            super(itemView);

            btnMarker = itemView.findViewById(R.id.btn_ecg_marker);
        }
    }

    public EcgMarkerAdapter(List<EcgAbnormal> ecgAbnormals, OnMarkerClickListener listener) {
        this.ecgAbnormals = ecgAbnormals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EcgMarkerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_marker, parent, false);
        final EcgMarkerAdapter.ViewHolder holder = new EcgMarkerAdapter.ViewHolder(view);

        holder.btnMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onMarkerClicked(ecgAbnormals.get(holder.getAdapterPosition()));
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgMarkerAdapter.ViewHolder holder, final int position) {
        EcgAbnormal abnormal = ecgAbnormals.get(position);
        holder.btnMarker.setText(abnormal.getDescription());
        holder.btnMarker.setEnabled(enable);
    }

    @Override
    public int getItemCount() {
        return ecgAbnormals.size();
    }

    public void setEnabled(boolean enable) {
        this.enable = enable;
        notifyDataSetChanged();
    }

}
