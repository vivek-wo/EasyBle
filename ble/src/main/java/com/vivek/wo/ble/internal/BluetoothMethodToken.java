package com.vivek.wo.ble.internal;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public abstract class BluetoothMethodToken implements MethodToken {
    //    private MethodQueueHandler methodQueueHandler;

    private static final long METHODEXEC_DEFAULT_TIMEOUT = 5 * 1000;

    BluetoothGattService gattService;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattDescriptor descriptor;

    private BluetoothComms target;
    private OnActionListener onActionListener;
    private Object[] methodArgs;
    private long methodExecTimeout = METHODEXEC_DEFAULT_TIMEOUT;

    BluetoothMethodToken(BluetoothComms target) {
        this.target = target;
    }

//    BluetoothMethodToken setMethodQueueHandler(MethodQueueHandler handler) {
//        this.methodQueueHandler = handler;
//        return this;
//    }

    BluetoothMethodToken setGattService(BluetoothGattService gattService) {
        this.gattService = gattService;
        return this;
    }

    BluetoothMethodToken setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        return this;
    }

    BluetoothMethodToken setDescriptor(BluetoothGattDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    abstract Object proxyMethod(Object... args);

//    public void callback(int result, BluetoothException exception, Object... args) {
//        if (result == 0) {
//            this.listener.onSuccess(args);
//        } else {
//            this.listener.onFailure(exception);
//        }
//    }

    @Override
    public MethodToken listen(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    @Override
    public MethodToken timeout(long timeout) {
        this.methodExecTimeout = timeout;
        return this;
    }

    @Override
    public MethodToken parameterArgs(Object... args) {
        this.methodArgs = args;
        return this;
    }

    @Override
    public MethodToken invoke() {
        return this;
    }

    @Override
    public MethodToken invokeInQueue() {
        //        if (methodQueueHandler != null) {
//            MethodObject methodObject = new MethodObject(this, this.args, this.timeout);
//            methodQueueHandler.invoke(methodObject);
//        } else {
//            proxyInvoke(this.args);
//        }
        return this;
    }
}
