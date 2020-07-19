package com.cmtech.android.ble.core;

import static com.cmtech.android.ble.core.DeviceConnectState.CONNECT;
import static com.cmtech.android.ble.core.DeviceConnectState.DISCONNECT;

public class WebConnector extends AbstractConnector {
    public WebConnector(String address, IConnectorCallback connectorCallback) {
        super(address, connectorCallback);
    }

    @Override
    public void connect() {
        super.connect();

        if (!connectorCallback.onConnectSuccess()) {
            disconnect(true);
        } else {
            setState(CONNECT);
        }
    }

    @Override
    public void disconnect(boolean forever) {
        super.disconnect(forever);
        connectorCallback.onDisconnect();
        setState(DISCONNECT);
    }


}
