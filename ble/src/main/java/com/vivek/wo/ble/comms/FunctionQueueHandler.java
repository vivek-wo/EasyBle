package com.vivek.wo.ble.comms;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.vivek.wo.ble.PrintLog;

import java.util.LinkedList;

public class FunctionQueueHandler extends Handler implements Runnable {
    private static final String tag = "FunctionQueueHandler";
    private static FunctionQueueHandler defaultQueueHandler;
    private LinkedList<Token> mTokenQueueList = new LinkedList<>();
    private HandlerThread mTimeoutHandlerThread;
    private Handler mTimeoutHandler;
    private boolean handlerActive;
    private Token mCurrentToken;

    static FunctionQueueHandler getMainQueueHandler() {
        if (defaultQueueHandler == null) {
            synchronized (FunctionQueueHandler.class) {
                if (defaultQueueHandler == null) {
                    defaultQueueHandler = new FunctionQueueHandler(Looper.getMainLooper());
                }
            }
        }
        return defaultQueueHandler;
    }

    public FunctionQueueHandler(Looper looper) {
        super(looper);
        mTimeoutHandlerThread = new HandlerThread("handler-timeout");
        mTimeoutHandlerThread.start();
        mTimeoutHandler = new Handler(mTimeoutHandlerThread.getLooper());
    }

    private void postToken() {
        if (!handlerActive) {
            handlerActive = true;
            PrintLog.log(tag, "PostToken");
            boolean result = post(this);
            if (!result) {
                throw new IllegalStateException("can not post handler message");
            }
        }
    }

    public void enqueue(Token token) {
        PrintLog.log(tag, "Enqueue Token " + token + " , " + token.getTokenContext());
        synchronized (mTokenQueueList) {
            mTokenQueueList.add(token);
            postToken();
        }
        if (token.getTimeoutMillis() != -1) {
            mTimeoutHandler.postDelayed(token, token.getTimeoutMillis());
        }
    }

    public Token get() {
        PrintLog.log(tag, "Get Token " + mCurrentToken + " , isCompleted "
                + (mCurrentToken != null ? mCurrentToken.isCompleted() : true));
        return mCurrentToken;
    }

    public void remove(Token token) {
        synchronized (mTokenQueueList) {
            mTokenQueueList.remove(token);
        }
    }

    public void dequeue() {
        if (mCurrentToken == null) {
            PrintLog.log(tag, "Dequeue Token NULL");
            synchronized (mTokenQueueList) {
                postToken();
            }
            return;
        }
        PrintLog.log(tag, "Dequeue Token " + mCurrentToken + " , "
                + mCurrentToken.getTokenContext());
        synchronized (mTokenQueueList) {
            mTokenQueueList.remove();
            mCurrentToken = null;
            handlerActive = false;
            postToken();
        }
    }

    @Override
    public void run() {
        mCurrentToken = mTokenQueueList.peek();
        PrintLog.log(tag, "Run Token " + mCurrentToken + " , " + mTokenQueueList.size());
        if (mCurrentToken == null) {
            synchronized (this) {
                mCurrentToken = mTokenQueueList.peek();
                if (mCurrentToken == null) {
                    handlerActive = false;
                    return;
                }
            }
        }
        mCurrentToken.invoke();
        PrintLog.log(tag, "Run Token End " + mCurrentToken);
    }

}
