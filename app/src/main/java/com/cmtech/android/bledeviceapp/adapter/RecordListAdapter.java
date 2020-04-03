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
import com.cmtech.android.bledevice.thermo.model.BleThermoRecord10;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledevice.interf.IRecord;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

import static com.cmtech.android.bledeviceapp.activity.LoginActivity.PLATFORM_NAME_ICON_PAIR;


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

public class RecordListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int TYPE_HR = 0; // hr record
    private static final int TYPE_ECG = 1; // ecg record
    private static final int TYPE_THERMO = 2; // thermo record

    private final RecordExplorerActivity activity;
    private final List<IRecord> allRecords;
    private int selPos = -1;
    private Drawable defaultBg; // 缺省背景

    static class HrViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvTimeLength; // record time length, unit: s
        TextView tvAddress;
        ImageView ivType;
        ImageButton ibDelete;

        HrViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvTimeLength = fileView.findViewById(R.id.tv_time_length);
            tvAddress = fileView.findViewById(R.id.tv_device_address);
            ivType = fileView.findViewById(R.id.iv_record_type);
            ibDelete = fileView.findViewById(R.id.ib_delete);
        }
    }

    static class EcgViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvTimeLength; // record time length, unit: s
        TextView tvAddress;
        ImageView ivType; // record type
        ImageButton ibDelete;

        EcgViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvTimeLength = fileView.findViewById(R.id.tv_time_length);
            tvAddress = fileView.findViewById(R.id.tv_device_address);
            ivType = fileView.findViewById(R.id.iv_record_type);
            ibDelete = fileView.findViewById(R.id.ib_delete);
        }
    }

    static class ThermoViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvTimeLength;
        TextView tvAddress;
        ImageView ivType; // record type
        ImageButton ibDelete;

        ThermoViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvTimeLength = fileView.findViewById(R.id.tv_time_length);
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HR) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_record_hr, parent, false);
            final HrViewHolder holder = new HrViewHolder(view);
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

        if (viewType == TYPE_ECG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_record_ecg, parent, false);
            final EcgViewHolder holder = new EcgViewHolder(view);

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

        if (viewType == TYPE_THERMO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_record_thermo, parent, false);
            final ThermoViewHolder holder = new ThermoViewHolder(view);

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
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof HrViewHolder) {
            HrViewHolder hrHolder = (HrViewHolder) holder;
            BleHrRecord10 record = (BleHrRecord10) allRecords.get(position);

            if(record == null) return;
            hrHolder.ivType.setImageResource(R.mipmap.ic_hr_24px);

            String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
            hrHolder.tvCreateTime.setText(createTime);
            hrHolder.tvCreator.setText(record.getCreatorName());
            Drawable drawable = ContextCompat.getDrawable(activity, PLATFORM_NAME_ICON_PAIR.get(record.getCreatorPlat()));
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            hrHolder.tvCreator.setCompoundDrawables(null, drawable, null, null);

            int time = (record.getRecordSecond() <= 60) ? 1 : record.getRecordSecond()/60;
            hrHolder.tvTimeLength.setText(time+"分钟");
            hrHolder.tvAddress.setText(record.getDevAddress());

            if(position == selPos) {
                int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
                hrHolder.fileView.setBackgroundColor(bgdColor);
            } else {
                hrHolder.fileView.setBackground(defaultBg);
            }
        }

        if(holder instanceof EcgViewHolder) {
            EcgViewHolder ecgHolder = (EcgViewHolder) holder;
            BleEcgRecord10 record = (BleEcgRecord10) allRecords.get(position);
            if(record == null) return;

            ecgHolder.ivType.setImageResource(R.mipmap.ic_ecg_24px);

            String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
            ecgHolder.tvCreateTime.setText(createTime);
            ecgHolder.tvCreator.setText(record.getCreatorName());
            Drawable drawable = ContextCompat.getDrawable(activity, PLATFORM_NAME_ICON_PAIR.get(record.getCreatorPlat()));
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            ecgHolder.tvCreator.setCompoundDrawables(null, drawable, null, null);

            ecgHolder.tvTimeLength.setText(record.getRecordSecond()+"秒");

            ecgHolder.tvAddress.setText(record.getDevAddress());

            if(position == selPos) {
                int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
                ecgHolder.fileView.setBackgroundColor(bgdColor);
            } else {
                ecgHolder.fileView.setBackground(defaultBg);
            }
        }

        if(holder instanceof ThermoViewHolder) {
            ThermoViewHolder thermoHolder = (ThermoViewHolder) holder;
            BleThermoRecord10 record = (BleThermoRecord10) allRecords.get(position);

            if(record == null) return;
            thermoHolder.ivType.setImageResource(R.mipmap.ic_hr_24px);

            String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
            thermoHolder.tvCreateTime.setText(createTime);
            thermoHolder.tvCreator.setText(record.getCreatorName());
            Drawable drawable = ContextCompat.getDrawable(activity, PLATFORM_NAME_ICON_PAIR.get(record.getCreatorPlat()));
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            thermoHolder.tvCreator.setCompoundDrawables(null, drawable, null, null);

            thermoHolder.tvTimeLength.setText((int)record.getHighestTemp());
            thermoHolder.tvAddress.setText(record.getDevAddress());

            if(position == selPos) {
                int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
                thermoHolder.fileView.setBackgroundColor(bgdColor);
            } else {
                thermoHolder.fileView.setBackground(defaultBg);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        IRecord record = allRecords.get(position);
        if (record instanceof BleHrRecord10) {
            return TYPE_HR;
        } else if (record instanceof BleEcgRecord10) {
            return TYPE_ECG;
        } else {
            return TYPE_THERMO;
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
