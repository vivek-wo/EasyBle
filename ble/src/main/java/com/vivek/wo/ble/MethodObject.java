package com.vivek.wo.ble;

public class MethodObject {
    Object[] args;
    MethodProxy target;
    int timeout;
    int callbackCompleted = -1;//超时
    Object[] callbackArgs;

    MethodObject(MethodProxy target, Object[] args) {
        this(target, args, 0);
    }

    MethodObject(MethodProxy target, Object[] args, int timeout) {
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
                    BluetoothException.EXCEPTION_BLUETOOTH_FUNCTION_TIMEOUT);
        }
        this.target.callback(callbackCompleted, exception, args);
    }

    void callback(int result, BluetoothException exception, Object... args) {
        this.target.callback(result, exception, args);
    }
}
