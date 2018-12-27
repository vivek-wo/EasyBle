package com.vivek.wo.ble.scan;

import com.vivek.wo.ble.comms.BluetoothDeviceExtend;

import java.util.List;

public interface IScanCallback {
    /**
     * 发现设备
     *
     * @param bluetoothDeviceExtend 发现的蓝牙设备
     * @param result    蓝牙设备集合 ，已排除重复的设备集合
     */
    void onDeviceFound(BluetoothDeviceExtend bluetoothDeviceExtend, List<BluetoothDeviceExtend> result);

    /**
     * 扫描完成
     *
     * @param result 蓝牙设备集合 ， 已排除重复的设备集合
     */
    void onScanFinish(List<BluetoothDeviceExtend> result);

    /**
     * 扫描超时
     */
    void onScanTimeout();
}
