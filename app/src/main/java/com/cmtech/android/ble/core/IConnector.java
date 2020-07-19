package com.cmtech.android.ble.core;

import android.content.Context;

public interface IConnector {
    boolean open(Context context); // open connector
    void close(); // close connector
    void connect(); // connect
    void disconnect(boolean forever); // disconnect. if forever=true, no reconnection occurred, otherwise reconnect it.
    DeviceConnectState getState(); // get connect state

    interface IConnectorCallback {
        boolean onConnectSuccess(); // operation when connection success
        void onConnectFailure(); // operation when connection failure
        void onDisconnect(); // operation when disconnection
        void onConnectStateUpdated(); // operation when connect state updated
    }
}
