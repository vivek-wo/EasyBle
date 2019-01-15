package com.vivek.wo.ble.comms;

public interface IRssiCallback extends ITimeoutCallback {

    void onRssi(Token token, int rssi, int status);
}
