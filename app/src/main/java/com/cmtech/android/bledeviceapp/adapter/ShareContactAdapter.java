package com.cmtech.android.bledeviceapp.adapter;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_POS;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;

import java.util.List;


/**
 * SharePersonAdapter : 分享给联系人Adapter
 * Created by bme on 2022/10/27.
 */

public class ShareContactAdapter extends RecyclerView.Adapter<ShareContactAdapter.ViewHolder> {
    private static final int SELECT_ITEM_BACKGROUND_COLOR = R.color.secondary;
    private static final int NONSELECT_ITEM_BACKGROUND_COLOR = R.color.primary;

    private final List<ContactPerson> cps; // 可分享联系人列表
    private int selPos = INVALID_POS; // 选择的项位置

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView nameOrId; // 联系人昵称或ID号
        ImageView image; // 头像
        Context context; // 上下文

        ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            view = itemView;
            nameOrId = view.findViewById(R.id.tv_name_or_id);
            image = view.findViewById(R.id.iv_image);
        }
    }

    public ShareContactAdapter(List<ContactPerson> cps) {
        this.cps = cps;
    }

    @Override
    public ShareContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_share_contact, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareContactAdapter.ViewHolder holder, int position) {
        ContactPerson cp = cps.get(position);

        String nickName = cp.getNickName();
        if(TextUtils.isEmpty(nickName)) {
            holder.nameOrId.setText("ID:"+cp.getAccountId());
        } else {
            holder.nameOrId.setText(nickName);
        }

        String icon = cp.getIcon();
        if(TextUtils.isEmpty(icon)) {
            holder.image.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(icon,  32);
            holder.image.setImageBitmap(bitmap);
        }

        int finalPosition = position;
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                notifyItemChanged(selPos);
                selPos = finalPosition;
                notifyItemChanged(selPos);
            }
        });

        if(selPos == position) {
            holder.view.setBackgroundColor(ContextCompat.getColor(holder.context, SELECT_ITEM_BACKGROUND_COLOR));
        } else {
            holder.view.setBackgroundColor(ContextCompat.getColor(holder.context, NONSELECT_ITEM_BACKGROUND_COLOR));
        }
    }

    @Override
    public int getItemCount() {
        return cps.size();
    }

    // 获取选择的联系人ID
    public int getSelectContactPersonId() {
        if(selPos == INVALID_POS) return INVALID_ID;
        return cps.get(selPos).getAccountId();
    }
}
