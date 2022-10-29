package com.cmtech.android.bledeviceapp.adapter;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.AGREE;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.DENY;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ShareManageActivity;
import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.model.ShareInfo;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;

import java.util.List;


/**
 * ShareInfoAdapter : 分享信息Adapter
 * Created by bme on 2022/10/27.
 */

public class SharePersonAdapter extends RecyclerView.Adapter<SharePersonAdapter.ViewHolder> {
    private final List<ContactPerson> cps;
    private int pos = -1;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view; // 设备视图
        TextView name; //
        ImageView image;
        Context context;

        ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            view = itemView;
            name = view.findViewById(R.id.tv_name);
            image = view.findViewById(R.id.iv_image);
        }
    }

    public SharePersonAdapter(List<ContactPerson> cps) {
        this.cps = cps;
    }


    @Override
    public SharePersonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_contact_person, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharePersonAdapter.ViewHolder holder, int position) {
        position = holder.getBindingAdapterPosition();
        ContactPerson cp = cps.get(position);

        int myId = MyApplication.getAccountId();
        int id = cp.getAccountId();
        Pair<String,String> rtn = getNameAndIcon(id, myId);
        holder.name.setText(rtn.first);
        if(TextUtils.isEmpty(rtn.second)) {
            holder.image.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(rtn.second,  32);
            holder.image.setImageBitmap(bitmap);
        }

        int finalPosition = position;
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                //Toast.makeText(holder.context, cp.getNote(), Toast.LENGTH_SHORT).show();
                notifyItemChanged(pos);
                pos = finalPosition;
                notifyItemChanged(pos);
            }
        });

        if(pos == position) {
            holder.view.setBackgroundColor(ContextCompat.getColor(holder.context, R.color.secondary));
        } else {
            holder.view.setBackgroundColor(ContextCompat.getColor(holder.context, R.color.primary));
        }
    }

    private Pair<String, String> getNameAndIcon(int id, int myId) {
        String nameStr = "";
        String icon = "";
        if(id == myId) {
            nameStr = MyApplication.getAccount().getNickNameOrUserId();
            icon = MyApplication.getAccount().getIcon();
        } else {
            ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
            if(cp != null) {
                nameStr = cp.getNickName();
                icon = cp.getIcon();
            } else {
                nameStr = "ID："+ id;
                icon = "";
            }
        }
        return new Pair<>(nameStr, icon);
    }

    @Override
    public int getItemCount() {
        return cps.size();
    }

    public int getSelectContactPersonId() {
        if(pos == -1) return INVALID_ID;
        return cps.get(pos).getAccountId();
    }
}
