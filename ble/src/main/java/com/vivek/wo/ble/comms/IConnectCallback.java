package com.vivek.wo.ble.comms;

public interface IConnectCallback extends ITimeoutCallback {

    void onConnected(BluetoothComms bluetoothComms);

    void onConnectFailure(BluetoothComms bluetoothComms, int status);

    void onDisconnected(BluetoothComms bluetoothComms, boolean isActiveDisconnect);
}
