package com.vivek.wo.ble.internal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class GattComms extends BluetoothGattCallback {
    private static final String TAG = "GattComms";
    //系统默认
    public static final String CLIENT_CHARACTERISTIC_CONFIG =
            "00002902-0000-1000-8000-00805f9b34fb";
    private Context mContext;
    private boolean isActiveDisconnect = false;//是否主动断开连接
    private volatile ConnectStateEnum mConnectState = ConnectStateEnum.STATE_NOTCONNECT;//连接状态
    private List<GattCommsObserver> mGattCommsObservers = new ArrayList<>(1);

    protected BluetoothGatt mBluetoothGatt; //提供子类使用


    enum ConnectStateEnum {
        /**
         * 无连接
         */
        STATE_NOTCONNECT(-1),
        /**
         * 已连接
         */
        STATE_CONNECTED(BluetoothGatt.STATE_CONNECTED),
        /**
         * 正在连接
         */
        STATE_CONNECTING(BluetoothGatt.STATE_CONNECTING),
        /**
         * 已断开连接
         */
        STATE_DISCONNECTED(BluetoothGatt.STATE_DISCONNECTED),
        /**
         * 正在断开连接
         */
        STATE_DISCONNECTING(BluetoothGatt.STATE_DISCONNECTING);

        private int connectState;

        ConnectStateEnum(int connectState) {
            this.connectState = connectState;
        }

        public int getConnectState() {
            return connectState;
        }
    }

    protected GattComms(Context context) {
        mContext = context;
    }

    private void changeConnectionState(ConnectStateEnum connectStateEnum) {
        synchronized (mConnectState) {
            mConnectState = connectStateEnum;
        }
    }

    /**
     * Callback indicating when GATT client has connected/disconnected to/from a remote
     * GATT server.
     *
     * @param gatt     GATT client
     * @param status   Status of the connect or disconnect operation.
     *                 {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     * @param newState Returns the new connection state. Can be one of
     *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
     *                 {@link BluetoothProfile#STATE_CONNECTED}
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (isConnected()) {
                changeConnectionState(ConnectStateEnum.STATE_DISCONNECTED);
            } else {
                changeConnectionState(ConnectStateEnum.STATE_NOTCONNECT);
            }
            gatt.close();
            observeGattCommsConnectFailure(status, new BluetoothException(status,
                    "onConnectionStateChange State Disconnected " + status));
        } else if (newState == BluetoothGatt.STATE_CONNECTING) {
            changeConnectionState(ConnectStateEnum.STATE_CONNECTING);
        } else if (newState == BluetoothGatt.STATE_DISCONNECTING) {
            changeConnectionState(ConnectStateEnum.STATE_DISCONNECTING);
        }
    }

    /**
     * Callback invoked when the list of remote services, characteristics and descriptors
     * for the remote device have been updated, ie new services have been discovered.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#discoverServices}
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device
     *               has been explored successfully.
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mBluetoothGatt = gatt;
            isActiveDisconnect = false;
            changeConnectionState(ConnectStateEnum.STATE_CONNECTED);
            observeGattCommsConnectComplete();
        } else {
            gatt.disconnect();
            gatt.close();
            observeGattCommsConnectFailure(status, new BluetoothException(status,
                    "onServicesDiscovered Failure " + status));
        }
    }

    /**
     * 添加蓝牙连接监听
     *
     * @param observer
     */
    public void addGattCommsObserver(GattCommsObserver observer) {
        synchronized (mGattCommsObservers) {
            if (!mGattCommsObservers.contains(observer)) {
                mGattCommsObservers.add(observer);
            }
        }
    }

    /**
     * 移除蓝牙连接监听
     *
     * @param observer
     */
    public void removeGattCommsObserver(GattCommsObserver observer) {
        synchronized (mGattCommsObservers) {
            if (mGattCommsObservers != null && mGattCommsObservers.contains(observer)) {
                mGattCommsObservers.remove(observer);
            }
        }
    }

    private void observeGattCommsConnectComplete() {
        synchronized (mGattCommsObservers) {
            for (GattCommsObserver observer : mGattCommsObservers) {
                observer.connectComplete();
            }
        }
    }

    private void observeGattCommsConnectFailure(int status, BluetoothException e) {
        synchronized (mGattCommsObservers) {
            for (GattCommsObserver observer : mGattCommsObservers) {
                observer.connectLost(e);
            }
        }
    }

    /**
     * 蓝牙设备是否连接（并且发现服务）
     *
     * @return
     */
    public boolean isConnected() {
        boolean isConnected;
        synchronized (mConnectState) {
            isConnected = (mConnectState == ConnectStateEnum.STATE_CONNECTED);
        }
        return isConnected;
    }

    /**
     * @return
     */
    public boolean isDisconnected() {
        boolean isDisconnected;
        synchronized (mConnectState) {
            isDisconnected = (mConnectState == ConnectStateEnum.STATE_DISCONNECTED);
        }
        return isDisconnected;
    }

    /**
     * Callback reporting the result of a characteristic read operation.
     *
     * @param gatt           GATT client invoked {@link BluetoothGatt#readCharacteristic}
     * @param characteristic Characteristic that was read from the associated
     *                       remote device.
     * @param status         {@link BluetoothGatt#GATT_SUCCESS} if the read operation
     *                       was completed successfully.
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    /**
     * Callback indicating the result of a characteristic write operation.
     * <p>
     * <p>If this callback is invoked while a reliable write transaction is
     * in progress, the value of the characteristic represents the value
     * reported by the remote device. An application should compare this
     * value to the desired value to be written. If the values don't match,
     * the application must abort the reliable write transaction.
     *
     * @param gatt           GATT client invoked {@link BluetoothGatt#writeCharacteristic}
     * @param characteristic Characteristic that was written to the associated
     *                       remote device.
     * @param status         The result of the write operation
     *                       {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    /**
     * Callback triggered as a result of a remote characteristic notification.
     *
     * @param gatt           GATT client the characteristic is associated with
     * @param characteristic Characteristic that has been updated as a result
     *                       of a remote notification event.
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
    }

    /**
     * Callback reporting the result of a descriptor read operation.
     *
     * @param gatt       GATT client invoked {@link BluetoothGatt#readDescriptor}
     * @param descriptor Descriptor that was read from the associated
     *                   remote device.
     * @param status     {@link BluetoothGatt#GATT_SUCCESS} if the read operation
     *                   was completed successfully
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
    }

    /**
     * Callback indicating the result of a descriptor write operation.
     *
     * @param gatt       GATT client invoked {@link BluetoothGatt#writeDescriptor}
     * @param descriptor Descriptor that was writte to the associated
     *                   remote device.
     * @param status     The result of the write operation
     *                   {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    /**
     * Callback reporting the RSSI for a remote device connection.
     * <p>
     * This callback is triggered in response to the
     * {@link BluetoothGatt#readRemoteRssi} function.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#readRemoteRssi}
     * @param rssi   The RSSI value for the remote device
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the RSSI was read successfully
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    /**
     * Callback indicating the MTU for a given device connection has changed.
     * <p>
     * This callback is triggered in response to the
     * {@link BluetoothGatt#requestMtu} function, or in response to a connection
     * event.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#requestMtu}
     * @param mtu    The new MTU size
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the MTU has been changed successfully
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
    }

    private static void checkCharacteristicNULL(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            throw new NullPointerException("BluetoothGattCharacteristic cannot be NULL");
        }
    }

    protected void innerConnect(BluetoothDevice bluetoothDevice, boolean autoConnect) {
        changeConnectionState(ConnectStateEnum.STATE_CONNECTING);
        bluetoothDevice.connectGatt(mContext, autoConnect, this);
    }

    boolean read(BluetoothGattCharacteristic characteristic) {
        checkCharacteristicNULL(characteristic);
        return mBluetoothGatt.readCharacteristic(characteristic);
    }

    boolean write(BluetoothGattCharacteristic characteristic, byte[] data) {
        checkCharacteristicNULL(characteristic);
        characteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * @param characteristic
     * @param data
     * @param writeType      The write type to for this characteristic. Can be one
     *                       of:
     *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
     *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or
     *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return
     */
    boolean write(BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
        checkCharacteristicNULL(characteristic);
        characteristic.setWriteType(writeType);
        characteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    boolean readRemoteRssi() {
        return mBluetoothGatt.readRemoteRssi();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    boolean setMTU(int mtu) {
        return mBluetoothGatt.requestMtu(mtu);
    }

    boolean enable(BluetoothGattCharacteristic characteristic,
                   BluetoothGattDescriptor descriptor, boolean enable, boolean isIndication) {
        checkCharacteristicNULL(characteristic);
        boolean result = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (!result) {
            return false;
        }
        if (descriptor == null) {
            descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        }
        if (!isIndication) {
            if ((characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            } else if ((characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
        } else {
            if ((characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
        }
        mBluetoothGatt.writeDescriptor(descriptor);
        return true;
    }

    boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mBluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(mBluetoothGatt);
                return success;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return
     */
    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    void disconnect() {
        isActiveDisconnect = true;
        changeConnectionState(ConnectStateEnum.STATE_DISCONNECTING);
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
