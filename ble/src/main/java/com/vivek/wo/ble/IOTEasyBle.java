package com.vivek.wo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

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
    private String serviceUUIDString;
    private String readableCharacteristicUUIDString;
    private String writableCharacteristicUUIDString;
    private String noticableCharacteristicUUIDString;
    private String noticableDescriptorUUIDString;

    IOTEasyBle(Builder builder) {
        mContext = builder.context;
        mBluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        this.deviceName = builder.deviceName;
        this.deviceAddress = builder.deviceAddress;
        this.serviceUUIDString = builder.serviceUUIDString;
        this.readableCharacteristicUUIDString = builder.readableCharacteristicUUIDString;
        this.writableCharacteristicUUIDString = builder.writableCharacteristicUUIDString;
        this.noticableCharacteristicUUIDString = builder.noticableCharacteristicUUIDString;
        this.noticableDescriptorUUIDString = builder.noticableDescriptorUUIDString;
    }

    public void connect() {

    }

    public void notify(boolean enable) {

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

    public static final class Builder {
        private Context context;
        private String deviceName;
        private String deviceAddress;
        private String serviceUUIDString;
        private String readableCharacteristicUUIDString;
        private String writableCharacteristicUUIDString;
        private String noticableCharacteristicUUIDString;
        private String noticableDescriptorUUIDString;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder setDeviceAddress(String deviceAddress) {
            this.deviceAddress = deviceAddress;
            return this;
        }

        public Builder setServiceUUIDString(String serviceUUIDString) {
            this.serviceUUIDString = serviceUUIDString;
            return this;
        }

        public Builder setReadableCharacteristicUUIDString(String readableCharacteristicUUIDString) {
            this.readableCharacteristicUUIDString = readableCharacteristicUUIDString;
            return this;
        }

        public Builder setWritableCharacteristicUUIDString(String writableCharacteristicUUIDString) {
            this.writableCharacteristicUUIDString = writableCharacteristicUUIDString;
            return this;
        }

        public Builder setNoticableCharacteristicUUIDString(String noticableCharacteristicUUIDString) {
            this.noticableCharacteristicUUIDString = noticableCharacteristicUUIDString;
            return this;
        }

        public Builder setNoticableDescriptorUUIDString(String noticableDescriptorUUIDString) {
            this.noticableDescriptorUUIDString = noticableDescriptorUUIDString;
            return this;
        }

        public IOTEasyBle build() {
            return new IOTEasyBle(this);
        }
    }
}
