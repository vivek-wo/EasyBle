package com.vivek.wo.ble;

public interface OnActionListener {

    void onSuccess(Object... args);

    void onFailure(BluetoothException exception);
}
