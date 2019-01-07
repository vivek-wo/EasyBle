package com.vivek.wo.ble.comms;

public interface IQueueHandler<T extends IToken> {

    void enqueue(T t);

    T get();

    void dequeue();

}
