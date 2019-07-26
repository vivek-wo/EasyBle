package com.vivek.wo.ble.token;

import android.os.Handler;

import com.vivek.wo.ble.OnActionListener;
import com.vivek.wo.ble.internal.BluetoothException;

public abstract class DisconnectToken extends Token {

    public DisconnectToken() {
        super();
    }

    public DisconnectToken(Handler handler) {
        super(handler);
    }

    public DisconnectToken setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public DisconnectToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void disconnect() {
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void callback(BluetoothException exception, Boolean isActiveDisconnect) {
        callbackRequest(exception, isActiveDisconnect);
    }

    @Override
    public void run() {
        callbackTimeout("Connect");
    }
}
