package com.vivek.wo.ble;

import android.os.Handler;

public abstract class ReadToken extends Token {

    public ReadToken() {
        super();
    }

    public ReadToken(Handler handler) {
        super(handler);
    }

    public ReadToken setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public ReadToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void read() {
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void callback(BluetoothException exception, byte[] data, String characteristicUuid) {
        callbackRequest(exception, data, characteristicUuid);
    }

    @Override
    public void run() {
        callbackTimeout("Read");
    }
}
