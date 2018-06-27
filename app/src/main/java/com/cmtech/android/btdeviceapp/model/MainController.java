package com.cmtech.android.btdeviceapp.model;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;

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

    // 从数据库中获取以前添加的设备基本信息列表
    public void initialize() {
        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BLEDeviceBasicInfo> basicInfoList = DataSupport.findAll(BLEDeviceBasicInfo.class);
        if(basicInfoList != null && !basicInfoList.isEmpty()) {
            for(BLEDeviceBasicInfo info : basicInfoList) {
                createAndAddNewDevice(info);
            }
        }
    }

    public List<BLEDeviceModel> getIncludedDeviceList() {
        return includedDeviceList;
    }

    // 开始扫描新的设备
    public void startScanDevice() {
        List<String> deviceMacList = getIncludedDeviceMacAddressList();
        activity.startScanActivity(deviceMacList);
    }

    // 创建并添加一个新的设备
    public void createAndAddNewDevice(BLEDeviceBasicInfo basicInfo) {
        BLEDeviceModel device = createDeviceUsingBasicInfo(basicInfo);

        if(device != null) {
            // 将设备基本信息保存到数据库
            basicInfo.save();
            // 将设备添加到设备列表
            includedDeviceList.add(device);
            // 添加Activity作为设备连接状态的观察者
            device.registerConnectStateObserver(activity);
            // 通知观察者
            device.notifyConnectStateObservers();
        }
    }

    private BLEDeviceModel createDeviceUsingBasicInfo(BLEDeviceBasicInfo basicInfo) {
        BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(basicInfo);
        if(factory == null) return null;
        return factory.createDevice(basicInfo);
    }


    // 打开设备
    public void openDevice(BLEDeviceModel device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            activity.showDeviceFragment(fragment);
            fragment.connectDevice();
        } else {
            BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo());
            if(factory == null) return;
            BLEDeviceController deviceController = factory.createController(device, activity);
            openedControllerList.add(deviceController);
            activity.addFragmentToManager(device, deviceController.getFragment());
        }
    }

    // 连接设备
    public void connectDevice(BLEDeviceModel device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            fragment.connectDevice();
        }
    }

    // 连接设备
    public void disconnectDevice(BLEDeviceModel device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            fragment.disconnectDevice();
        }
    }

    // 关闭设备
    public void closeDevice(BLEDeviceModel device) {
        if(device == null) return;

        BLEDeviceController controller = getController(device);
        if(controller == null) return;
        BLEDeviceFragment fragment = controller.getFragment();
        if(fragment != null) {
            controller.closeDevice();
            openedControllerList.remove(controller);
            activity.deleteFragment(fragment);
        }
    }

    public void closeAllFragment() {
        for(BLEDeviceController controller : openedControllerList) {
            controller.closeDevice();
            activity.deleteFragment(controller.getFragment());
        }
        openedControllerList.clear();
    }

    // 删除设备
    public void deleteIncludedDevice(final BLEDeviceModel device) {
        if(device == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.getBasicInfo().delete();
                includedDeviceList.remove(device);
                device.notifyConnectStateObservers();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    private boolean isDeviceOpened(BLEDeviceModel device) {
        return (getController(device) == null) ? false : true;
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

    // 从deviceControllerList中寻找Fragment对应的控制器
    public BLEDeviceController getController(BLEDeviceModel device) {
        for(BLEDeviceController controller : openedControllerList) {
            if(device.equals(controller.getDevice())) {
                return controller;
            }
        }
        return null;
    }

    // 获取设备对应的Fragment
    private BLEDeviceFragment getFragmentForDevice(BLEDeviceModel device) {
        BLEDeviceController controller = getController(device);
        return (controller != null) ? controller.getFragment() : null;
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
