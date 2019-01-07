package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Handler;

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

    public void connect() {
        connect(null);
    }

    public void connect(IConnectCallback callback) {
        QHandler h = new QHandler();
        h.dos();
        IToken token = new FunctionToken(mQueueHandler).callback(callback).method(new IMethod() {
            @Override
            public Object onMethod(Object[] args) {
                connect(bluetoothDeviceExtend.getBluetoothDevice(), true);
                return null;
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

    }

    public void disconnect(ITimeoutCallback callback) {

    }

    class QHandler extends Handler {
        QHandler() {

        }

        void dos() {
            System.out.println(" " + bluetoothDeviceExtend);
        }
    }
}
