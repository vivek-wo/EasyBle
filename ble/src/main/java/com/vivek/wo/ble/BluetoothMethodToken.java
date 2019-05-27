package com.vivek.wo.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public abstract class BluetoothMethodToken implements MethodToken {
    private static final long METHODEXEC_DEFAULT_TIMEOUT = 5 * 1000;

    private String contextHandler;

    BluetoothGattService gattService;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattDescriptor descriptor;

    private BluetoothComms target;
    private OnActionListener onActionListener;
    private Object[] methodArgs;
    private long methodExecTimeout = METHODEXEC_DEFAULT_TIMEOUT;

    BluetoothMethodToken(String contextHandler, BluetoothComms target) {
        this.contextHandler = contextHandler;
        this.target = target;
    }

    String getContextHandler() {
        return this.contextHandler;
    }

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
        Object object = proxyMethod(this.methodArgs);
        return this;
    }

    @Override
    public MethodToken invokeInQueue() {
        return this;
    }
}
