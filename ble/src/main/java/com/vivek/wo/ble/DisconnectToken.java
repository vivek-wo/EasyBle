package com.vivek.wo.ble;

import android.os.Handler;

public abstract class DisconnectToken extends Token {

    public DisconnectToken() {
        super();
    }

    public DisconnectToken(Handler handler) {
        super(handler);
    }

    public DisconnectToken setTimeout(long timeout) {
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
    void onTimeout() {
        callbackTimeout("Disconnect");
    }
}
