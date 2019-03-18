package com.vivek.wo.ble;

public class Function {
    Object[] args;
    FunctionProxy target;
    int timeout;
    Object[] callbackArgs;
    boolean completed = false;

    Function(FunctionProxy target, Object[] args) {
        this(target, args, 0);
    }

    Function(FunctionProxy target, Object[] args, int timeout) {
        this.target = target;
        this.args = args;
        this.timeout = timeout;
    }

    Object invoke() {
        return this.target.invoke(args);
    }

    void callback() {
        BluetoothException exception = null;
        boolean result = completed;
        if (!completed) {
            exception = new BluetoothException(
                    BluetoothException.EXCEPTION_BLUETOOTH_FUNCTION_TIMEOUT);
        }
        this.target.callback(result, exception, args);
    }

    void callback(boolean result, BluetoothException exception, Object... args) {
        this.target.callback(result, exception, args);
    }
}
