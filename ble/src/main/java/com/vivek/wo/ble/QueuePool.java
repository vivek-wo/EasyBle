package com.vivek.wo.ble;

public interface QueuePool<T> {

    void add(T object);

    T remove();
}
