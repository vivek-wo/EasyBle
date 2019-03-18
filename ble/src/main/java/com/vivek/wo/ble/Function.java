package com.vivek.wo.ble;

public class Function {
    Object[] args;
    FunctionProxy target;
    int timeout;
    Object[] callbackArgs;

    Function(FunctionProxy target, Object[] args) {
        this(target, args, 0);
    }

    Function(FunctionProxy target, Object[] args, int timeout) {
        this.target = target;
        this.args = args;
        this.timeout = timeout;
    }

    void invoke() {
        this.target.invoke(args);
    }

    void callback() {
        this.target.callback(callbackArgs);
    }
}
