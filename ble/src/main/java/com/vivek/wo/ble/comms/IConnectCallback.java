package com.vivek.wo.ble.comms;

public interface IConnectCallback extends ITimeoutCallback {

    void onConnected(Token token);

    void onConnectFailure(Token token, int status);

    void onDisconnected(Token token, boolean isActiveDisconnect);
}
