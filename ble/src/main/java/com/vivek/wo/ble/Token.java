package com.vivek.wo.ble;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Token {
    private Handler mHandler;
    protected AtomicBoolean isTimeoutCallback = new AtomicBoolean(false);
    protected long timeout;
    protected OnActionListener onActionListener;

    public Token() {
        this(null);
    }

    public Token(Handler handler) {
        mHandler = handler;
    }

    /**
     * 蓝牙请求前方法
     *
     * @return
     */
    protected boolean onRequestPrepared() {
        addTimeoutTask();
        return true;
    }

    /**
     * 执行蓝牙的请求操作
     *
     * @return 返回操作结果
     */
    protected abstract Object invoke();

    /**
     * 蓝牙请求完成方法
     *
     * @param isTimeout
     */
    protected void onRequestFinished(boolean isTimeout) {
        if (!isTimeout) {
            removeTimeoutTask();
        }
    }

    /**
     * 是否第一次进入超时或者回调
     *
     * @return
     */
    protected boolean isFirstTimeoutOrCallback() {
        return isTimeoutCallback.compareAndSet(false, true);
    }

    /**
     * 添加超时任务
     */
    void addTimeoutTask() {
        if (timeout > 0) {
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.postDelayed(mTimeoutRunnable, timeout);
        }
    }

    /**
     * 移除超时任务
     */
    void removeTimeoutTask() {
        if (timeout > 0 && mHandler != null) {
            mHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    protected Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            onTimeout();
        }
    };

    abstract void onTimeout();

    /**
     * 蓝牙请求回调
     *
     * @param exception
     * @param args
     */
    protected void callbackRequest(BluetoothException exception, Object... args) {
        //回调
        if (isFirstTimeoutOrCallback()) {
            onRequestFinished(false);
            if (this.onActionListener == null) {
                return;
            }
            if (exception == null) {
                this.onActionListener.onSuccess(args);
            } else {
                this.onActionListener.onFailure(exception);
            }
        }
    }

    /**
     * 蓝牙请求超时
     *
     * @param requestTag
     */
    protected void callbackTimeout(String requestTag) {
        //超时
        if (isFirstTimeoutOrCallback()) {
            onRequestFinished(true);
            if (this.onActionListener == null) {
                return;
            }
            this.onActionListener.onFailure(new BluetoothException(
                    BluetoothException.BLUETOOTH_CALLBACK_TIMEOUT, requestTag + "callback timeout! "));
        }
    }
}
