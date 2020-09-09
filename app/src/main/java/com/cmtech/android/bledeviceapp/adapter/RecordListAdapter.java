package com.cmtech.android.bledeviceapp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledevice.record.BasicRecord;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledevice.record.RecordFactory;
import com.cmtech.android.bledevice.record.RecordType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.util.FastClickUtil;

import org.litepal.LitePal;

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
    private static final int SELECT_COLOR = R.color.accent;
    private static final int UNSELECT_COLOR = R.color.primary_dark;
    private static final int INVALID_POS = -1;

    private final RecordExplorerActivity activity;
    private final List<IRecord> records;
    private int position = INVALID_POS;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        View selectIndicate;
        TextView tvCreatorName; //
        ImageView ivCreatorImage;
        ImageView ivRecordType;
        TextView tvCreateTime; //
        TextView tvAddress;
        TextView tvNote; //
        ImageView ivUpload;
        ImageView ivDelete;
        LinearLayout llRecordInfo;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvCreatorName = view.findViewById(R.id.tv_creator_name);
            ivCreatorImage = view.findViewById(R.id.iv_creator_image);
            tvCreateTime = view.findViewById(R.id.tv_create_time);
            ivRecordType = view.findViewById(R.id.iv_record_type);
            tvAddress = view.findViewById(R.id.tv_device_address);
            tvNote = view.findViewById(R.id.tv_note);
            ivUpload = view.findViewById(R.id.iv_need_upload);
            ivDelete = view.findViewById(R.id.iv_delete);
            llRecordInfo = view.findViewById(R.id.ll_basic_info);
            selectIndicate = view.findViewById(R.id.view_select_indicate);
        }
    }

    public RecordListAdapter(RecordExplorerActivity activity, List<IRecord> records) {
        this.activity = activity;
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_record, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.llRecordInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FastClickUtil.isFastClick()) return;

                int prePos = position;
                position = holder.getAdapterPosition();
                if(prePos >= 0 && prePos < records.size()) {
                    notifyItemChanged(prePos);
                }
                notifyItemChanged(position);
                activity.openRecord(records.get(position));
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FastClickUtil.isFastClick()) return;
                activity.deleteRecord(records.get(holder.getAdapterPosition()));
            }
        });

        holder.ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FastClickUtil.isFastClick()) return;
                BasicRecord record = (BasicRecord) records.get(holder.getAdapterPosition());
                record = (BasicRecord) RecordFactory.createFromLocalDb(RecordType.getType(record.getTypeCode()), record.getCreateTime(), record.getDevAddress());
                if(record == null || record.noSignal()) {
                    Toast.makeText(activity, R.string.record_damage, Toast.LENGTH_SHORT).show();
                    return;
                }
                records.set(holder.getAdapterPosition(), record);
                activity.uploadRecord(record);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        IRecord record = records.get(position);
        if(record == null) return;

        holder.tvCreatorName.setText(record.getCreatorName());
        Account account = MyApplication.getAccount();
        if(TextUtils.isEmpty(account.getIcon())) {
            // load icon by platform name
            holder.ivCreatorImage.setImageResource(SUPPORT_LOGIN_PLATFORM.get(account.getPlatName()));
        } else {
            Glide.with(activity).load(account.getIcon()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.ivCreatorImage);
        }

        holder.ivRecordType.setImageResource(RecordType.getType(record.getTypeCode()).getImgId());
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        holder.tvAddress.setText(record.getDevAddress());

        if(TextUtils.isEmpty(record.getNote())) {
            holder.tvNote.setText(R.string.null_content);
        } else {
            holder.tvNote.setText(record.getNote());
        }

        if(record.needUpload()) {
            holder.ivUpload.setVisibility(View.VISIBLE);
        } else {
            holder.ivUpload.setVisibility(View.GONE);
        }

        if(position == this.position) {
            holder.selectIndicate.setBackgroundResource(SELECT_COLOR);
        } else {
            holder.selectIndicate.setBackgroundResource(UNSELECT_COLOR);
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public IRecord getSelectedRecord() {
        if(position == INVALID_POS) return null;
        return records.get(position);
    }

    public void unselected() {
        position = INVALID_POS;
    }

    public void notifySelectedItemChanged() {
        IRecord record = records.get(position);
        records.set(position, LitePal.find(record.getClass(), record.getId()));
        notifyItemChanged(position);
    }
}
