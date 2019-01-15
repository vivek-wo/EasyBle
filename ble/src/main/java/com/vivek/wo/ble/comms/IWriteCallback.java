package com.vivek.wo.ble.comms;

public interface IWriteCallback extends ITimeoutCallback {

    void onWrite(Token token, String[] uuid, int status);
}
