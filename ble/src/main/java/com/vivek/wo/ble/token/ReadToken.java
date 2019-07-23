package com.vivek.wo.ble.token;

import android.os.Handler;

import com.vivek.wo.ble.OnActionListener;
import com.vivek.wo.ble.internal.BluetoothException;

public abstract class ReadToken extends Token {

    public ReadToken() {
        super();
    }

    public ReadToken(Handler handler) {
        super(handler);
    }

    public ReadToken setTimeout(int timeout) {
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
