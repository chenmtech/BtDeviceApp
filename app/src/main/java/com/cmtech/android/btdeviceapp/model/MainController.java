package com.cmtech.android.btdeviceapp.model;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceObserver;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainController {
    //
    private MainActivity activity;

    // 设备列表
    private List<BLEDeviceModel> includedDeviceList = new ArrayList<>();

    // 设备控制器列表
    private List<BLEDeviceController> openedControllerList = new LinkedList<>();

    public MainController(MainActivity activity) {
        this.activity = activity;
    }

    public void initialize() {
        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BLEDevicePersistantInfo> persistantInfoList = DataSupport.findAll(BLEDevicePersistantInfo.class);
        if(persistantInfoList != null && !persistantInfoList.isEmpty()) {
            for(BLEDevicePersistantInfo info : persistantInfoList) {
                BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(info);
                includedDeviceList.add(factory.createDevice(info));
            }
        }
    }

    public List<BLEDeviceModel> getIncludedDeviceList() {
        return includedDeviceList;
    }

    public void startScanAndAddDevice() {
        List<String> deviceMacList = getDeviceMacAddressList();
        activity.startScanActivity(deviceMacList);
    }

    public void createAndAddNewDevice(BLEDevicePersistantInfo persistantInfo) {
        // 保存到数据库
        persistantInfo.save();
        // 添加到设备列表
        BLEDeviceModel device = BLEDeviceAbstractFactory.getBLEDeviceFactory(persistantInfo).createDevice(persistantInfo);
        includedDeviceList.add(device);
        // 添加deviceAdapter作为观察者
        activity.registerDeviceObserver(device);
        // 通知观察者
        device.notifyDeviceObservers(IBLEDeviceObserver.TYPE_ADDED);
    }

    // 从deviceControllerList中寻找Fragment对应的控制器
    public BLEDeviceController getController(BLEDeviceFragment fragment) {
        for(BLEDeviceController controller : openedControllerList) {
            if(controller.getFragment().equals(fragment)) {
                return controller;
            }
        }
        return null;
    }

    // 打开设备
    public void openBLEDevice(BLEDeviceModel device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getOpenedFragmentForDevice(device);
        if(fragment != null) {
            activity.showDeviceFragment(fragment);
            fragment.connectDevice();
        } else {
            BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(device);
            BLEDeviceController deviceController = factory.createController(device, activity);
            openedControllerList.add(deviceController);
            activity.addFragmentToManager(device, deviceController.getFragment());
        }
    }

    // 删除设备
    public void deleteBLEDevice(final BLEDeviceModel device) {
        if(device == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.delete();
                includedDeviceList.remove(device);
                device.notifyDeviceObservers(IBLEDeviceObserver.TYPE_DELETED);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    private BLEDeviceFragment getOpenedFragmentForDevice(BLEDeviceModel device) {
        for(BLEDeviceController controller : openedControllerList) {
            if(device.equals(controller.getDevice())) {
                return controller.getFragment();
            }
        }
        return null;
    }


    private List<String> getDeviceMacAddressList() {
        // 产生设备Mac地址字符串列表，防止多次添加同一个设备
        List<String> deviceMacList = new ArrayList<>();
        for(BLEDeviceModel device : includedDeviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }


}
