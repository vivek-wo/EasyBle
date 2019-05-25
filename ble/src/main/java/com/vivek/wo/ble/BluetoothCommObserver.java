package com.vivek.wo.ble;

public interface BluetoothCommObserver {

    /**
     * 连接完成
     */
    void connectComplete();

    /**
     * 连接断开
     *
     * @param throwable
     */
    void connectLost(Throwable throwable);

    /**
     * 远程数据监听
     *
     * @param data
     */
    void remoteDataChanged(byte[] data);
}
