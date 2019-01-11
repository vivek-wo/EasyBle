package com.vivek.wo.ble.comms;

public abstract class Token implements Runnable {
    private String tokenContext;
    private long timeoutMillis;
    private IMethod method;
    private ITimeoutCallback callback;
    private Object[] args;
    private Object invokedObject = null;
    //timeout or callback
    private volatile boolean isCompleted = false;

    public Token(String tokenContext) {
        this.tokenContext = tokenContext;
    }

    public Token timeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public Token waitForCompleted() {
        return this;
    }

    public Token execute() {
        return this;
    }

    public Token callback(ITimeoutCallback callback) {
        this.callback = callback;
        return this;
    }

    public Token args(Object... args) {
        this.args = args;
        return this;
    }

    public Token method(IMethod method) {
        this.method = method;
        return this;
    }

    public void invoke() {
        if (this.method != null) {
            invokedObject = this.method.onMethod(this.args);
        }
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public String getTokenContext() {
        return tokenContext;
    }

    public ITimeoutCallback getCallback() {
        return callback;
    }

    /**
     * 设置是否已经完成当前事件
     *
     * @param completed
     */
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    /**
     * 当前事件是否已经完成
     *
     * @return
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public void run() {
    }
}
