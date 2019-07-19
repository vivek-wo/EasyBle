package com.vivek.wo.ble.token;

import com.vivek.wo.ble.OnActionListener;

public abstract class ConnectToken extends Token {

    public ConnectToken() {
    }

    public ConnectToken setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ConnectToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    public void connect() {

        invoke();
    }
}
