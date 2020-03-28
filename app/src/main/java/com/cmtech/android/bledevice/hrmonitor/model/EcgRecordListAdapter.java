package com.cmtech.android.bledevice.hrmonitor.model;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.view.EcgRecordExplorerActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

import static com.cmtech.android.bledeviceapp.activity.LoginActivity.SUPPORT_PLATFORM;


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

public class EcgRecordListAdapter extends RecyclerView.Adapter<EcgRecordListAdapter.ViewHolder>{
    private final EcgRecordExplorerActivity activity;
    private final List<BleEcgRecord10> allRecordList;
    private int selPos = -1;
    private Drawable defaultBg; // 缺省背景

    class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvTimeLength; // record time length, unit: s
        TextView tvAddress;
        ImageButton ibDelete;

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvTimeLength = fileView.findViewById(R.id.tv_time_length);
            tvAddress = fileView.findViewById(R.id.tv_device_address);
            ibDelete = fileView.findViewById(R.id.ib_delete);
        }
    }

    public EcgRecordListAdapter(EcgRecordExplorerActivity activity, List<BleEcgRecord10> allRecordList) {
        this.activity = activity;
        this.allRecordList = allRecordList;
    }

    @NonNull
    @Override
    public EcgRecordListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_record, parent, false);

        final EcgRecordListAdapter.ViewHolder holder = new EcgRecordListAdapter.ViewHolder(view);

        defaultBg = holder.fileView.getBackground();

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int before = selPos;
                selPos = holder.getAdapterPosition();
                if(before >= 0 && before < allRecordList.size()) {
                    notifyItemChanged(before);
                }
                notifyItemChanged(selPos);
                activity.selectRecord(allRecordList.get(selPos));
            }
        });

        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.deleteRecord(allRecordList.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EcgRecordListAdapter.ViewHolder holder, final int position) {
        BleEcgRecord10 record = allRecordList.get(position);
        if(record == null) return;
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);
        holder.tvCreator.setText(record.getCreatorName());
        Drawable drawable = ContextCompat.getDrawable(activity, SUPPORT_PLATFORM.get(record.getCreatorPlat()));
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        holder.tvCreator.setCompoundDrawables(null, drawable, null, null);

        List<Short> ecgList = record.getEcgList();
        if(ecgList == null || ecgList.size() == 0)
            holder.tvTimeLength.setText(String.valueOf(0));
        else {
            holder.tvTimeLength.setText(""+ecgList.size()/record.getSampleRate());
        }
        holder.tvAddress.setText(record.getDevAddress());

        if(position == selPos) {
            int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBg);
        }
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
