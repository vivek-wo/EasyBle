package com.vivek.wo.ble.internal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.vivek.wo.ble.MethodObject;
import com.vivek.wo.ble.MethodQueueHandler;
import com.vivek.wo.ble.OnScanCallback;
import com.vivek.wo.ble.ScanCallback;
import com.vivek.wo.ble.SingleFilterScanCallback;

import java.util.List;

public class IOTEasyBle implements BluetoothCommObserver {
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothComms mBluetoothComms;
    private MethodQueueHandler mMethodQueueHandler;
    private BluetoothCommObserver mBluetoothCommObserver;
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
        mMethodQueueHandler = new MethodQueueHandler();
        this.deviceName = builder.deviceName;
        this.deviceAddress = builder.deviceAddress;
        this.serviceUUIDString = builder.serviceUUIDString;
        this.readableCharacteristicUUIDString = builder.readableCharacteristicUUIDString;
        this.writableCharacteristicUUIDString = builder.writableCharacteristicUUIDString;
        this.noticableCharacteristicUUIDString = builder.noticableCharacteristicUUIDString;
        this.noticableDescriptorUUIDString = builder.noticableDescriptorUUIDString;
    }

    /**
     * 设置蓝牙监听器
     *
     * @param observer
     */
    public void setBluetoothCommObserver(BluetoothCommObserver observer) {
        mBluetoothCommObserver = observer;
    }

    /**
     * 打开数据通知和关闭通知
     *
     * @param enable
     * @param listener
     * @throws BluetoothException
     */
    public void notify(boolean enable, OnActionListener listener) throws BluetoothException {
        CommonMethod.checkNotConnected(mBluetoothComms);
        mBluetoothComms.notify(serviceUUIDString, noticableCharacteristicUUIDString,
                noticableDescriptorUUIDString, enable, false, listener).invoke();
    }

    /**
     * 写数据
     *
     * @param data
     * @return
     * @throws BluetoothException
     */
    public MethodProxy write(byte[] data) throws BluetoothException {
        CommonMethod.checkNotConnected(mBluetoothComms);
        return mBluetoothComms.write(serviceUUIDString, writableCharacteristicUUIDString, data);
    }

    /**
     * 先搜索后连接
     */
    public void scanConnect() {
        scanConnect(ScanCallback.DEFAULT_SCANSECOND);
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
                        mBluetoothComms = new BluetoothComms(mContext, bluetoothDeviceExtend);
                        mBluetoothComms.setMethodQueueHandler(mMethodQueueHandler);
                        mBluetoothComms.connect(listener).invoke();
                    }

                    @Override
                    public void onScanFinish(List<BluetoothDeviceExtend> result) {
                    }

                    @Override
                    public void onScanTimeout() {
                        if (listener != null) {
                            BluetoothException exception = new BluetoothException(
                                    BluetoothException.BLUETOOTH_SCAN_TIMEOUT,
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
        connect(MethodObject.DEFAULT_TIMEOUT);
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
    public void connect(long timeout, final OnActionListener listener) throws BluetoothException {
        innerConnect(timeout, listener);
    }

    private void innerConnect(long timeout, final OnActionListener listener) throws BluetoothException {
        CommonMethod.checkBluetoothAddress(deviceAddress);
        BluetoothDevice device = CommonMethod.getRemoteDevice(mBluetoothAdapter, deviceAddress);
        mBluetoothComms = new BluetoothComms(mContext, new BluetoothDeviceExtend(device));
        mBluetoothComms.setMethodQueueHandler(mMethodQueueHandler);
        mBluetoothComms.connect(listener).timeout(timeout).invoke();
    }

    @Override
    public void connectComplete() {
        if (mBluetoothCommObserver != null) {
            mBluetoothCommObserver.connectComplete();
        }
    }

    @Override
    public void connectLost(Throwable throwable) {
        if (mBluetoothCommObserver != null) {
            mBluetoothCommObserver.connectLost(throwable);
        }
    }

    @Override
    public void remoteDataChanged(byte[] data) {
        if (mBluetoothCommObserver != null) {
            mBluetoothCommObserver.remoteDataChanged(data);
        }
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
