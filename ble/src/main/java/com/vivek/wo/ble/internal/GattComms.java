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
        mBluetoothGatt = gatt;
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (isConnected()) {
                changeConnectionState(ConnectStateEnum.STATE_DISCONNECTED);
            } else {
                changeConnectionState(ConnectStateEnum.STATE_NOTCONNECT);
            }
            refreshDeviceCache();
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
        mBluetoothGatt = gatt;
        if (status == BluetoothGatt.GATT_SUCCESS) {
            isActiveDisconnect = false;
            changeConnectionState(ConnectStateEnum.STATE_CONNECTED);
            observeGattCommsConnectComplete();
        } else {
            gatt.disconnect();
            refreshDeviceCache();
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
                observer.connectLost(isActiveDisconnect, e);
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
     * 蓝牙设备是否断开连接
     *
     * @return
     */
    public boolean isDisconnected() {
        boolean isDisconnected;
        synchronized (mConnectState) {
            isDisconnected = (mConnectState == ConnectStateEnum.STATE_DISCONNECTED);
        }
        return isDisconnected;
    }

    private static void checkCharacteristicNULL(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            throw new NullPointerException("BluetoothGattCharacteristic cannot be NULL");
        }
    }

    /**
     * @param bluetoothDevice
     * @param autoConnect
     */
    protected void innerConnect(BluetoothDevice bluetoothDevice, boolean autoConnect) {
        changeConnectionState(ConnectStateEnum.STATE_CONNECTING);
        bluetoothDevice.connectGatt(mContext, autoConnect, this);
    }

    /**
     * @param characteristic
     * @return
     */
    protected boolean innerRead(BluetoothGattCharacteristic characteristic) {
        checkCharacteristicNULL(characteristic);
        return mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * @param characteristic
     * @param data
     * @return
     */
    protected boolean innerWrite(BluetoothGattCharacteristic characteristic, byte[] data) {
        checkCharacteristicNULL(characteristic);
        characteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * @return
     */
    protected boolean readRemoteRssi() {
        return mBluetoothGatt.readRemoteRssi();
    }

    /**
     * @param mtu
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected boolean requestMtu(int mtu) {
        return mBluetoothGatt.requestMtu(mtu);
    }

    /**
     * @param characteristic
     * @param descriptor
     * @param enable
     * @param isIndication
     * @return
     */
    protected boolean enable(BluetoothGattCharacteristic characteristic,
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

    /**
     * @return
     */
    protected boolean refreshDeviceCache() {
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

    /**
     *
     */
    protected void innerDisconnectAndClose() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            refreshDeviceCache();
            mBluetoothGatt.close();
        }
    }

    /**
     *
     */
    protected void innerDisconnect() {
        isActiveDisconnect = true;
        changeConnectionState(ConnectStateEnum.STATE_DISCONNECTING);
        innerDisconnectAndClose();
        mBluetoothGatt = null;
    }

}
