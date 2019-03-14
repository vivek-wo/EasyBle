package com.vivek.wo.ble;

public interface OnActionListener extends OnTimeoutListener {

    void onSuccess(Object... args);

    void onFailure(Throwable throwable);
}
