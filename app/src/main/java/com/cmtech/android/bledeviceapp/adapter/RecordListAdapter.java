package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledevice.common.AbstractRecord;
import com.cmtech.android.bledevice.thermo.model.BleThermoRecord10;
import com.cmtech.android.bledevice.thm.model.BleTempHumidRecord10;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledevice.common.IRecord;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

import static com.cmtech.android.bledeviceapp.AppConstant.SUPPORT_LOGIN_PLATFORM;


/**
  *
  * ClassName:      RecordListAdapter
  * Description:    记录列表Adapter
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2018/11/10 下午4:09
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder>{
    private final RecordExplorerActivity activity;
    private final List<IRecord> allRecords;
    private int selPos = -1;
    private Drawable defaultBg; // 缺省背景

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvDesc; // record description
        TextView tvAddress;
        ImageView ivType;
        ImageButton ibDelete;

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvDesc = fileView.findViewById(R.id.tv_desc);
            tvAddress = fileView.findViewById(R.id.tv_device_address);
            ivType = fileView.findViewById(R.id.iv_record_type);
            ibDelete = fileView.findViewById(R.id.ib_delete);
        }
    }

    public RecordListAdapter(RecordExplorerActivity activity, List<IRecord> allRecords) {
        this.activity = activity;
        this.allRecords = allRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_record, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        defaultBg = holder.fileView.getBackground();

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int prePos = selPos;
                selPos = holder.getAdapterPosition();
                if(prePos >= 0 && prePos < allRecords.size()) {
                    notifyItemChanged(prePos);
                }
                notifyItemChanged(selPos);
                activity.selectRecord(allRecords.get(selPos));
            }
        });

        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.deleteRecord(allRecords.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        IRecord record = allRecords.get(position);
        if(record == null) return;

        if(record instanceof BleHrRecord10) {
            holder.ivType.setImageResource(R.mipmap.ic_hr_24px);
        } else if(record instanceof BleEcgRecord10) {
            holder.ivType.setImageResource(R.mipmap.ic_ecg_24px);
        } else if(record instanceof BleThermoRecord10) {
            holder.ivType.setImageResource(R.mipmap.ic_thermo_24px);
        } else if(record instanceof BleTempHumidRecord10) {
            holder.ivType.setImageResource(R.drawable.ic_thm_default_icon);
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        holder.tvCreator.setText(record.getCreatorName());
        Drawable drawable = ContextCompat.getDrawable(activity, SUPPORT_LOGIN_PLATFORM.get(record.getCreatorPlat()));
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        holder.tvCreator.setCompoundDrawables(null, drawable, null, null);

        holder.tvAddress.setText(record.getDevAddress());

        holder.tvDesc.setText(record.getDesc());

        if(position == selPos) {
            int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBg);
        }
    }

    @Override
    public int getItemCount() {
        return allRecords.size();
    }

    public void updateRecordList() {
        notifyDataSetChanged();
    }

    public void clear() {
        allRecords.clear();
    }
}
