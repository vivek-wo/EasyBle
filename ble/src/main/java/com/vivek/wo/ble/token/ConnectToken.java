package com.vivek.wo.ble.token;

import android.os.Handler;

import com.vivek.wo.ble.OnActionListener;
import com.vivek.wo.ble.internal.BluetoothException;

public abstract class ConnectToken extends Token {

    public ConnectToken() {
        super();
    }

    public ConnectToken(Handler handler) {
        super(handler);
    }

    public ConnectToken setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ConnectToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void connect() {
        addTimeoutTask();
        invoke();
    }

    public void callback(boolean isConnected, BluetoothException exception) {
        if (isTimeoutCallback.compareAndSet(false, true)) {
            removeTimeoutTask();
            if (this.onActionListener == null) {
                return;
            }
            if (isConnected) {
                this.onActionListener.onSuccess();
            } else {
                this.onActionListener.onFailure(exception);
            }
        }
    }

    @Override
    public void run() {
        //超时
        if (isTimeoutCallback.compareAndSet(false, true)) {
            if (this.onActionListener == null) {
                return;
            }
            this.onActionListener.onFailure(new BluetoothException(
                    BluetoothException.BLUETOOTH_CALLBACK_TIMEOUT, "Connect callback timeout! "));
        }
    }
}
