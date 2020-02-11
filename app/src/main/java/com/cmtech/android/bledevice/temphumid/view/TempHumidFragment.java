package com.cmtech.android.bledevice.temphumid.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledevice.temphumid.adapter.TempHumidHistoryDataAdapter;
import com.cmtech.android.bledevice.temphumid.model.ITempHumidDataObserver;
import com.cmtech.android.bledevice.temphumid.model.BleTempHumidData;
import com.cmtech.android.bledevice.temphumid.model.TempHumidDevice;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

import java.util.ArrayList;
import java.util.Locale;


/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends DeviceFragment implements ITempHumidDataObserver {

    private TextView tvTempData;
    private TextView tvHumidData;
    private TextView tvHeadIndex;

    private RecyclerView rvHistoryData;

    private TempHumidHistoryDataAdapter historyDataAdapter;

    private TempHumidDevice device;

    public TempHumidFragment() {

        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (TempHumidDevice) getDevice();
        device.registerTempHumidDataObserver(this);

        return inflater.inflate(R.layout.fragment_temphumid, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTempData = view.findViewById(R.id.tv_temp_data);
        tvHumidData = view.findViewById(R.id.tv_humid_data);
        tvHeadIndex = view.findViewById(R.id.tv_heat_index);

        rvHistoryData = view.findViewById(R.id.rv_history_temphumid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyApplication.getContext());
        rvHistoryData.setLayoutManager(layoutManager);
        rvHistoryData.addItemDecoration(new DividerItemDecoration(MyApplication.getContext(), DividerItemDecoration.VERTICAL));
        historyDataAdapter = new TempHumidHistoryDataAdapter(new ArrayList<BleTempHumidData>());
        rvHistoryData.setAdapter(historyDataAdapter);
        rvHistoryData.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if(lastVisiblePosition == recyclerView.getLayoutManager().getItemCount()-1) {
                        //device.updateHistoryData();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                historyDataAdapter.notifyDataSetChanged();

                // 打开设备
                MainActivity activity = (MainActivity) getActivity();
                device.open(activity.getNotifyService());
            }
        });
    }

    @Override
    public void openConfigureActivity() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeTempHumidDataObserver(this);
    }

    @Override
    public void updateCurrentData() {
        BleTempHumidData data = device.getCurTempHumid();

        tvHumidData.setText(String.valueOf(data.getHumid()));

        tvTempData.setText(String.format(Locale.getDefault(), "%.1f", data.getTemp()));

        float heatindex = data.computeHeatIndex();
        tvHeadIndex.setText(String.format(Locale.getDefault(),"%.1f", heatindex));
    }

    @Override
    public void addHistoryData(BleTempHumidData data) {
        historyDataAdapter.notifyDataSetChanged();
    }

}
