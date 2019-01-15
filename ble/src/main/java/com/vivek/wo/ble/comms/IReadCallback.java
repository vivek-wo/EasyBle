package com.vivek.wo.ble.comms;

public interface IReadCallback extends ITimeoutCallback {

    void onRead(Token token, String[] uuid, int status, byte[] data);
}
