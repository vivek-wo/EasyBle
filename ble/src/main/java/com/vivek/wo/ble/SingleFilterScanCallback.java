package com.vivek.wo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

public class SingleFilterScanCallback extends ScanCallback {
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private String deviceAddress;
    private String deviceName;

    public SingleFilterScanCallback(Context context, OnScanCallback scanCallback) {
        super(context, scanCallback);
    }

    public SingleFilterScanCallback(BluetoothAdapter bluetoothAdapter, OnScanCallback scanCallback) {
        super(bluetoothAdapter, scanCallback);
    }

    /**
     * 设置搜索设备MAC地址，如若同时设置了DeviceName，此优先级高
     *
     * @param deviceAddress
     * @return
     */
    public SingleFilterScanCallback setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        return this;
    }

    /**
     * 设置搜索设备名称，如若同时设置了DeviceAddress，DeviceAddress设置优先级高
     *
     * @param deviceName
     * @return
     */
    public SingleFilterScanCallback setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceAddress != null && device.getAddress().equalsIgnoreCase(deviceAddress)) {
            if (atomicBoolean.compareAndSet(false, true)) {
                stop();
                return true;
            }
        } else if (deviceName != null && device.getName().equalsIgnoreCase(deviceName)) {
            if (atomicBoolean.compareAndSet(false, true)) {
                stop();
                return true;
            }
        }
        return false;
    }
}
