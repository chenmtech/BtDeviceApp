package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendixDataLocation;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgAppendixAdapter extends RecyclerView.Adapter<EcgAppendixAdapter.ViewHolder> {
    private List<IEcgAppendix> appendixList; // 附加信息列表
    private final IEcgAppendixOperator appendixOperator; // 附加信息操作者
    private int sampleRate;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View appendixView;
        TextView tvCreatorName;
        TextView tvCreatorTime;
        TextView tvContent;
        ImageButton ibLocate;
        ImageButton ibDelete;

        private ViewHolder(View itemView) {
            super(itemView);
            appendixView = itemView;
            tvContent = appendixView.findViewById(R.id.ecgappendix_content);
            tvCreatorName = appendixView.findViewById(R.id.ecgappendix_creator);
            tvCreatorTime = appendixView.findViewById(R.id.ecgappendix_createtime);
            ibLocate = appendixView.findViewById(R.id.ib_ecgappendix_locate);
            ibDelete = appendixView.findViewById(R.id.ib_ecgappendix_delete);
        }
    }

    public EcgAppendixAdapter(List<IEcgAppendix> appendixList, IEcgAppendixOperator appendixOperator, int sampleRate) {
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
                /*if(appendixOperator != null) {
                    appendixOperator.deleteAppendix(appendixList.get(holder.getAdapterPosition()));
                }*/
                if(appendixOperator != null) {
                    int pos = holder.getAdapterPosition();
                    String content = appendixList.get(pos).toString();
                    appendixOperator.insertAppendix("回复：" + content, pos+1);
                }
            }
        });

        holder.ibLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(appendixOperator != null)
                    appendixOperator.locateAppendix(appendixList.get(holder.getAdapterPosition()));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgAppendixAdapter.ViewHolder holder, final int position) {
        IEcgAppendix appendix = appendixList.get(position);
        holder.tvCreatorName.setText(Html.fromHtml("<u>"+appendix.getCreatorName()+"</u>"));

        holder.tvCreatorTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(appendix.getCreateTime()));
        holder.tvContent.setText(appendix.toStringWithSampleRate(sampleRate));

        if(appendixOperator != null) {
            holder.ibDelete.setVisibility(View.VISIBLE);
            if(appendix instanceof IEcgAppendixDataLocation) {
                holder.ibLocate.setVisibility(View.VISIBLE);
            } else {
                holder.ibLocate.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return appendixList.size();
    }

    public void update(List<IEcgAppendix> commentList, int sampleRate) {
        this.appendixList = commentList;
        this.sampleRate = sampleRate;
        notifyDataSetChanged();
    }

    public void update(List<IEcgAppendix> appendixList) {
        this.appendixList = appendixList;
        notifyDataSetChanged();
    }
}
