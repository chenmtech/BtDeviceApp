package com.cmtech.android.btdeviceapp.model;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceModelInterface;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainController {
    //
    private final MainActivity activity;

    // 已添加的设备列表
    private final List<IBLEDeviceModelInterface> includedDeviceList = new ArrayList<>();

    // 已打开的设备控制器列表
    private final List<IBLEDeviceControllerInterface> openedControllerList = new LinkedList<>();

    public MainController(MainActivity activity) {
        this.activity = activity;
    }

    public List<IBLEDeviceModelInterface> getIncludedDeviceList() {
        return includedDeviceList;
    }

    // 从数据库中获取以前添加的设备基本信息列表
    public void initialize() {
        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BLEDeviceBasicInfo> basicInfoList = DataSupport.findAll(BLEDeviceBasicInfo.class);
        if(basicInfoList != null && !basicInfoList.isEmpty()) {
            for(BLEDeviceBasicInfo info : basicInfoList) {
                createNewDeviceUsingBasicInfo(info);
            }
        }
    }

    // 根据设备基本信息创建一个新的设备，并添加到设备列表中
    public void createNewDeviceUsingBasicInfo(BLEDeviceBasicInfo basicInfo) {
        IBLEDeviceModelInterface device = createDeviceUsingBasicInfo(basicInfo);

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

    // 开始扫描新的设备
    public void scanDevice() {
        List<String> deviceMacList = getIncludedDeviceMacAddressList();
        activity.startScanActivity(deviceMacList);
    }

    // 打开设备
    public void openDevice(IBLEDeviceModelInterface device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            // 已经打开了，只要显示Fragment，并开始连接
            activity.showFragment(fragment);
            fragment.connectDevice();
        } else {
            BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo());
            if(factory == null) return;
            IBLEDeviceControllerInterface deviceController = factory.createController(device, activity);
            openedControllerList.add(deviceController);
            activity.addFragment(device, deviceController.getFragment());
        }
    }

    // 连接设备
    public void connectDevice(IBLEDeviceModelInterface device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            fragment.connectDevice();
        }
    }

    // 断开设备
    public void disconnectDevice(IBLEDeviceModelInterface device) {
        if(device == null) return;

        BLEDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            fragment.disconnectDevice();
        }
    }

    // 关闭设备
    public void closeDevice(IBLEDeviceModelInterface device) {
        if(device == null) return;

        IBLEDeviceControllerInterface controller = getController(device);
        if(controller == null) return;
        BLEDeviceFragment fragment = controller.getFragment();
        if(fragment != null) {
            //controller.closeDevice();
            openedControllerList.remove(controller);
            activity.deleteFragment(fragment);
        }
        MyApplication.getViseBle().clear();
    }


    // 删除设备
    public void deleteIncludedDevice(final IBLEDeviceModelInterface device) {
        if(device == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.getBasicInfo().delete();
                includedDeviceList.remove(device);
                activity.updateDeviceListAdapter();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    // 从deviceControllerList中寻找Fragment对应的控制器
    public IBLEDeviceControllerInterface getController(BLEDeviceFragment fragment) {
        for(IBLEDeviceControllerInterface controller : openedControllerList) {
            if(controller.getFragment().equals(fragment)) {
                return controller;
            }
        }
        return null;
    }

    // 从deviceControllerList中寻找Fragment对应的控制器
    public IBLEDeviceControllerInterface getController(IBLEDeviceModelInterface device) {
        for(IBLEDeviceControllerInterface controller : openedControllerList) {
            if(device.equals(controller.getDevice())) {
                return controller;
            }
        }
        return null;
    }

    // 获取设备对应的Fragment
    private BLEDeviceFragment getFragmentForDevice(IBLEDeviceModelInterface device) {
        IBLEDeviceControllerInterface controller = getController(device);
        return (controller != null) ? controller.getFragment() : null;
    }

    // 产生已包含的设备Mac地址字符串列表字符串
    private List<String> getIncludedDeviceMacAddressList() {
        List<String> deviceMacList = new ArrayList<>();
        for(IBLEDeviceModelInterface device : includedDeviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }

    // 设备是否打开
    private boolean isDeviceOpened(BLEDeviceModel device) {
        return (getController(device) == null) ? false : true;
    }

    // 根据设备基本信息创建一个新的设备
    private IBLEDeviceModelInterface createDeviceUsingBasicInfo(BLEDeviceBasicInfo basicInfo) {
        BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(basicInfo);
        if(factory == null) return null;
        return factory.createDevice(basicInfo);
    }
}
