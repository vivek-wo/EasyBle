package com.vivek.wo.ble.token;

import android.os.Handler;

import com.vivek.wo.ble.OnActionListener;
import com.vivek.wo.ble.internal.BluetoothException;

public abstract class NotifyToken extends Token {

    public NotifyToken() {
        super();
    }

    public NotifyToken(Handler handler) {
        super(handler);
    }

    public NotifyToken setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public NotifyToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void notify(boolean enable) {
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
