package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;

import org.litepal.LitePal;

import java.util.List;


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
    private static final int INVALID_POS = -1;

    private final RecordExplorerActivity activity;
    private final List<BasicRecord> records;
    private int position = INVALID_POS;


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

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
            tvNote = view.findViewById(R.id.tv_time_length);
            ivUpload = view.findViewById(R.id.iv_need_upload);
            ivDelete = view.findViewById(R.id.iv_delete);
            llRecordInfo = view.findViewById(R.id.ll_basic_info);
        }
    }

    public RecordListAdapter(RecordExplorerActivity activity, List<BasicRecord> records) {
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

                if(ClickCheckUtil.isFastClick()) return;

                int prePos = position;
                position = holder.getAdapterPosition();
                if(prePos >= 0 && prePos < records.size()) {
                    notifyItemChanged(prePos);
                }
                notifyItemChanged(position);
                activity.openRecord(position);

                /*BasicRecord record = records.get(position);
                record = LitePal.find(record.getClass(), record.getId(), true);
                records.set(position, record);
                ViseLog.e("opening record" + isOpeningRecord);
                if(record.noSignal()) {
                    record.download(activity, code -> {
                        if (code == RETURN_CODE_SUCCESS) {
                            activity.openRecord(records.get(position));
                        } else {
                            Toast.makeText(activity, "无法打开记录，请检查网络是否正常。", Toast.LENGTH_SHORT).show();
                        }
                        isOpeningRecord = false;
                    });
                } else {
                    activity.openRecord(records.get(position));
                    isOpeningRecord = false;
                }*/
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                activity.deleteRecord(records.get(holder.getAdapterPosition()));
            }
        });

        holder.ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                BasicRecord record = records.get(holder.getAdapterPosition());
                if(record == null) {
                    Toast.makeText(activity, "记录已损坏。", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(record.noSignal()) {
                    record = LitePal.find(record.getClass(), record.getId(), true);
                    records.set(holder.getAdapterPosition(), record);
                }
                activity.uploadRecord(record);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        BasicRecord record = records.get(position);
        if(record == null) return;

        holder.tvCreatorName.setText(record.getCreatorNickName());
        Account account = MyApplication.getAccount();
        if(TextUtils.isEmpty(account.getIcon())) {
            // load icon by platform name
            holder.ivCreatorImage.setImageResource(R.mipmap.ic_user);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(account.getIcon(),  32);
            holder.ivCreatorImage.setImageBitmap(bitmap);
        }

        holder.ivRecordType.setImageResource(RecordType.fromCode(record.getTypeCode()).getIconId());
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
            holder.view.setBackgroundColor(ContextCompat.getColor(activity, R.color.secondary));
        } else {
            holder.view.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary));
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public BasicRecord getSelectedRecord() {
        if(position == INVALID_POS) return null;
        return records.get(position);
    }

    public void unselected() {
        position = INVALID_POS;
    }

    public void notifySelectedItemChanged() {
        if(position == INVALID_POS) return;
        BasicRecord record = records.get(position);
        records.set(position, LitePal.find(record.getClass(), record.getId(), true));
        notifyItemChanged(position);
    }
}
