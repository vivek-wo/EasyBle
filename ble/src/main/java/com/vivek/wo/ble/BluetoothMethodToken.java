package com.vivek.wo.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

abstract class BluetoothMethodToken implements MethodToken {
    private static final long METHODEXEC_DEFAULT_TIMEOUT = 5 * 1000;

    BluetoothGattService gattService;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattDescriptor descriptor;

    //    private BluetoothComms target;
    private String contextHandler;
    private OnActionListener onActionListener;
    private Object[] args;
    private long methodExecuteTimeout = METHODEXEC_DEFAULT_TIMEOUT;

    BluetoothMethodToken(String contextHandler) {
        this(contextHandler, null);
    }

    BluetoothMethodToken(String contextHandler, BluetoothComms target) {
        this.contextHandler = contextHandler;
//        this.target = target;
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
        this.methodExecuteTimeout = timeout;
        return this;
    }

    @Override
    public MethodToken parameterArgs(Object... args) {
        this.args = args;
        return this;
    }

    @Override
    public MethodToken invoke() {
        Object object = proxyMethod(this.args);
        return this;
    }

    @Override
    public MethodToken invokeInQueue() {
        return this;
    }

    public void callback(BluetoothException e, Object... args) {
        if (this.onActionListener != null) {
            if (e == null) {
                this.onActionListener.onSuccess(args);
            } else {
                this.onActionListener.onFailure(e);
            }
        }
    }

    static class QueueObject {
        private BluetoothMethodToken target;
        private Object[] args;

        QueueObject(BluetoothMethodToken target) {
            this.target = target;
        }

        public QueueObject setParamtersArgs(Object[] args) {
            this.args = args;
            return this;
        }
    }
}
