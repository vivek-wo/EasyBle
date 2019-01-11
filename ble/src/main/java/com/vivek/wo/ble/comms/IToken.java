package com.vivek.wo.ble.comms;

public interface IToken extends Runnable {

    void invoke();

    IToken timeout(long millis);

    IToken waitForCompleted();

    IToken execute(IQueueHandler queueHandler);

    IToken execute();

    long getTimeout();

    boolean isTimeout();

    void setTimeout(boolean timeout);

    boolean isCallbacked();

    void setCallback(boolean callbacked);

    ITimeoutCallback getCallback();

    String getMethodContext();
}
