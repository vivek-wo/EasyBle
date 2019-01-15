package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.lang.reflect.Method;

public abstract class GattComms extends BluetoothGattCallback {
    private static final String TAG = "GattComms";
    Context mContext;
    protected BluetoothGatt mBluetoothGatt;
    volatile ConnectState connectState = ConnectState.CONNECT_INIT;//连接状态
    boolean isActiveDisconnect = false;//是否主动断开连接

    protected GattComms(Context context) {
        mContext = context;
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
            return;
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectState = ConnectState.CONNECT_DISCONNECT;
            } else {
                connectState = ConnectState.CONNECT_FAILURE;
            }
        } else if (newState == BluetoothGatt.STATE_CONNECTING) {
            connectState = ConnectState.CONNECT_PROCESS;
        }
        onConnectionStateChange(gatt, connectState);
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
            connectState = ConnectState.CONNECT_SUCCESS;
            mBluetoothGatt = gatt;
            isActiveDisconnect = false;
        } else {
            gatt.disconnect();
            gatt.close();
            connectState = ConnectState.CONNECT_FAILURE;
        }
        onConnectionStateChange(gatt, connectState);
    }

    void onConnectionStateChange(BluetoothGatt gatt, ConnectState connectState) {
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

    void connect(BluetoothDevice bluetoothDevice, boolean autoConnect) {
        if (connectState == ConnectState.CONNECT_SUCCESS) {
            return;
        }
        if (connectState == ConnectState.CONNECT_PROCESS) {
            return;
        }
        connectState = ConnectState.CONNECT_PROCESS;
        bluetoothDevice.connectGatt(mContext, autoConnect, this);
    }

    boolean read(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            throw new NullPointerException("BluetoothGattCharacteristic Cannot be NULL");
        }
        return mBluetoothGatt.readCharacteristic(characteristic);
    }

    boolean write(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (characteristic == null) {
            throw new NullPointerException("BluetoothGattCharacteristic Cannot be NULL");
        }
//        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public boolean readRssi() {
        return mBluetoothGatt.readRemoteRssi();
    }

    boolean enable(BluetoothGattCharacteristic characteristic,
                   BluetoothGattDescriptor descriptor, boolean enable, boolean isIndication) {
        if (characteristic == null) {
            throw new NullPointerException("BluetoothGattCharacteristic Cannot be NULL");
        }
        if (descriptor == null) {
            throw new NullPointerException("BluetoothGattDescriptor Cannot be NULL");
        }
        boolean result = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (!result) {
            return false;
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
        return result;
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

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void disconnect() {
        isActiveDisconnect = true;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
