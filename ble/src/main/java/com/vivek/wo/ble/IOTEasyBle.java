package com.vivek.wo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

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
    private long scanTimeout;
    private long connectTimeout;

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
        this.scanTimeout = builder.scanTimeout;
        this.connectTimeout = builder.connectTimeout;
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
    public void notify(boolean enable, final OnActionListener listener) throws BluetoothException {
        NotifyToken notifyToken = mBluetoothComms.createNotifyToken(serviceUuid,
                noticableCharacteristicUuid, noticableDescriptorUuid);
        if (listener != null) {
            notifyToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    if (listener != null) {
                        listener.onSuccess(args);
                    }
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    if (listener != null) {
                        listener.onFailure(exception);
                    }
                }
            });
        }
        notifyToken.notify(enable);
    }

    /**
     * 写数据
     *
     * @param data
     * @return
     * @throws BluetoothException
     */
    public void write(byte[] data) throws BluetoothException {
        write(data, null);
    }

    /**
     * 写数据
     *
     * @param data
     * @return
     * @throws BluetoothException
     */
    public void write(byte[] data, final OnActionListener listener) throws BluetoothException {
        WriteToken writeToken = mBluetoothComms.createWriteToken(serviceUuid, writableCharacteristicUuid);
        if (listener != null) {
            writeToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    if (listener != null) {
                        listener.onSuccess(args);
                    }
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    if (listener != null) {
                        listener.onFailure(exception);
                    }
                }
            });
        }
        writeToken.write(data);

    }

    /**
     * 连接
     */
    public void connect(final OnActionListener listener) {
        new SingleFilterScanCallback(mBluetoothAdapter,
                new OnScanCallback() {
                    @Override
                    public void onDeviceFound(BluetoothDeviceExtend bluetoothDeviceExtend,
                                              List<BluetoothDeviceExtend> result) {
                        mBluetoothComms = new BluetoothComms(mContext, bluetoothDeviceExtend);
                        mBluetoothComms.createConnectToken()
                                .setOnActionListener(new OnActionListener() {
                                    @Override
                                    public void onSuccess(Object... args) {
                                        if (listener != null) {
                                            listener.onSuccess(args);
                                        }
                                    }

                                    @Override
                                    public void onFailure(BluetoothException exception) {
                                        if (listener != null) {
                                            listener.onFailure(exception);
                                        }
                                    }
                                }).setTimeout(connectTimeout).connect();
                    }

                    @Override
                    public void onScanFinish(List<BluetoothDeviceExtend> result) {
                    }

                    @Override
                    public void onScanTimeout() {
                        if (listener != null) {
                            listener.onFailure(new BluetoothException("Scan timeout."));
                        }
                    }
                })
                .setDeviceAddress(this.deviceAddress)
                .setDeviceName(this.deviceName)
                .scanTimeout(scanTimeout)
                .scan();
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
        public static final long DEFAULT_SCAN_TIMEOUT = 1 * 1000;
        public static final long DEFAULT_CONNECT_TIMEOUT = 1 * 1000;
        private Context context;
        private String deviceName;
        private String deviceAddress;
        private String serviceUuid;
        private String readableCharacteristicUuid;
        private String writableCharacteristicUuid;
        private String noticableCharacteristicUuid;
        private String noticableDescriptorUuid;
        private long scanTimeout = DEFAULT_SCAN_TIMEOUT;
        private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;

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

        public Builder setScanTimeout(long scanTimeout) {
            this.scanTimeout = scanTimeout;
            return this;
        }

        public Builder setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public IOTEasyBle build() {
            return new IOTEasyBle(this);
        }
    }
}
