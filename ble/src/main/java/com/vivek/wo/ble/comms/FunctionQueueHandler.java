package com.vivek.wo.ble.comms;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class FunctionQueueHandler<T extends IToken> extends Handler implements IQueueHandler<T>, Runnable {
    private List<IToken> mFunctionQueueList = new ArrayList<>();
    private HandlerThread mTimeoutHandlerThread;
    private Handler mTimeoutHandler;
    private boolean handlerActive;
    private IToken mCurrentFunction;

    public FunctionQueueHandler() {
        mTimeoutHandlerThread = new HandlerThread("handler-timeout");
        mTimeoutHandler = new Handler(mTimeoutHandlerThread.getLooper());
        mTimeoutHandlerThread.start();
    }

    public FunctionQueueHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void enqueue(T t) {
        synchronized (mFunctionQueueList) {
            mFunctionQueueList.add(t);
            if (!handlerActive) {
                handlerActive = true;
                boolean result = post(this);
                if (!result) {
                    throw new IllegalStateException("can not post handler message");
                }
            }
        }
    }

    @Override
    public T get() {
        return (T) mCurrentFunction;
    }

    @Override
    public void dequeue() {

    }

    @Override
    public void run() {
        try {
//            while (true) {
            mCurrentFunction = mFunctionQueueList.remove(0);
            if (mCurrentFunction == null) {
                synchronized (this) {
                    mCurrentFunction = mFunctionQueueList.remove(0);
                    if (mCurrentFunction == null) {
                        handlerActive = false;
                        return;
                    }
                }
            }
            mCurrentFunction.invoke();
//            }
        } finally {
            handlerActive = false;
        }
    }
}
