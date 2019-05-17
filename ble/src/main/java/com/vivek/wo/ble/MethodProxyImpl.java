package com.vivek.wo.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.vivek.wo.ble.internal.BluetoothException;
import com.vivek.wo.ble.internal.OnActionListener;

public abstract class MethodProxyImpl implements MethodProxy {
    private MethodQueueHandler methodQueueHandler;
    BluetoothGattService gattService;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattDescriptor descriptor;
    private OnActionListener listener;
    private Object[] args;
    private long timeout;

    MethodProxyImpl() {

    }

    MethodProxyImpl setMethodQueueHandler(MethodQueueHandler handler) {
        this.methodQueueHandler = handler;
        return this;
    }

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

    @Override
    public void callback(int result, BluetoothException exception, Object... args) {
        if (result == 0) {
            this.listener.onSuccess(args);
        } else {
            this.listener.onFailure(exception);
        }
    }

    @Override
    public MethodProxy listen(OnActionListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public MethodProxy timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public MethodProxy parameterArgs(Object... args) {
        this.args = args;
        return this;
    }

    protected abstract Object proxyInvoke(Object... args);

    @Override
    public MethodProxy invoke() {
        if (methodQueueHandler != null) {
            MethodObject methodObject = new MethodObject(this, this.args, this.timeout);
            methodQueueHandler.invoke(methodObject);
        } else {
            proxyInvoke(this.args);
        }
        return this;
    }
}
