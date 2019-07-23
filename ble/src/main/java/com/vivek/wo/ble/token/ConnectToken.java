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
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void callback(BluetoothException exception) {
        callbackRequest(exception);
    }

    @Override
    public void run() {
        callbackTimeout("Connect");
    }
}
