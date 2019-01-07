package com.vivek.wo.ble.comms;

public interface IConnectCallback extends ITimeoutCallback {

    void onConnected();

    void onConnectFailure(int status);

    void onDisconnected(boolean isActiveDisconnect);
}
