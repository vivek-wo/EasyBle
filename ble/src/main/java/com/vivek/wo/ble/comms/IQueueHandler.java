package com.vivek.wo.ble.comms;

public interface IQueueHandler {

    void enqueue(IToken token);

    IToken get();

    void dequeue();

    boolean onTimeout(IToken token);
}
