package com.vivek.wo.ble;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class FunctionHandler extends Handler {
    private List<Function> proxyFunctionList = new ArrayList<>();
    private Object loackObject = new Object();
    private boolean handlerActive;
    private Function currentFunction;

    public FunctionHandler() {
        this(Looper.getMainLooper());
    }

    public FunctionHandler(Looper looper) {
        super(looper);
    }

    public Object invoke(Function function) {
        synchronized (this) {
            proxyFunctionList.add(function);
            if (!handlerActive) {
                handlerActive = true;
                sendMessage(obtainMessage());
            }
        }
        return null;
    }

    public void callback(Object... args) {
        synchronized (this) {
            currentFunction.callbackArgs = args;
            loackObject.notifyAll();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            while (true) {
                currentFunction = proxyFunctionList.remove(0);
                if (currentFunction == null) {
                    synchronized (this) {
                        currentFunction = proxyFunctionList.remove(0);
                        if (proxyFunctionList == null) {
                            handlerActive = false;
                            return;
                        }
                    }
                }
                currentFunction.invoke();
                synchronized (this) {
                    try {
                        loackObject.wait(currentFunction.timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentFunction.callback();
            }
        } finally {
            handlerActive = false;
        }
    }
}
