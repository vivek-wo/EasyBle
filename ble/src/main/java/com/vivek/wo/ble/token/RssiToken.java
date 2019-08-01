package com.vivek.wo.ble.token;

import android.os.Handler;

import com.vivek.wo.ble.OnActionListener;
import com.vivek.wo.ble.internal.BluetoothException;

public abstract class RssiToken extends Token {

    public RssiToken() {
        super();
    }

    public RssiToken(Handler handler) {
        super(handler);
    }

    public RssiToken setTimeout(int timeout) {
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
    public void run() {
        callbackTimeout("Rssi");
    }
}
