package com.cmtech.android.bledevice.ptt.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledevice.ptt.model.ptt2bp.AverageFilter;
import com.cmtech.android.bledevice.ptt.model.ptt2bp.Ptt2BpModel2;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BlePttRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;

import org.litepal.LitePal;

import java.util.Date;
import java.util.UUID;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.PTT;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.eeg.model
 * ClassName:      EegDevice
 * Description:    脑电设备
 * Author:         作者名
 * CreateDate:     2020/6/11 下午3:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/6/11 下午3:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */


public class PttDevice extends AbstractDevice {
    private static final int DEFAULT_SAMPLE_RATE = 125; // default sample rate, unit: Hz
    public static final int DEFAULT_ECG_CALI = 160; // default ecg 1mV calibration value
    public static final int DEFAULT_PPG_CALI = 100; // default ppg calibration value
    private static final int PTT_RECORD_MAX_SECOND = 30;

    // ppg service
    private static final String pttServiceUuid = "AAC0";
    private static final String pttMeasUuid = "AAC1";
    private static final String pttSampleRateUuid = "AAC2";
    private static final UUID pttServiceUUID = UuidUtil.stringToUUID(pttServiceUuid, MY_BASE_UUID);
    private static final UUID pttMeasUUID = UuidUtil.stringToUUID(pttMeasUuid, MY_BASE_UUID);
    private static final UUID pttSampleRateUUID = UuidUtil.stringToUUID(pttSampleRateUuid, MY_BASE_UUID);

    private static final BleGattElement PTTMEAS = new BleGattElement(pttServiceUUID, pttMeasUUID, null, "PTT Data Packet");
    private static final BleGattElement PTTMEASCCC = new BleGattElement(pttServiceUUID, pttMeasUUID, CCC_UUID, "PTT Data Packet CCC");
    private static final BleGattElement PTTSAMPLERATE = new BleGattElement(pttServiceUUID, pttSampleRateUUID, null, "PTT Sample Rate");

    private int sampleRate = DEFAULT_SAMPLE_RATE; // sample rate

    private PttDataProcessor pttProcessor; // PTT processor

    private OnPttListener listener; // device listener

    private BlePttRecord pttRecord;
    private boolean isPttRecord = false; // is recording ppg

    private final PttCfg config; // PTTdevice configuration

    private final AverageFilter pttFilter;

    private boolean showAveragePtt = false;

