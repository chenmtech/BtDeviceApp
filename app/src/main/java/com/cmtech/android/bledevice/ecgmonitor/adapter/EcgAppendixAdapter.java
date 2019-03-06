package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgAppendixAdapter extends RecyclerView.Adapter<EcgAppendixAdapter.ViewHolder> {
    private List<EcgAppendix> appendixList; // 附加信息列表
    private final IEcgAppendixOperator appendixOperator; // 附加信息操作者
    private int sampleRate;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View appendixView;
        TextView tvCreatorName;
        TextView tvCreatorTime;
        EditText etContent;
        ImageButton ibLocate;
        ImageButton ibDelete;

        private ViewHolder(View itemView) {
            super(itemView);
            appendixView = itemView;
            etContent = appendixView.findViewById(R.id.ecgappendix_content);
            tvCreatorName = appendixView.findViewById(R.id.ecgappendix_creator);
            tvCreatorTime = appendixView.findViewById(R.id.ecgappendix_createtime);
            ibLocate = appendixView.findViewById(R.id.ib_ecgappendix_locate);
            ibDelete = appendixView.findViewById(R.id.ib_ecgappendix_delete);
        }
    }

    public EcgAppendixAdapter(List<EcgAppendix> appendixList, IEcgAppendixOperator appendixOperator, int sampleRate) {
        this.appendixList = appendixList;
        this.appendixOperator = appendixOperator;
        this.sampleRate = sampleRate;
    }

    @NonNull
    @Override
    public EcgAppendixAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgappendix, parent, false);

        final EcgAppendixAdapter.ViewHolder holder = new EcgAppendixAdapter.ViewHolder(view);

        holder.tvCreatorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserAccount creator = appendixList.get(holder.getAdapterPosition()).getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_LONG).show();
            }
        });

        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(appendixOperator != null) {
                    appendixOperator.deleteAppendix(appendixList.get(holder.getAdapterPosition()));
                }
            }
        });

        holder.ibLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(appendixOperator != null) {
                    //appendixOperator.locateAppendix(appendixList.get(holder.getAdapterPosition()));
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EcgAppendixAdapter.ViewHolder holder, final int position) {
        EcgAppendix appendix = appendixList.get(position);
        holder.tvCreatorName.setText(Html.fromHtml("<u>"+appendix.getCreatorName()+"</u>"));
        holder.tvCreatorTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(appendix.getCreateTime()));
        holder.etContent.setText(appendix.getContent());

        if(appendixOperator != null) {
            holder.ibDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return appendixList.size();
    }

    public void update(List<EcgAppendix> commentList, int sampleRate) {
        this.appendixList = commentList;
        this.sampleRate = sampleRate;
        notifyDataSetChanged();
    }

    public void update(List<EcgAppendix> appendixList) {
        this.appendixList = appendixList;
        notifyDataSetChanged();
    }
}
