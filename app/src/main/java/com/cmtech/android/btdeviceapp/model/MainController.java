package com.cmtech.android.btdeviceapp.model;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

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

    // 从数据库中获取以前添加的设备列表
    public void initialize() {
        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BLEDevicePersistantInfo> persistantInfoList = DataSupport.findAll(BLEDevicePersistantInfo.class);
        if(persistantInfoList != null && !persistantInfoList.isEmpty()) {
            for(BLEDevicePersistantInfo info : persistantInfoList) {
                BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(info);
                if(factory != null)
                    includedDeviceList.add(factory.createDevice(info));
            }
        }
    }

    public List<BLEDeviceModel> getIncludedDeviceList() {
        return includedDeviceList;
    }

    // 开始扫描新的设备
    public void startScanNewDevice() {
        List<String> deviceMacList = getIncludedDeviceMacAddressList();
        activity.startScanActivity(deviceMacList);
    }

    // 添加一个新的扫描到的设备
    public void addNewScanedDevice(BLEDevicePersistantInfo persistantInfo) {
        BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(persistantInfo);
        if(factory == null) return;

        // 保存到数据库
        persistantInfo.save();
        // 添加到设备列表
        BLEDeviceModel device = factory.createDevice(persistantInfo);
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

    // 连接设备
    public void connectBLEDevice(BLEDeviceModel device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getOpenedFragmentForDevice(device);
        if(fragment != null) {
            activity.showDeviceFragment(fragment);
            fragment.connectDevice();
        } else {
            BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(device.getPersistantInfo());
            if(factory == null) return;
            BLEDeviceController deviceController = factory.createController(device, activity);
            openedControllerList.add(deviceController);
            activity.addFragmentToManager(device, deviceController.getFragment());
        }
    }

    // 关闭一个打开的Fragment
    public void closeFragment(BLEDeviceFragment fragment) {
        BLEDeviceController controller = getController(fragment);
        controller.closeDevice();
        openedControllerList.remove(controller);
    }

    public void closeAllFragment() {
        for(BLEDeviceController controller : openedControllerList) {
            controller.closeDevice();
            activity.deleteFragment(controller.getFragment());
        }
        openedControllerList.clear();
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
                device.getPersistantInfo().delete();
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

    // 获取设备对应的打开的Fragment
    private BLEDeviceFragment getOpenedFragmentForDevice(BLEDeviceModel device) {
        for(BLEDeviceController controller : openedControllerList) {
            if(device.equals(controller.getDevice())) {
                return controller.getFragment();
            }
        }
        return null;
    }

    // 产生已包含的设备Mac地址字符串列表字符串
    private List<String> getIncludedDeviceMacAddressList() {
        List<String> deviceMacList = new ArrayList<>();
        for(BLEDeviceModel device : includedDeviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }


}
