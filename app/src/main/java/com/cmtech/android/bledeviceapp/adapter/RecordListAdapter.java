package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
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

        LinearLayout llDiagnoseResult;
        TextView tvDiagnoseResult;

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

            llDiagnoseResult = view.findViewById(R.id.layout_diagnose);
            tvDiagnoseResult = view.findViewById(R.id.tv_diagnose_result);
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BasicRecord record = records.get(position);
        if(record == null) return;

        holder.tvCreatorName.setText(record.getCreatorNickNameOrId());
        String icon = record.getCreatorIcon();
        if(TextUtils.isEmpty(icon)) {
            holder.ivCreatorImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(icon,  32);
            holder.ivCreatorImage.setImageBitmap(bitmap);
        }

        holder.ivRecordType.setImageResource(RecordType.fromCode(record.getTypeCode()).getIconId());
        String createTime = DateTimeUtil.timeToStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        holder.tvAddress.setText(record.getDevAddress());

        if(TextUtils.isEmpty(record.getComment())) {
            holder.tvNote.setText(R.string.null_content);
        } else {
            holder.tvNote.setText(record.getComment());
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

        holder.llRecordInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ClickCheckUtil.isFastClick()) return;
                activity.openRecord(holder.getAdapterPosition());
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                activity.deleteRecord(holder.getAdapterPosition());
            }
        });

        holder.ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                activity.uploadRecord(holder.getAdapterPosition());
            }
        });

        holder.tvDiagnoseResult.setText(record.getReportContent());

        holder.llRecordInfo.setOnLongClickListener(new View.OnLongClickListener() {
            final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {         //设置每个菜单的点击动作
                    switch (item.getItemId()){
                        case 1:
                            if(ClickCheckUtil.isFastClick()) return true;
                            activity.openRecord(holder.getAdapterPosition());
                            break;
                        case 2:
                            if(ClickCheckUtil.isFastClick()) return true;
                            activity.deleteRecord(holder.getAdapterPosition());
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            };

            @Override
            public boolean onLongClick(View view) {
                view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        MenuItem open = menu.add(Menu.NONE, 1, 0, "打开");
                        MenuItem delete = menu.add(Menu.NONE, 2, 0, R.string.delete);
                        open.setOnMenuItemClickListener(listener);            //响应点击事件
                        delete.setOnMenuItemClickListener(listener);            //响应点击事件
                    }
                });
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public BasicRecord getSelectedRecord() {
        if(position == INVALID_POS) return null;
        return records.get(position);
    }

    public void setSelectedRecord(int position) {
        this.position = position;
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
