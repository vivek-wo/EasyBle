package com.vivek.wo.ble;

import com.vivek.wo.ble.internal.BluetoothException;
import com.vivek.wo.ble.internal.MethodToken;

public class MethodObject {
    static final long DEFAULT_TIMEOUT = 15 * 1000;
    Object[] args;
    MethodToken target;
    long timeout = DEFAULT_TIMEOUT;//默认15秒超时
    int callbackCompleted = -1;//超时
    Object[] callbackArgs;

    MethodObject(MethodToken target, Object[] args) {
        this(target, args, DEFAULT_TIMEOUT);
    }

    MethodObject(MethodToken target, Object[] args, long timeout) {
        this.target = target;
        this.args = args;
        this.timeout = timeout;
    }

    Object invoke() {
        return this.target.proxyInvoke(args);
    }

    void callback() {
        BluetoothException exception = null;
        if (callbackCompleted == -1) {
            exception = new BluetoothException(
                    BluetoothException.BLUETOOTH_FUNCTION_TIMEOUT);
        }
        this.target.callback(callbackCompleted, exception, args);
    }

    void callback(int result, BluetoothException exception, Object... args) {
        this.target.callback(result, exception, args);
    }
}
