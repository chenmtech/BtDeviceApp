package com.cmtech.android.btdeviceapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.ConfiguredDeviceAdapter;
import com.cmtech.android.btdeviceapp.adapter.ScanedDeviceAdapter;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;

public class ConfiguredDeviceActivity extends AppCompatActivity {
    private ViseBle viseBle = ViseBle.getInstance();
    private ConfiguredDeviceAdapter configuredDeviceAdapter;
    private RecyclerView rvConfiguredDevices;
    List<ConfiguredDevice> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configured_device);

        String macAddress = "11:22:33";
        List<ConfiguredDevice> devices = DataSupport.where("macAddress = ?", macAddress)
                                                    .find(ConfiguredDevice.class);
        ConfiguredDevice oneDevice = new ConfiguredDevice();
        if(devices.isEmpty()) {
            oneDevice.setMacAddress("11:22:33");
            oneDevice.setNickName("体温计");
            oneDevice.save();
        }

        deviceList = DataSupport.findAll(ConfiguredDevice.class);

        rvConfiguredDevices = (RecyclerView)findViewById(R.id.rvConfiguredDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvConfiguredDevices.setLayoutManager(layoutManager);
        configuredDeviceAdapter = new ConfiguredDeviceAdapter(deviceList);
        rvConfiguredDevices.setAdapter(configuredDeviceAdapter);
        rvConfiguredDevices.scrollToPosition(0);
    }
}
