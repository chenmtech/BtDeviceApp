package com.cmtech.android.btdeviceapp.btdevice.common;

import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.btdeviceapp.btdevice.unknowndevice.UnknownDeviceFragment;
import com.cmtech.android.btdeviceapp.btdevice.thermo.ThermoFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Arrays;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;

/**
 * Created by bme on 2018/2/28.
 */

public class OpenedDeviceFragmentFactory {
    private static final byte[] UUID_SIMPLE128GATTPROFILE =
            new byte[]{-8, 110, 63, -125, -22, 56, 86, -95, 37, 64, -27, -52, (byte)(0x10), (byte)(0xAA), 32, 10};

    private static final byte[] UUID_HEIGHTSCALE =
            new byte[]{-8, 110, 63, -125, -22, 56, 86, -95, 37, 64, -27, -52, (byte)(0x20), (byte)(0xAA), 32, 10};

    private static final byte[] UUID_THERMOMETER =
            new byte[]{-8, 110, 63, -125, -22, 56, 86, -95, 37, 64, -27, -52, (byte)(0x30), (byte)(0xAA), 32, 10};

    private static final byte[] UUID_ECGMONITOR =
            new byte[]{-8, 110, 63, -125, -22, 56, 86, -95, 37, 64, -27, -52, (byte)(0x40), (byte)(0xAA), 32, 10};

    private static final byte[] UUID_SIGGENERATOR =
            new byte[]{-8, 110, 63, -125, -22, 56, 86, -95, 37, 64, -27, -52, (byte)(0x50), (byte)(0xAA), 32, 10};


    private OpenedDeviceFragmentFactory() {

    }

    public static OpenedDeviceFragment build(ConfiguredDevice device) {
        AdRecord record = device.getDeviceMirror().getBluetoothLeDevice()
                .getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record != null) {
            byte[] uuid = record.getData();
            if(Arrays.equals(UUID_THERMOMETER, uuid)) {
                return ThermoFragment.newInstance(device);
            } else {
                return UnknownDeviceFragment.newInstance(device);
            }
        }
        return null;
    }
}
