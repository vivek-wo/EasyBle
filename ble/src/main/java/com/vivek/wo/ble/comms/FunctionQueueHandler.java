package com.vivek.wo.ble.comms;

import android.os.Handler;
import android.os.Looper;

public class FunctionQueueHandler<T> extends Handler implements IQueueHandler<T>, Runnable {

    public FunctionQueueHandler() {

    }

    public FunctionQueueHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void enqueue(T t) {

    }

    @Override
    public T dequeue() {
        return null;
    }

    @Override
    public void run() {

    }
}
