package com.cmtech.android.bledevice.hrmonitor.model;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecg.activity.EcgRecordExplorerActivity;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledevice.hrmonitor.view.HrRecordExplorerActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import java.util.List;


/**
  *
  * ClassName:      EcgRecordListAdapter
  * Description:    Ecg记录列表Adapter
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2018/11/10 下午4:09
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class HrRecordListAdapter extends RecyclerView.Adapter<HrRecordListAdapter.ViewHolder>{
    private final HrRecordExplorerActivity activity;
    private final List<BleHrRecord10> allRecordList;

    class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvHrNum; // 心率次数

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvHrNum = fileView.findViewById(R.id.tv_hr_num);
        }
    }

    public HrRecordListAdapter(HrRecordExplorerActivity activity, List<BleHrRecord10> allRecordList) {
        this.activity = activity;
        this.allRecordList = allRecordList;
    }

    @NonNull
    @Override
    public HrRecordListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_hr_record, parent, false);

        final HrRecordListAdapter.ViewHolder holder = new HrRecordListAdapter.ViewHolder(view);

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectRecord(allRecordList.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HrRecordListAdapter.ViewHolder holder, final int position) {
        ViseLog.e("onBindViewHolder " + position);
        BleHrRecord10 record = allRecordList.get(position);
        if(record == null) return;
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);
        holder.tvCreator.setText(record.getCreatorName());

        if(record.getHrList() == null)
            holder.tvHrNum.setText(String.valueOf(0));
        else
            holder.tvHrNum.setText(String.valueOf(record.getHrList().size()));

    }
    @Override
    public int getItemCount() {
        return allRecordList.size();
    }

    public void updateRecordList() {
        notifyDataSetChanged();
    }

    public void clear() {
        allRecordList.clear();
    }
}
