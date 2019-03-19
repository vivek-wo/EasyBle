package com.vivek.wo.ble;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class MethodQueueHandler extends Handler {
    private List<MethodObject> methodObjectList = new ArrayList<>();
    private Object loackObject = new Object();
    private boolean handlerActive;
    private MethodObject currentMethodObject;

    public MethodQueueHandler() {
        this(Looper.getMainLooper());
    }

    public MethodQueueHandler(Looper looper) {
        super(looper);
    }

    public Object invoke(MethodObject methodObject) {
        synchronized (this) {
            methodObjectList.add(methodObject);
            if (!handlerActive) {
                handlerActive = true;
                sendMessage(obtainMessage());
            }
        }
        return null;
    }

    public void callback(int status, Object... args) {
        synchronized (this) {
            currentMethodObject.callbackArgs = args;
            loackObject.notifyAll();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            while (true) {
                currentMethodObject = methodObjectList.remove(0);
                if (currentMethodObject == null) {
                    synchronized (this) {
                        currentMethodObject = methodObjectList.remove(0);
                        if (methodObjectList == null) {
                            handlerActive = false;
                            return;
                        }
                    }
                }
                Object result = currentMethodObject.invoke();
                if (result instanceof Boolean) {
                    if (!((Boolean) result)) {
                        currentMethodObject.callback(BluetoothException.EXCEPTION_BLUETOOTH_EXECUTE_FAILURE,
                                new BluetoothException("Current MethodObject Execute Failure."));
                        continue;
                    }
                }
                synchronized (this) {
                    try {
                        loackObject.wait(currentMethodObject.timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentMethodObject.callback();
            }
        } finally {
            handlerActive = false;
        }
    }
}
