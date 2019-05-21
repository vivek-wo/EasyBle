package com.vivek.wo.ble.internal;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public abstract class MethodProxyImpl implements MethodProxy {
    //    private MethodQueueHandler methodQueueHandler;
    BluetoothGattService gattService;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattDescriptor descriptor;
    private OnActionListener onActionListener;
    private Object[] methodArgs;
    private long methodExecTimeout;

    MethodProxyImpl() {

    }

//    MethodProxyImpl setMethodQueueHandler(MethodQueueHandler handler) {
//        this.methodQueueHandler = handler;
//        return this;
//    }

    MethodProxyImpl setGattService(BluetoothGattService gattService) {
        this.gattService = gattService;
        return this;
    }

    MethodProxyImpl setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        return this;
    }

    MethodProxyImpl setDescriptor(BluetoothGattDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

//    public void callback(int result, BluetoothException exception, Object... args) {
//        if (result == 0) {
//            this.listener.onSuccess(args);
//        } else {
//            this.listener.onFailure(exception);
//        }
//    }

    @Override
    public MethodProxy listen(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    @Override
    public MethodProxy timeout(long timeout) {
        this.methodExecTimeout = timeout;
        return this;
    }

    @Override
    public MethodProxy parameterArgs(Object... args) {
        this.methodArgs = args;
        return this;
    }

    abstract Object proxyInvoke(Object... args);

    @Override
    public MethodProxy invoke() {
//        if (methodQueueHandler != null) {
//            MethodObject methodObject = new MethodObject(this, this.args, this.timeout);
//            methodQueueHandler.invoke(methodObject);
//        } else {
//            proxyInvoke(this.args);
//        }
        return this;
    }

    @Override
    public MethodProxy invokeInQueue() {
        return null;
    }
}
