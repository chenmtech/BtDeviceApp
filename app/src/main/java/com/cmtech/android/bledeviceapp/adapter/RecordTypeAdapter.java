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
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.utils.view.BitmapUtil;

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

public class RecordTypeAdapter extends RecyclerView.Adapter<RecordTypeAdapter.ViewHolder>{
    private final RecordType[] types;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivType;
        TextView tvType; //

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivType = view.findViewById(R.id.iv_record_type);
            tvType = view.findViewById(R.id.tv_record_type_name);
        }
    }

    public RecordTypeAdapter(RecordType[] types) {
        this.types = types;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_record_type, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.ivType.setImageResource(types[position].getImgId());
        holder.tvType.setText(types[position].getName());
    }

    @Override
    public int getItemCount() {
        return types.length;
    }
}
