package com.vivek.wo.ble.comms;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.vivek.wo.ble.PrintLog;

import java.util.LinkedList;

public class FunctionQueueHandler extends Handler implements IQueueHandler, Runnable {
    private static final String tag = "FunctionQueueHandler";
    private static IQueueHandler defaultQueueHandler;
    private LinkedList<IToken> mTokenQueueList = new LinkedList<>();
    private HandlerThread mTimeoutHandlerThread;
    private Handler mTimeoutHandler;
    private boolean handlerActive;
    private IToken mCurrentToken;

    static IQueueHandler getMainQueueHandler() {
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

    void postToken() {
        if (!handlerActive) {
            handlerActive = true;
            PrintLog.log(tag, "postToken token ");
            boolean result = post(this);
            if (!result) {
                throw new IllegalStateException("can not post handler message");
            }
        }
    }

    @Override
    public void enqueue(IToken token) {
        PrintLog.log(tag, "enqueue token " + token + " , " + token.getMethodContext());
        synchronized (mTokenQueueList) {
            mTokenQueueList.add(token);
            PrintLog.log(tag, "enqueue sync token " + token + " , handlerActive " + handlerActive);
            postToken();
        }
        if (token.getTimeout() != -1) {
            //执行倒计时
            mTimeoutHandler.postDelayed(token, token.getTimeout());
        }
    }

    @Override
    public synchronized boolean onTimeout(IToken token) {
        PrintLog.log(tag, "onTimeout token " + token + " , isCallbacked " + token.isCallbacked());
        if (token.isCallbacked()) {
            return false;
        }
        boolean result = false;
        //超时
        synchronized (mTokenQueueList) {
            if (token == mCurrentToken) {
                result = true;
            } else if (mTokenQueueList.contains(token)) {
                mTokenQueueList.remove(token);
                result = true;
            }
        }
        if (result) {
            token.setTimeout(result);
            ITimeoutCallback callback = token.getCallback();
            if (callback != null) {
                callback.onTimeout();
            }
            dequeue();
        }
        PrintLog.log(tag, "onTimeout token " + token + " , " + result + " , " + token.getMethodContext());
        return result;
    }

    @Override
    public synchronized IToken get() {
        PrintLog.log(tag, "onCallback token " + mCurrentToken + " , isTimeout "
                + (mCurrentToken != null ? mCurrentToken.isTimeout() : true));
        if (mCurrentToken == null || mCurrentToken.isTimeout()) {
            return null;
        }
        mCurrentToken.setCallback(true);
        return mCurrentToken;
    }

    @Override
    public void dequeue() {
        if (mCurrentToken == null) {
            return;
        }
        //执行完成
        PrintLog.log(tag, "dequeue token " + mCurrentToken + " , " + mCurrentToken.isTimeout()
                + " , " + mCurrentToken.getMethodContext());
        if (!mCurrentToken.isTimeout()) {
            mTimeoutHandler.removeCallbacks(mCurrentToken);
        }
        synchronized (mTokenQueueList) {
            mTokenQueueList.remove();
            mCurrentToken = null;
            handlerActive = false;
            postToken();
        }
        PrintLog.log(tag, "dequeue end token " + mCurrentToken);
    }

    @Override
    public void run() {
        mCurrentToken = mTokenQueueList.peek();
        PrintLog.log(tag, "run token " + mCurrentToken + " , " + mTokenQueueList.size());
        if (mCurrentToken == null) {
            synchronized (this) {
                mCurrentToken = mTokenQueueList.peek();
                PrintLog.log(tag, "enqueue sync token " + mCurrentToken + " , " + mTokenQueueList.size());
                if (mCurrentToken == null) {
                    handlerActive = false;
                    return;
                }
            }
        }
        mCurrentToken.invoke();
        PrintLog.log(tag, "run end token " + mCurrentToken);
    }

}
