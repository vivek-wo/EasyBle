package com.vivek.wo.ble;

import android.os.Handler;

public abstract class RssiToken extends Token {

    public RssiToken() {
        super();
    }

    public RssiToken(Handler handler) {
        super(handler);
    }

    public RssiToken setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RssiToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void readRssi() {
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void callback(BluetoothException exception, Integer rssi) {
        callbackRequest(exception, rssi);
    }

    @Override
    void onTimeout() {
        callbackTimeout("Rssi");
    }
}
