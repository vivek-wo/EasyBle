package com.vivek.wo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.vivek.wo.ble.internal.BluetoothException;
import com.vivek.wo.ble.internal.GattCommsObserver;
import com.vivek.wo.ble.scan.OnScanCallback;
import com.vivek.wo.ble.scan.SingleFilterScanCallback;

import java.util.List;

public class IOTEasyBle implements GattCommsObserver {
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothComms mBluetoothComms;
    private GattCommsObserver mGattCommsObserver;
    private String deviceName;
    private String deviceAddress;
    private String serviceUuid;
    private String readableCharacteristicUuid;
    private String writableCharacteristicUuid;
    private String noticableCharacteristicUuid;
    private String noticableDescriptorUuid;

    IOTEasyBle(Builder builder) {
        mContext = builder.context;
        mBluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        this.deviceName = builder.deviceName;
        this.deviceAddress = builder.deviceAddress;
        this.serviceUuid = builder.serviceUuid;
        this.readableCharacteristicUuid = builder.readableCharacteristicUuid;
        this.writableCharacteristicUuid = builder.writableCharacteristicUuid;
        this.noticableCharacteristicUuid = builder.noticableCharacteristicUuid;
        this.noticableDescriptorUuid = builder.noticableDescriptorUuid;
    }

    /**
     * 设置蓝牙监听器
     *
     * @param observer
     */
    public void setBluetoothCommObserver(GattCommsObserver observer) {
        mGattCommsObserver = observer;
    }

    /**
     * 打开数据通知和关闭通知
     *
     * @param enable
     * @param listener
     * @throws BluetoothException
     */
    public void notify(boolean enable, OnActionListener listener) throws BluetoothException {
    }

    /**
     * 写数据
     *
     * @param data
     * @return
     * @throws BluetoothException
     */
    public void write(byte[] data) throws BluetoothException {
    }

    /**
     * 先搜索后连接
     */
    public void scanConnect() {
    }

    /**
     * 先搜索后连接
     *
     * @param scanSecond
     */
    public void scanConnect(int scanSecond) {
        scanConnect(scanSecond, null);
    }

    /**
     * 先搜索后连接
     *
     * @param scanSecond
     * @param listener
     */
    public void scanConnect(int scanSecond, final OnActionListener listener) {
        innerScanConnect(scanSecond, listener);
    }

    private void innerScanConnect(int scanSecond, final OnActionListener listener) {
        new SingleFilterScanCallback(mBluetoothAdapter,
                new OnScanCallback() {
                    @Override
                    public void onDeviceFound(BluetoothDeviceExtend bluetoothDeviceExtend, List<BluetoothDeviceExtend> result) {
                    }

                    @Override
                    public void onScanFinish(List<BluetoothDeviceExtend> result) {
                    }

                    @Override
                    public void onScanTimeout() {
                        if (listener != null) {
                            BluetoothException exception = new BluetoothException(
                                    "Scan timeout.");
                            listener.onFailure(exception);
                        }
                    }
                })
                .setDeviceAddress(this.deviceAddress)
                .setDeviceName(this.deviceName)
                .scanSecond(scanSecond)
                .scan();
    }

    /**
     * 直接进行蓝牙连接
     */
    public void connect() throws BluetoothException {
    }

    /**
     * 直接进行蓝牙连接
     *
     * @param timeout
     */
    public void connect(long timeout) throws BluetoothException {
        connect(timeout, null);
    }

    /**
     * 直接进行蓝牙连接
     *
     * @param timeout
     * @param listener
     */
    public void connect(long timeout, OnActionListener listener)
            throws BluetoothException {
    }

    private void connect(BluetoothDeviceExtend bluetoothDeviceExtend, long timeout,
                         OnActionListener listener) {
    }

    @Override
    public void connectComplete() {
        if (mGattCommsObserver != null) {
            mGattCommsObserver.connectComplete();
        }
    }

    @Override
    public void connectLost(boolean isActiveDisconnect, BluetoothException e) {
        if (mGattCommsObserver != null) {
            mGattCommsObserver.connectLost(isActiveDisconnect, e);
        }
    }

    public static final class Builder {
        private Context context;
        private String deviceName;
        private String deviceAddress;
        private String serviceUuid;
        private String readableCharacteristicUuid;
        private String writableCharacteristicUuid;
        private String noticableCharacteristicUuid;
        private String noticableDescriptorUuid;

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

        public Builder setServiceUuid(String serviceUuid) {
            this.serviceUuid = serviceUuid;
            return this;
        }

        public Builder setReadableCharacteristicUuid(String readableCharacteristicUuid) {
            this.readableCharacteristicUuid = readableCharacteristicUuid;
            return this;
        }

        public Builder setWritableCharacteristicUuid(String writableCharacteristicUuid) {
            this.writableCharacteristicUuid = writableCharacteristicUuid;
            return this;
        }

        public Builder setNoticableCharacteristicUuid(String noticableCharacteristicUuid) {
            this.noticableCharacteristicUuid = noticableCharacteristicUuid;
            return this;
        }

        public Builder setNoticableDescriptorUuid(String noticableDescriptorUuid) {
            this.noticableDescriptorUuid = noticableDescriptorUuid;
            return this;
        }

        public IOTEasyBle build() {
            return new IOTEasyBle(this);
        }
    }
}
