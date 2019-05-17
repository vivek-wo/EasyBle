package com.vivek.wo.ble.internal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class CommonMethod {

    static void checkBluetoothAddress(String deviceAddress) throws BluetoothException {
        if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
            throw new BluetoothException(
                    new IllegalArgumentException("Connect deviceAddress not a String Bluetooth address."));
        }
    }

    static void checkNotConnected(BluetoothComms comms) throws BluetoothException {
        if (!comms.isConnected()) {
            throw new BluetoothException(
                    BluetoothException.BLUETOOTH_REMOTEDEVICE_NOICONNECTED,
                    "Execute not allowed before connected.");
        }
    }

    static BluetoothDevice getRemoteDevice(BluetoothAdapter bluetoothAdapter, String deviceAddress)
            throws BluetoothException {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device == null) {
            throw new BluetoothException(
                    BluetoothException.BLUETOOTH_REMOTEDEVICE_NOIFOUND,
                    "Get remoteDevice NULL");
        }
        return device;
    }
}
