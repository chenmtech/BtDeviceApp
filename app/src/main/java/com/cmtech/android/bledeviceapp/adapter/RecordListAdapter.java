package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledevice.record.RecordType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.utils.view.BitmapUtil;

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
    private static final int SELECT_BG_COLOR = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
    private final RecordExplorerActivity activity;
    private final List<IRecord> allRecords;
    private int selPos = -1;
    private Drawable defaultBg; // 缺省背景

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        TextView tvCreateTime; // 创建时间
        TextView tvCreator; // 创建人
        TextView tvDesc; // record description
        TextView tvAddress;
        ImageView ivType;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvCreateTime = view.findViewById(R.id.tv_create_time);
            tvCreator = view.findViewById(R.id.tv_creator);
            tvDesc = view.findViewById(R.id.tv_desc);
            tvAddress = view.findViewById(R.id.tv_device_address);
            ivType = view.findViewById(R.id.iv_record_type);
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
        defaultBg = holder.view.getBackground();

        holder.view.setOnClickListener(new View.OnClickListener() {
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
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        IRecord record = allRecords.get(position);
        if(record == null) return;

        holder.ivType.setImageResource(RecordType.getType(record.getTypeCode()).getImgId());

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        holder.tvCreator.setText(record.getCreatorName());

        Drawable drawable;
        if(TextUtils.isEmpty(AccountManager.getAccount().getLocalIcon())) {
            drawable = ContextCompat.getDrawable(activity, SUPPORT_LOGIN_PLATFORM.get(record.getCreatorPlat()));
        } else {
            Bitmap bitmap = BitmapUtil.getSmallBitmap(AccountManager.getAccount().getLocalIcon(), 80, 80);
            drawable = BitmapUtil.bitmapToDrawable(bitmap);
        }
        drawable.setBounds(0,0,80,80);
        holder.tvCreator.setCompoundDrawables(null, drawable, null, null);

        holder.tvAddress.setText(record.getDevAddress());

        holder.tvDesc.setText(record.getDesc());

        if(position == selPos) {
            holder.view.setBackgroundColor(SELECT_BG_COLOR);
        } else {
            holder.view.setBackground(defaultBg);
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

    public IRecord getSelectedRecord() {
        if(selPos == -1) return null;
        return allRecords.get(selPos);
    }

    public void setSelectedPosition(int position) {
        if(position < allRecords.size())
            selPos = position;
    }
}
