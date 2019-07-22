package com.vivek.wo.ble.token;

import android.os.Handler;
import android.os.Looper;

import com.vivek.wo.ble.OnActionListener;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Token implements Runnable {
    private Handler mHandler;
    protected AtomicBoolean isTimeoutCallback = new AtomicBoolean(false);
    protected int timeout;
    protected OnActionListener onActionListener;

    public Token() {

    }

    public Token(Handler handler) {
        mHandler = handler;
    }

    /**
     * 执行蓝牙的请求操作
     *
     * @return 返回操作结果
     */
    protected abstract Object invoke();

    void addTimeoutTask() {
        if (timeout > 0) {
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.postDelayed(this, timeout);
        }
    }

    void removeTimeoutTask() {
        if (mHandler != null) {
            mHandler.removeCallbacks(this);
        }
    }
}
