package com.cmtech.android.btdevice.temphumid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;


/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends BLEDeviceFragment {

    private TextView tvTempData;
    private TextView tvHumidData;
    private TextView tvHeadIndex;

    private RecyclerView rvHistoryData;
    private TempHumidHistoryDataAdapter historyDataAdapter;

    private View rootView;

    public TempHumidFragment() {

    }

    public static TempHumidFragment newInstance() {
        return new TempHumidFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("TempHumidFragment", "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_temphumid, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTempData = (TextView)view.findViewById(R.id.tv_temp_data);
        tvHumidData = (TextView)view.findViewById(R.id.tv_humid_data);
        tvHeadIndex = (TextView)view.findViewById(R.id.tv_heat_index);

        rvHistoryData = (RecyclerView)view.findViewById(R.id.rv_history_temphumid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyApplication.getContext());
        rvHistoryData.setLayoutManager(layoutManager);
        rvHistoryData.addItemDecoration(new DividerItemDecoration(MyApplication.getContext(), DividerItemDecoration.VERTICAL));
        historyDataAdapter = new TempHumidHistoryDataAdapter(((TempHumidDevice)device).getDataList());
        rvHistoryData.setAdapter(historyDataAdapter);
        rvHistoryData.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if(lastVisiblePosition == recyclerView.getLayoutManager().getItemCount()-1) {
                        //Toast.makeText(MyApplication.getContext(), "更新历史数据", Toast.LENGTH_SHORT).show();
                        ((TempHumidDeviceController)controller).initDeviceTimerService();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
}
