package com.vivek.wo.ble.comms;

public interface IQueueHandler<T> {

    void enqueue(T t);

    T dequeue();

}
