package com.vivek.wo.ble;

import android.os.Handler;

public abstract class NotifyToken extends Token {
    /**
     * 是否是指示器模式
     */
    protected boolean isIndication;

    protected boolean enable;

    public NotifyToken() {
        super();
    }

    public NotifyToken(Handler handler) {
        super(handler);
    }

    public NotifyToken setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public NotifyToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public NotifyToken setIndication(boolean indication) {
        this.isIndication = indication;
        return this;
    }

    public void notify(boolean enable) {
        this.enable = enable;
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void callback(BluetoothException exception, String characteristicUuid) {
        callbackRequest(exception, characteristicUuid);
    }

    @Override
    void onTimeout() {
        callbackTimeout("Notify");
    }
}
