package com.vivek.wo.ble;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedList;

public class ObjectQueuePool extends Handler implements QueuePool<BluetoothMethodToken.QueueObject> {
    private LinkedList<BluetoothMethodToken.QueueObject> queue = new LinkedList<>();
    private boolean handleActive = false;

    public ObjectQueuePool() {
        super();
    }

    public ObjectQueuePool(Looper looper) {
        super(looper);
    }

    @Override
    public void add(BluetoothMethodToken.QueueObject object) {
        synchronized (this) {
            if (!handleActive) {
                queue.offer(object);
                handleActive = true;
                if (sendMessage(obtainMessage())) {
                    throw new BluetoothException(BluetoothException.BLUETOOTH_QUEUEPOOL_LOOPERROR,
                            "Bluetooth method queue loop failure!");
                }
            }
        }
    }

    @Override
    public BluetoothMethodToken.QueueObject remove() {
        return queue.poll();
    }

    @Override
    public void clear() {
        synchronized (this) {
            queue.clear();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        BluetoothMethodToken.QueueObject object = null;
        while (true) {
            synchronized (this) {
                object = remove();
                if (object == null) {
                    handleActive = false;
                    break;
                }
            }


        }
    }
}