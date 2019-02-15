package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.vivek.wo.ble.PrintLog;

import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    private FunctionQueueHandler mFunctionQueueHandler;
    BluetoothDeviceExtend bluetoothDeviceExtend;

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
        mFunctionQueueHandler = FunctionQueueHandler.getMainQueueHandler();
    }

    /**
     * 超时返回
     *
     * @param token
     */
    void onTimeoutCallback(Token token) {
        Token currentToken;
        synchronized (this) {
            currentToken = mFunctionQueueHandler.get();
            PrintLog.log(TAG, token + " onTimeoutCallback " + token.getTokenContext()
                    + " And Current " + currentToken);
            if (currentToken == token) {
                //当前执行超时
                if (currentToken.isCompleted()) {
                    return;
                }
                currentToken.setCompleted(true);
            } else {
                //队列Function执行超时
                token.setCompleted(true);
                mFunctionQueueHandler.remove(token);
            }
        }
        if (currentToken == token) {
            mFunctionQueueHandler.dequeue();
        }
        ITimeoutCallback callback = token.getCallback();
        if (callback != null) {
            callback.onTimeout(token);
        }
    }

    void execute(Token token) {
        mFunctionQueueHandler.enqueue(token);
    }

    Token onCallbackCurrentToken() {
        Token currentToken;
        synchronized (this) {
            currentToken = mFunctionQueueHandler.get();
            PrintLog.log(TAG, currentToken + " onCallbackCurrentToken " + connectState.getCode()
                    + " " + (currentToken != null ? currentToken.getTokenContext() : ""));
            if (currentToken == null) {
                return null;
            }
            if (currentToken.isCompleted()) {
                return null;
            }
            currentToken.setCompleted(true);
            mFunctionQueueHandler.cancelTimeout(currentToken);
        }
        return currentToken;
    }

    @Override
    void onConnectionStateChange(BluetoothGatt gatt, ConnectState connectState) {
        super.onConnectionStateChange(gatt, connectState);
        Token currentToken = onCallbackCurrentToken();
        if (currentToken == null) {
            return;
        }
        if (currentToken.getTokenContext() != "method-connect") {
            return;
        }
        mFunctionQueueHandler.dequeue();
        IConnectCallback callback = (IConnectCallback) currentToken.getCallback();
        if (callback != null) {
            if (connectState == ConnectState.CONNECT_SUCCESS) {
                //TODO 临时添加
                bluetoothDeviceExtend.setConnected(true);
                callback.onConnected(currentToken);
            } else {
                if (connectState == ConnectState.CONNECT_DISCONNECT) {
                    //TODO 临时添加
                    bluetoothDeviceExtend.setConnected(false);
                    callback.onDisconnected(currentToken, isActiveDisconnect);
                } else {
                    callback.onConnectFailure(currentToken, connectState.getCode());
                }
            }
        }
    }

    public Token connect() {
        return connect(null);
    }

    public Token connect(IConnectCallback callback) {
        return new FunctionToken("method-connect", this)
                .callback(callback).method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        connect(bluetoothDeviceExtend.getBluetoothDevice(), true);
                        return true;
                    }
                });
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        Token currentToken = onCallbackCurrentToken();
        if (currentToken == null) {
            return;
        }
        if (currentToken.getTokenContext() != "method-read") {
            return;
        }
        mFunctionQueueHandler.dequeue();
        IReadCallback callback = (IReadCallback) currentToken.getCallback();
        if (callback != null) {
            callback.onRead(currentToken, new String[]{characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString()}, status, characteristic.getValue());
        }
    }

    public Token read(String serviceUUID, String characteristicUUID) {
        return read(serviceUUID, characteristicUUID, null);
    }

    public Token read(String serviceUUID, String characteristicUUID,
                      IReadCallback callback) {
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        return new FunctionToken("method-read", this)
                .args(characteristic)
                .callback(callback)
                .method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        return read((BluetoothGattCharacteristic) args[0]);
                    }
                });
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        Token currentToken = onCallbackCurrentToken();
        if (currentToken == null) {
            return;
        }
        if (currentToken.getTokenContext() != "method-write") {
            return;
        }
        mFunctionQueueHandler.dequeue();
        IWriteCallback callback = (IWriteCallback) currentToken.getCallback();
        if (callback != null) {
            callback.onWrite(currentToken, new String[]{characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString()}, status);
        }
    }

    public Token write(String serviceUUID, String characteristicUUID, byte[] data) {
        return write(serviceUUID, characteristicUUID, data, null);
    }

    public Token write(String serviceUUID, String characteristicUUID, byte[] data,
                       ITimeoutCallback callback) {
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        return new FunctionToken("method-write", this)
                .args(characteristic, data)
                .callback(callback)
                .method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        return write((BluetoothGattCharacteristic) args[0], (byte[]) args[1]);
                    }
                });
    }

    public Token notify(String serviceUUID, String characteristicUUID, String descriptorUUID,
                        boolean enable, boolean isIndication) {
        return notify(serviceUUID, characteristicUUID, descriptorUUID, enable,
                isIndication, null);
    }

    public Token notify(String serviceUUID, String characteristicUUID, String descriptorUUID,
                        boolean enable, boolean isIndication, ITimeoutCallback callback) {
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        BluetoothGattDescriptor descriptor = findDescriptor(characteristic, descriptorUUID);
        return new FunctionToken("method-notify", this)
                .args(characteristic, descriptor, enable, isIndication)
                .callback(callback)
                .method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        return enable((BluetoothGattCharacteristic) args[0], (BluetoothGattDescriptor) args[1],
                                (Boolean) args[2], (Boolean) args[3]);
                    }
                });
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    public Token rssi() {
        return rssi(null);
    }

    public Token rssi(IRssiCallback callback) {
        return new FunctionToken("method-rssi", this).callback(callback)
                .method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        return readRssi();
                    }
                });
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    public void disconnect(ITimeoutCallback callback) {

    }

    private BluetoothGattCharacteristic findCharacteristic(String serviceUUID, String characteristicUUID) {
        BluetoothGattService bluetoothGattService = null;
        if (serviceUUID != null) {
            bluetoothGattService = getBluetoothGatt()
                    .getService(UUID.fromString(serviceUUID));
        }
        BluetoothGattCharacteristic characteristic = null;
        if (bluetoothGattService != null && characteristicUUID != null) {
            characteristic = bluetoothGattService.
                    getCharacteristic(UUID.fromString(characteristicUUID));
        }
        return characteristic;
    }

    private BluetoothGattDescriptor findDescriptor(BluetoothGattCharacteristic characteristic,
                                                   String descriptorUUID) {
        BluetoothGattDescriptor descriptor = null;
        if (characteristic != null && descriptorUUID != null) {
            descriptor = characteristic.
                    getDescriptor(UUID.fromString(descriptorUUID));
        }
        return descriptor;
    }

}
