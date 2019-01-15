package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.vivek.wo.ble.PrintLog;

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
        return new FunctionUuidToken(serviceUUID, characteristicUUID, "method-read",
                this).callback(callback).method(new IMethod() {
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
        IReadCallback callback = (IReadCallback) currentToken.getCallback();
        if (callback != null) {
            callback.onRead(currentToken, new String[]{characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString()}, status, characteristic.getValue());
        }
    }

    public Token write(String serviceUUID, String characteristicUUID, byte[] data) {
        return write(serviceUUID, characteristicUUID, data, null);
    }

    public Token write(String serviceUUID, String characteristicUUID, byte[] data,
                       ITimeoutCallback callback) {
        return new FunctionUuidToken(serviceUUID, characteristicUUID, "method-write",
                this).args(data).callback(callback).method(new IMethod() {
            @Override
            public Object onMethod(Object[] args) {
                return write((BluetoothGattCharacteristic) args[0], (byte[]) args[2]);
            }
        });
    }

    public void notify(boolean enable, boolean isIndication) {

    }

    public void notify(boolean enable, boolean isIndication, ITimeoutCallback callback) {

    }

    public void rssi() {

    }

    public void rssi(ITimeoutCallback callback) {

    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    public void disconnect(ITimeoutCallback callback) {

    }
}
