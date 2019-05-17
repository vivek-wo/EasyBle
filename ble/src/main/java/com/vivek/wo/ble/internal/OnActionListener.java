package com.vivek.wo.ble.internal;

public interface OnActionListener {

    void onSuccess(Object... args);

    void onFailure(BluetoothException exception);
}
