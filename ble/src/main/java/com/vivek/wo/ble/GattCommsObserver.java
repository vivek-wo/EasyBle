package com.vivek.wo.ble;

public interface GattCommsObserver {

    /**
     * 连接完成
     */
    void connectComplete();

    /**
     * 连接断开
     *
     * @param isActiveDisconnect 是否主动断开连接
     * @param e
     */
    void connectLost(boolean isActiveDisconnect, BluetoothException e);

}
