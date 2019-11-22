package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;

import java.util.List;

public class EcgReceiverAdapter extends RecyclerView.Adapter<EcgReceiverAdapter.ViewHolder> {
    private final List<Account> receivers;
    private boolean enable = false;

    public interface OnReceiverChangedListener {
        void onReceiverChanged(Account receiver, boolean isChecked);
    }
    private final OnReceiverChangedListener listener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbReceiver;

        ViewHolder(View itemView) {
            super(itemView);
            cbReceiver = itemView.findViewById(R.id.cb_ecg_signal_receiver);
        }
    }

    public EcgReceiverAdapter(List<Account> receivers, OnReceiverChangedListener listener) {
        this.receivers = receivers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EcgReceiverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_receiver, parent, false);
        final EcgReceiverAdapter.ViewHolder holder = new EcgReceiverAdapter.ViewHolder(view);

        holder.cbReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onReceiverChanged(receivers.get(holder.getAdapterPosition()), holder.cbReceiver.isChecked());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgReceiverAdapter.ViewHolder holder, final int position) {
        Account receiver = receivers.get(position);
        holder.cbReceiver.setText(receiver.getName());
        holder.cbReceiver.setEnabled(enable);
    }

    @Override
    public int getItemCount() {
        return receivers.size();
    }

    public void setEnabled(boolean enable) {
        this.enable = enable;
        notifyDataSetChanged();
    }
}
