package com.vivek.wo.ble;

import android.os.Handler;

public abstract class ConnectToken extends Token {

    public ConnectToken() {
        super();
    }

    public ConnectToken(Handler handler) {
        super(handler);
    }

    public ConnectToken setTimeout(long timeout) {
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

    void callback(BluetoothException exception) {
        callbackRequest(exception);
    }

    @Override
    void onTimeout() {
        callbackTimeout("Connect");
    }
}
