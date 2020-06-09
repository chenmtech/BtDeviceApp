package com.cmtech.android.bledeviceapp.adapter;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledevice.record.RecordType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

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
    private static final int SELECT_BG_COLOR = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
    private static final int INVALID_POS = -1;
    private final RecordExplorerActivity activity;
    private final List<IRecord> allRecords;
    private int selPos = INVALID_POS;
    private Drawable defaultBg; // default background

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        TextView tvCreatorName; //
        ImageView ivCreatorImage;
        TextView tvCreateTime; //
        TextView tvAddress;
        TextView tvNote; // record description
        ImageView ivType;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvCreatorName = view.findViewById(R.id.tv_creator_name);
            ivCreatorImage = view.findViewById(R.id.iv_creator_image);
            tvCreateTime = view.findViewById(R.id.tv_create_time);
            tvAddress = view.findViewById(R.id.tv_device_address);
            tvNote = view.findViewById(R.id.tv_note);
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
                activity.openRecord(allRecords.get(selPos));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        IRecord record = allRecords.get(position);
        if(record == null) return;

        holder.tvCreatorName.setText(record.getCreatorName());
        User account = AccountManager.getAccount();
        if(TextUtils.isEmpty(account.getLocalIcon())) {
            // load icon by platform name
            holder.ivCreatorImage.setImageResource(SUPPORT_LOGIN_PLATFORM.get(account.getPlatName()));
        } else {
            Glide.with(activity).load(account.getLocalIcon()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.ivCreatorImage);
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        holder.tvAddress.setText(record.getDevAddress());

        holder.tvNote.setText(record.getNote());

        holder.ivType.setImageResource(RecordType.getType(record.getTypeCode()).getImgId());

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

    public IRecord getSelectedRecord() {
        if(selPos == INVALID_POS) return null;
        return allRecords.get(selPos);
    }

    public void unselected() {
        selPos = INVALID_POS;
    }

    public void notifySelectedItemChanged() {
        ViseLog.e("activity result");
        IRecord record = allRecords.get(selPos);
        allRecords.set(selPos, LitePal.find(record.getClass(), record.getId()));
        notifyItemChanged(selPos);
    }
}
