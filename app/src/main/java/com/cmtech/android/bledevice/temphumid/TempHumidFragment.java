package com.cmtech.android.bledevice.temphumid;

import android.content.Context;
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

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;


/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends BleDeviceFragment implements ITempHumidDataObserver{

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((TempHumidDevice)device).registerTempHumidDataObserver(this);
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
        historyDataAdapter = new TempHumidHistoryDataAdapter(((TempHumidDevice)device).getHistoryDataList());
        rvHistoryData.setAdapter(historyDataAdapter);
        rvHistoryData.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if(lastVisiblePosition == recyclerView.getLayoutManager().getItemCount()-1) {
                        //Toast.makeText(MyApplication.getContext(), "更新历史数据", Toast.LENGTH_SHORT).show();
                        ((TempHumidDevice)device).updateHistoryData();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void updateCurrentTempHumidData() {
        TempHumidData data = ((TempHumidDevice)device).getCurTempHumid();

        tvHumidData.setText( ""+data.getHumid() );

        tvTempData.setText(String.format("%.1f", data.getTemp()));

        float heatindex = data.computeHeatIndex();
        tvHeadIndex.setText(String.format("%.1f", heatindex));
    }

    @Override
    public void updateHistoryTempHumidData() {
        historyDataAdapter.notifyDataSetChanged();
    }
}