    public PttDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);

        PttCfg config = LitePal.where("address = ?", getAddress()).findFirst(PttCfg.class);
        if (config == null) {
            config = new PttCfg();
            config.setAddress(getAddress());
            config.save();
        }
        this.config = config;

        pttFilter = new AverageFilter(30);
    }

    public final int getSampleRate() {
        return sampleRate;
    }

    public void setListener(OnPttListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public void close() {
        super.close();

        if(isPttRecord) {
            setPttRecord(false);
        }
    }

    @Override
    public boolean onConnectSuccess() {
        BleConnector connector = (BleConnector)this.connector;

        BleGattElement[] elements = new BleGattElement[]{PTTMEAS, PTTMEASCCC, PTTSAMPLERATE};
        if(connector.containGattElements(elements)) {
            readSampleRate();
            ((BleConnector)connector).runInstantly(new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if (listener != null)
                        listener.onFragmentUpdated(sampleRate, DEFAULT_ECG_CALI, DEFAULT_PPG_CALI, DEFAULT_ZERO_LOCATION);

                    updateSignalShowState(true);

                    pttProcessor = new PttDataProcessor(PttDevice.this);
                    pttProcessor.start();
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });

            enablePtt(true);
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void onConnectFailure() {
        if(pttProcessor != null) {
            pttProcessor.stop();
        }

        updateSignalShowState(false);

        setPttRecord(false);
    }

    @Override
    public void onDisconnect() {
        if(pttProcessor != null) {
            pttProcessor.stop();
        }

        updateSignalShowState(false);

        setPttRecord(false);
    }

    @Override
    public void disconnect(final boolean forever) {
        enablePtt(false);
        setPttRecord(false);
        super.disconnect(forever);
    }

    public void setPttRecord(final boolean isRecord) {
        if(isPttRecord == isRecord) return;

        isPttRecord = isRecord;
        if(isRecord) {
            pttRecord = (BlePttRecord) RecordFactory.create(PTT, DEFAULT_RECORD_VER, new Date().getTime(), getAddress(), MyApplication.getAccountId());
            if(pttRecord != null) {
                pttRecord.setSampleRate(sampleRate);
                pttRecord.setEcgCaliValue(DEFAULT_ECG_CALI);
                pttRecord.setPpgCaliValue(DEFAULT_PPG_CALI);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), R.string.pls_be_quiet_when_record, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            if(pttRecord == null) return;

            pttRecord.setCreateTime(new Date().getTime());
            pttRecord.setRecordSecond(pttRecord.getEcgData().size()/sampleRate);
            pttRecord.save();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), R.string.save_record_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(listener != null) {
            listener.onPttSignalRecordStatusChanged(isPttRecord);
        }
    }

    public void showPttSignal(int ecgSignal, int ppgSignal) {
        if (listener != null) {
            listener.onPttSignalShowed(ecgSignal, ppgSignal);
        }
    }

    public void recordPttSignal(int ecgSignal, int ppgSignal) {
        if(isPttRecord && pttRecord != null) {
            pttRecord.process(ecgSignal, ppgSignal);
            if(pttRecord.getDataNum() % sampleRate == 0 && listener != null) {
                int second = pttRecord.getDataNum()/sampleRate;
                listener.onPttSignalRecordTimeUpdated(second);
                if(second >= PTT_RECORD_MAX_SECOND) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setPttRecord(false);
                        }
                    });
                }
            }
        }
    }

    public void processPtt(int ptt) {
        if(ptt > 0 && ptt < 500) {
            int avePtt = pttFilter.filter(ptt);
            Pair<Integer, Integer> bp;
            if(showAveragePtt) {
                bp = calculateBPUsingPTT(avePtt);
                showPttAndBpValue(avePtt, bp.first, bp.second);
            } else {
                bp = calculateBPUsingPTT(ptt);
                showPttAndBpValue(ptt, bp.first, bp.second);
            }
        }
    }

    public void setShowAveragePtt(boolean showAveragePtt) {
        this.showAveragePtt = showAveragePtt;
    }

    private void showPttAndBpValue(int ptt, int sbp, int dbp) {
        if (listener != null) {
            new Handler((Looper.getMainLooper())).post(new Runnable() {
                @Override
                public void run() {
                    listener.onPttAndBpValueShowed(ptt, sbp, dbp);
                }
            });
        }
    }

    private void enablePtt(boolean enable) {
        //((BleConnector)connector).notify(PPGMEASCCC, false, null);

        if(enable) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if(pttProcessor != null)
                        pttProcessor.processData(data);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(PTTMEASCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(PTTMEASCCC, false, null);

            if(pttProcessor != null)
                pttProcessor.stop();

        }
    }

    private void readSampleRate() {
        ((BleConnector)connector).read(PTTSAMPLERATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                sampleRate = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void updateSignalShowState(boolean isShow) {
        if (listener != null) {
            listener.onPttSignalShowStatusUpdated(isShow);
        }
    }


    public final PttCfg getConfig() {
        return config;
    }

    public void updateConfig(PttCfg config) {
        this.config.copyFrom(config);
        this.config.save();
    }

    private Pair<Integer, Integer> calculateBPUsingPTT(int ptt) {
        int ptt0 = config.getPtt0();
        int sbp0 = config.getSbp0();
        int dbp0 = config.getDbp0();
        Pair<Double, Double> rlt = new Ptt2BpModel2(ptt0, sbp0, dbp0).getBp(ptt);
        return new Pair<>(rlt.first.intValue(), rlt.second.intValue());
    }
}