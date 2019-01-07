package com.vivek.wo.ble.comms;

public class FunctionToken implements IToken {
    private IQueueHandler mQueueHandler;
    IMethod mMethod;
    ITimeoutCallback mCallback;
    Object[] args;
    private volatile boolean isCompleted = false;

    FunctionToken(IQueueHandler queueHandler) {
        mQueueHandler = queueHandler;
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
            mMethod.onMethod(this.args);
        }
    }

    @Override
    public void run() {

    }
}
