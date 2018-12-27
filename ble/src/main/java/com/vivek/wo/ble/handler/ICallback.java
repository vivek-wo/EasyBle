package com.vivek.wo.ble.handler;

public interface ICallback<R> {

    void onCallback(IToken<R> token, R r);
}
