package com.cmtech.android.bledevice.temphumid.activity;

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

import com.cmtech.android.bledevice.temphumid.adapter.TempHumidHistoryDataAdapter;
import com.cmtech.android.bledevice.temphumid.model.ITempHumidDataObserver;
import com.cmtech.android.bledevice.temphumid.model.TempHumidData;
import com.cmtech.android.bledevice.temphumid.model.TempHumidDevice;
import com.cmtech.android.bledevice.view.ScanWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

import java.util.Locale;


/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends BleDeviceFragment implements ITempHumidDataObserver {

    private TextView tvTempData;
    private TextView tvHumidData;
    private TextView tvHeadIndex;

    private RecyclerView rvHistoryData;
    private TempHumidHistoryDataAdapter historyDataAdapter;

    private ScanWaveView waveView;

    private TempHumidDevice device;

    public TempHumidFragment() {

    }

    public static BleDeviceFragment newInstance(String macAddress) {
        BleDeviceFragment fragment = new TempHumidFragment();
        return BleDeviceFragment.pushMacAddressIntoFragment(macAddress, fragment);
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
        historyDataAdapter = new TempHumidHistoryDataAdapter(device.getHistoryDataList());
        rvHistoryData.setAdapter(historyDataAdapter);
        rvHistoryData.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if(lastVisiblePosition == recyclerView.getLayoutManager().getItemCount()-1) {
                        device.updateHistoryData();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        waveView = view.findViewById(R.id.rwv_ecgview);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateWaveView(15, 2.0f, 15);
                historyDataAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateWaveView(final int xRes, final float yRes, final int viewGridWidth) {
        waveView.setResolution(xRes, yRes);
        waveView.setGridPixels(viewGridWidth);
        waveView.setZeroLocation(0.5);
        waveView.initView();

        for(TempHumidData data : device.getHistoryDataList()) {
            waveView.showData((int)((data.getTemp()-20)*100));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeTempHumidDataObserver(this);
    }

    @Override
    public void updateCurrentData() {
        TempHumidData data = device.getCurTempHumid();

        tvHumidData.setText(String.valueOf(data.getHumid()));

        tvTempData.setText(String.format(Locale.getDefault(), "%.1f", data.getTemp()));

        float heatindex = data.computeHeatIndex();
        tvHeadIndex.setText(String.format(Locale.getDefault(),"%.1f", heatindex));
    }

    @Override
    public void addHistoryData(TempHumidData data) {
        historyDataAdapter.notifyDataSetChanged();

        waveView.showData((int)((data.getTemp()-20)*100));
    }

}
