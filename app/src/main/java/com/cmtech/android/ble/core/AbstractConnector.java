package com.cmtech.android.ble.core;

import android.content.Context;

import com.vise.log.ViseLog;

import java.util.Timer;
import java.util.TimerTask;

import static com.cmtech.android.ble.core.DeviceConnectState.CLOSED;
import static com.cmtech.android.ble.core.DeviceConnectState.CONNECTING;
import static com.cmtech.android.ble.core.DeviceConnectState.DISCONNECT;
import static com.cmtech.android.ble.core.DeviceConnectState.DISCONNECTING;
import static com.cmtech.android.bledeviceapp.AppConstant.RECONNECT_INTERVAL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.ble.core
 * ClassName:      AbstractConnector
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2019-12-06 06:32
 * UpdateUser:     更新者
 * UpdateDate:     2019-12-06 06:32
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public abstract class AbstractConnector implements IConnector {
    protected final String address; // address
    protected final IConnectorCallback connectorCallback; // connectorCallback
    protected Context context; // context, set when calling open function
    protected volatile DeviceConnectState state = CLOSED; // state
    private Timer reconnectTimer = new Timer(); // reconnection timer
    private boolean canReconnect = false; // Can be reconnected?
    private final long reconnectInterval; // reconnection interval, unit: ms

    public AbstractConnector(String address, IConnectorCallback connectorCallback) {
        this(address, connectorCallback, RECONNECT_INTERVAL);
    }

    public AbstractConnector(String address, IConnectorCallback connectorCallback, long reconnectInterval) {
        if(connectorCallback == null) {
            throw new NullPointerException("The connCallback is null");
        }
        this.address = address;
        this.connectorCallback = connectorCallback;
        this.reconnectInterval = reconnectInterval;
    }

    @Override
    public boolean open(Context context) {
        if (state != CLOSED) {
            return false;
        }

        this.context = context;
        setState(DISCONNECT);
        return true;
    }

    @Override
    public void close() {
        context = null;
        setState(CLOSED);
        reconnectTimer.cancel();
        canReconnect = false;
    }

    @Override
    public void connect() {
        setState(CONNECTING);
        reconnectTimer.cancel();
        canReconnect = true;
    }

    @Override
    public void disconnect(boolean forever) {
        setState(DISCONNECTING);
        reconnectTimer.cancel();
        this.canReconnect = !forever;
    }

    @Override
    public DeviceConnectState getState() {
        return state;
    }

    protected void setState(DeviceConnectState state) {
        if (this.state != state) {
            ViseLog.e(address + ": " + state);
            this.state = state;
            connectorCallback.onConnectStateUpdated();
        }
    }

    protected void reconnect() {
        if(canReconnect && reconnectInterval > 0) {
            reconnectTimer.cancel();
            reconnectTimer = new Timer();
            reconnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    connect();
                }
            }, reconnectInterval);
        }
    }
}
