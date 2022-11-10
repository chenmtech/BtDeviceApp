package com.cmtech.android.bledeviceapp.adapter;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.model.ContactPerson.AGREE;
import static com.cmtech.android.bledeviceapp.model.ContactPerson.WAITING;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ContactManageActivity;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;

import java.util.List;


/**
 * ContactAdapter : 联系人Adapter
 * Created by bme on 2022/10/27.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private final List<ContactPerson> cps;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvNickname; // 昵称
        TextView tvId; // ID
        ImageView ivImage; // 头像
        TextView tvNote; // 简介
        TextView tvStatus; // 状态
        Context context; // 上下文

        ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            view = itemView;
            tvNickname = view.findViewById(R.id.tv_contact_nickname);
            tvId = view.findViewById(R.id.tv_contact_id);
            ivImage = view.findViewById(R.id.iv_contact_image);
            tvNote = view.findViewById(R.id.tv_contact_note);
            tvStatus = view.findViewById(R.id.tv_status);
        }
    }

    public ContactAdapter(List<ContactPerson> cps) {
        this.cps = cps;
    }


    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_contact, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        ContactPerson cp = cps.get(position);

        String name = cp.getNickName();
        if(TextUtils.isEmpty(name))
            holder.tvNickname.setVisibility(View.GONE);
        else {
            holder.tvNickname.setVisibility(View.VISIBLE);
            holder.tvNickname.setText(name);
        }

        holder.tvId.setText(cp.getAccountId());

        if(TextUtils.isEmpty(cp.getIcon())) {
            holder.ivImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(cp.getIcon(),  32);
            holder.ivImage.setImageBitmap(bitmap);
        }

        if(!TextUtils.isEmpty(cp.getNote()))
            holder.tvNote.setText(cp.getNote());

        String statusStr = "";
        switch (cp.getStatus()) {
            case WAITING:
                statusStr = "申请中";
                break;
            case AGREE:
                statusStr = "";
                break;
        }
        holder.tvStatus.setText(statusStr);

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case 1: // delete
                            if (ClickCheckUtil.isFastClick()) return true;
                            deleteContact(holder.context, cp.getAccountId());
                            break;
                        case 2: // agree
                            if (ClickCheckUtil.isFastClick()) return true;
                            agreeContact(holder.context, cp.getAccountId());
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
                        if (cp.getStatus() == AGREE) {
                            MenuItem agree = menu.add(Menu.NONE, 1, Menu.NONE, "删除");
                            agree.setOnMenuItemClickListener(listener);
                        }
                        if (cp.getStatus() == WAITING) {
                            MenuItem deny = menu.add(Menu.NONE, 2, Menu.NONE, "同意");
                            deny.setOnMenuItemClickListener(listener);
                        }
                    }
                });
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return cps.size();
    }

    // 同意一条联系人申请
    private void agreeContact(Context context, int contactId) {
        MyApplication.getAccount().agreeContact(context, contactId, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                if (code == RCODE_SUCCESS) {
                    ((ContactManageActivity)context).updateContact();
                }
            }
        });
    }

    // 删除联系人
    private void deleteContact(Context context, int contactId) {
        MyApplication.getAccount().deleteContact(context, contactId, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                if (code == RCODE_SUCCESS) {
                    ((ContactManageActivity)context).updateContact();
                }
            }
        });
    }
}
