package com.vivek.wo.ble;

import android.bluetooth.BluetoothDevice;

public interface ScanFilter {

    /**
     * 过滤
     *
     * @param device     蓝牙设备
     * @param rssi       蓝牙信号量
     * @param scanRecord 蓝牙广播数据
     * @return
     */
    boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord);
}
