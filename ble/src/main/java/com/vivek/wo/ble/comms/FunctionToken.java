package com.vivek.wo.ble.comms;

public class FunctionToken implements IToken {
    private String methodContext;
    private BluetoothComms mBluetoothComms;
    IMethod mMethod;
    ITimeoutCallback mCallback;
    Object[] args;
    private Object invokedObject = null;
    private volatile boolean isCompleted = false;
    private long timeoutMillis = -1;
    private boolean isTimeout = false;
    private boolean isCallbacked = false;

    FunctionToken(String methodContext, BluetoothComms bluetoothComms) {
        this.methodContext = methodContext;
        mBluetoothComms = bluetoothComms;
    }

    FunctionToken args(Object[] args) {
        this.args = args;
        return this;
    }

    FunctionToken callback(ITimeoutCallback callback) {
        mCallback = callback;
        return this;
    }

    FunctionToken method(IMethod method) {
        mMethod = method;
        return this;
    }

    public boolean isComplete() {
        return isCompleted;
    }

    @Override
    public void invoke() {
        if (mMethod != null) {
            invokedObject = mMethod.onMethod(this.args);
        }
    }

    @Override
    public IToken timeout(long millis) {
        this.timeoutMillis = millis;
        return this;
    }

    @Override
    public IToken waitForCompleted() {
        return this;
    }

    @Override
    public long getTimeout() {
        return this.timeoutMillis;
    }

    @Override
    public boolean isTimeout() {
        return isTimeout;
    }

    @Override
    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }

    @Override
    public boolean isCallbacked() {
        return isCallbacked;
    }

    @Override
    public void setCallback(boolean callbacked) {
        this.isCallbacked = callbacked;
    }

    @Override
    public ITimeoutCallback getCallback() {
        return mCallback;
    }

    @Override
    public String getMethodContext() {
        return methodContext;
    }

    @Override
    public IToken execute(IQueueHandler queueHandler) {
        queueHandler.enqueue(this);
        return this;
    }

    @Override
    public IToken execute() {
        mBluetoothComms.getQueueHandler().enqueue(this);
        return this;
    }

    @Override
    public void run() {
        mBluetoothComms.onTimeoutCallback(this);
    }
}
