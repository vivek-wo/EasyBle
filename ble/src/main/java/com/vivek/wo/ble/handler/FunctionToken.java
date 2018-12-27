package com.vivek.wo.ble.handler;

public class FunctionToken<R> implements IToken<R> {
    IMethod mMethod;
    ICallback<R> mCallback;
    Object[] args;
    private volatile boolean isCompleted = false;
    private R response;

    public FunctionToken() {

    }

    public FunctionToken args(Object[] args) {
        this.args = args;
        return this;
    }

    public FunctionToken callback(ICallback<R> callback) {
        mCallback = callback;
        return this;
    }

    public FunctionToken method(IMethod method) {
        mMethod = method;
        return this;
    }

    @Override
    public void waitForCompletion() {

    }

    @Override
    public void waitForCompletion(int timeout) {

    }

    @Override
    public boolean isComplete() {
        return isCompleted;
    }

    @Override
    public R getResponse() {
        return response;
    }

    @Override
    public void invoke() {
        if (mMethod != null) {
            mMethod.onMethod(this.args);
        }
    }

    @Override
    public void notifyComplete(R r) {
        response = r;
        if (mCallback != null) {
            mCallback.onCallback(this, r);
        }
    }
}
