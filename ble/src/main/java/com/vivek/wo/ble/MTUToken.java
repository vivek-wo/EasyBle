package com.vivek.wo.ble;

import android.os.Handler;

public abstract class MTUToken extends Token {

    protected int mtu;

    public MTUToken() {
        super();
    }

    public MTUToken(Handler handler) {
        super(handler);
    }

    public MTUToken setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public MTUToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void setMTU(int mtu) {
        this.mtu = mtu;
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void callback(BluetoothException exception, Integer mtu) {
        callbackRequest(exception, mtu);
    }

    @Override
    public void run() {
        callbackTimeout("MTU");
    }
}
