package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.vivek.wo.ble.handler.FunctionToken;
import com.vivek.wo.ble.handler.ICallback;
import com.vivek.wo.ble.handler.IMethod;
import com.vivek.wo.ble.handler.IToken;

public class BluetoothComms extends GattComms {
    private IQueueHandler mQueueHandler;
    BluetoothDeviceExtend bluetoothDeviceExtend;

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
    }

    public void setQueueHandler(IQueueHandler queueHandler) {
        mQueueHandler = queueHandler;
    }

    public IToken connect() {
        return connect(null);
    }

    public IToken connect(ICallback<Boolean> callback) {
        return enqueue(callback, new IMethod() {
            @Override
            public Object onMethod(Object[] args) {
                connect(bluetoothDeviceExtend.getBluetoothDevice(), true);
                return null;
            }
        });
    }

    public void read() {

    }

    public void read(ICallback callback) {

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    public IToken write(byte[] data) {
        return write(data, null);
    }

    public IToken write(byte[] data, ICallback callback) {

        return enqueue(new Object[]{data}, callback, new IMethod() {
            @Override
            public Object onMethod(Object[] args) {
                write(null, (byte[]) args[0]);
                return null;
            }
        });
    }

    public void notify(boolean enable, boolean isIndication) {

    }

    public void notify(boolean enable, boolean isIndication, ICallback callback) {

    }

    public void rssi() {

    }

    public void rssi(ICallback callback) {

    }

    @Override
    public void disconnect() {

    }

    public void disconnect(ICallback callback) {

    }

    IToken enqueue(ICallback callback, IMethod method) {
        return enqueue(null, callback, method);
    }

    IToken enqueue(Object[] args, ICallback callback, IMethod method) {
        IToken token = new FunctionToken().args(args).callback(callback).method(method);
        return token;
    }
}
