package com.vivek.wo.ble.internal;

public interface GattCommsObserver {

    /**
     * 连接完成
     */
    void connectComplete();

    /**
     * 连接断开
     *
     * @param e
     */
    void connectLost(BluetoothException e);

}
