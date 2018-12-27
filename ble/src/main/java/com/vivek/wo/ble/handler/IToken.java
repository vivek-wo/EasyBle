package com.vivek.wo.ble.handler;

/**
 * Created by VIVEK-WO on 2018/12/7.
 */

public interface IToken<R> {

    void waitForCompletion();

    void waitForCompletion(int timeout);

    boolean isComplete();

    R getResponse();

    void invoke();

    void notifyComplete(R r);
}


