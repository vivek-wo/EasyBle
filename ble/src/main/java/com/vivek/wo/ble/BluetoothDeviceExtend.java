package com.vivek.wo.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class BluetoothDeviceExtend implements Parcelable {
    public static final Creator<BluetoothDeviceExtend> CREATOR = new Creator<BluetoothDeviceExtend>() {
        @Override
        public BluetoothDeviceExtend createFromParcel(Parcel in) {
            return new BluetoothDeviceExtend(in);
        }

        @Override
        public BluetoothDeviceExtend[] newArray(int size) {
            return new BluetoothDeviceExtend[size];
        }
    };
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private byte[] scanRecord;

    public BluetoothDeviceExtend(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BluetoothDeviceExtend(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    protected BluetoothDeviceExtend(Parcel in) {
        bluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        rssi = in.readInt();
        scanRecord = in.createByteArray();
    }

    public String getDeviceName() {
        return bluetoothDevice != null ? bluetoothDevice.getName() : null;
    }

    public String getDeviceAddress() {
        return bluetoothDevice != null ? bluetoothDevice.getAddress() : null;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRssi() {
        return this.rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecord() {
        return this.scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bluetoothDevice, flags);
        dest.writeInt(rssi);
        dest.writeByteArray(scanRecord);
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + bluetoothDevice.getName() + "\" , " +
                " \"deviceAddress\": \"" + bluetoothDevice.getAddress() + "\" }";
    }
}
