package com.vivek.wo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.vivek.wo.ble.comms.BluetoothComms;
import com.vivek.wo.ble.comms.BluetoothDeviceExtend;
import com.vivek.wo.ble.scan.IScanCallback;
import com.vivek.wo.ble.scan.SingleFilterScanCallback;

import java.util.List;

public class IOTEasyBle {
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothComms mBluetoothComms;
    private String deviceName;
    private String deviceAddress;

    public IOTEasyBle(Context context) {
        mBluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public IOTEasyBle setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public IOTEasyBle setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        return this;
    }

    public void write(String data) {

    }


    private void scanConnect() {
        new SingleFilterScanCallback(mBluetoothAdapter, new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothDeviceExtend bluetoothDeviceExtend, List<BluetoothDeviceExtend> result) {
                mBluetoothComms = new BluetoothComms(mContext, bluetoothDeviceExtend);
            }

            @Override
            public void onScanFinish(List<BluetoothDeviceExtend> result) {
            }

            @Override
            public void onScanTimeout() {
            }
        }).setDeviceAddress(this.deviceAddress).setDeviceName(this.deviceName).scan();
    }

    private void directConnect() {
        if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
    }
}
