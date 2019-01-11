package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.vivek.wo.ble.PrintLog;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    private IQueueHandler mQueueHandler;
    BluetoothDeviceExtend bluetoothDeviceExtend;

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
        mQueueHandler = FunctionQueueHandler.getMainQueueHandler();
    }

    public void setQueueHandler(IQueueHandler queueHandler) {
        mQueueHandler = queueHandler;
    }

    public IQueueHandler getQueueHandler() {
        return mQueueHandler;
    }

    void onTimeoutCallback(IToken token) {
        mQueueHandler.onTimeout(token);
    }

    @Override
    void onConnectionStateChange(BluetoothGatt gatt, ConnectState connectState) {
        super.onConnectionStateChange(gatt, connectState);
        IToken token = mQueueHandler.get();
        if (token == null) {
            disconnect();
            return;
        }
        PrintLog.log(TAG, token + " onConnectionStateChange " + connectState.getCode());
        if (!token.getMethodContext().equals("method-connect")) {
            return;
        }
        IConnectCallback callback = (IConnectCallback) token.getCallback();
        if (callback != null) {
            if (connectState == ConnectState.CONNECT_SUCCESS) {
                //TODO 临时添加
                bluetoothDeviceExtend.setConnected(true);
                callback.onConnected(this);
            } else {
                if (connectState == ConnectState.CONNECT_DISCONNECT) {
                    //TODO 临时添加
                    bluetoothDeviceExtend.setConnected(false);
                    callback.onDisconnected(this, isActiveDisconnect);
                } else {
                    callback.onConnectFailure(this, connectState.getCode());
                }
            }
        }
        mQueueHandler.dequeue();
    }

    public IToken connect() {
        return connect(null);
    }

    public IToken connect(IConnectCallback callback) {
        return new FunctionToken("method-connect", this)
                .callback(callback).method(new IMethod() {
                    @Override
                    public Object onMethod(Object[] args) {
                        connect(bluetoothDeviceExtend.getBluetoothDevice(), true);
                        return true;
                    }
                });
    }

    public void read() {

    }

    public void read(ITimeoutCallback callback) {

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    public FunctionToken write(byte[] data) {
        return write(data, null);
    }

    public FunctionToken write(byte[] data, ITimeoutCallback callback) {
        return null;
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
