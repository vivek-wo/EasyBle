package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.vivek.wo.ble.comms.CharacteristicHelper;
import com.vivek.wo.ble.comms.FunctionToken;
import com.vivek.wo.ble.comms.IMethod;
import com.vivek.wo.ble.comms.Token;

import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    BluetoothDeviceExtend bluetoothDeviceExtend;

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
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
            PrintLog.log(TAG, currentToken + " onCallbackCurrentToken " + connectStateEnum.getCode()
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
    void onConnectionStateChange(BluetoothGatt gatt, int status, ConnectStateEnum connectStateEnum) {
        super.onConnectionStateChange(gatt, status, connectStateEnum);
        Token currentToken = onCallbackCurrentToken();
        if (currentToken == null) {
            return;
        }
        if (currentToken.getTokenContext() != "method-connect") {
            return;
        }
        mFunctionQueueHandler.dequeue();
        ConnectObserver callback = (ConnectObserver) currentToken.getCallback();
        if (callback != null) {
            if (connectStateEnum == ConnectStateEnum.CONNECT_SUCCESS) {
                //TODO 临时添加
                bluetoothDeviceExtend.setConnected(true);
                callback.onConnected(currentToken);
            } else {
                if (connectStateEnum == ConnectStateEnum.CONNECT_DISCONNECT) {
                    //TODO 临时添加
                    bluetoothDeviceExtend.setConnected(false);
                    callback.onDisconnected(currentToken, status, isActiveDisconnect);
                } else {
                    callback.onConnectFailure(currentToken, connectStateEnum.getCode());
                }
            }
        }
    }


    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
    }

    public Token connect() {
        return connect(null);
    }

    public Token connect(OnActionListener listener) {
        return new FunctionToken("method-connect", this)
                .callback(callback)
                .method(new IMethod() {
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
                      OnActionListener listener) {
        BluetoothGattService bluetoothGattService = getBluetoothGatt()
                .getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic = CharacteristicHelper
                .findReadableCharacteristic(bluetoothGattService, UUID.fromString(characteristicUUID));
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
                       OnActionListener listener) {
        BluetoothGattService bluetoothGattService = getBluetoothGatt()
                .getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic = CharacteristicHelper
                .findWritableCharacteristic(bluetoothGattService, UUID.fromString(characteristicUUID),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
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
                        boolean enable, boolean isIndication, OnActionListener listener) {
        BluetoothGattService bluetoothGattService = getBluetoothGatt()
                .getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic = CharacteristicHelper
                .findNotifyCharacteristic(bluetoothGattService, UUID.fromString(characteristicUUID));
        BluetoothGattDescriptor descriptor = CharacteristicHelper.findDescriptor(characteristic,
                UUID.fromString(descriptorUUID));
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

    public Token readRssi() {
        return readRssi(null);
    }

    public Token readRssi(OnActionListener listener) {
        return new FunctionToken("method-rssi", this)
                .callback(callback)
                .method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        return readRemoteRssi();
                    }
                });
    }

    public void disconnect(OnActionListener listener) {

    }

}
