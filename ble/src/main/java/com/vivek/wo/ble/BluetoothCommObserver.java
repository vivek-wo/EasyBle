package com.vivek.wo.ble;

public interface BluetoothCommObserver {

    void connectComplete();

    void connectLost(Throwable throwable);

    void remoteDataChanged(byte[] data);
}
