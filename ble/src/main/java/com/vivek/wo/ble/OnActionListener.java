package com.vivek.wo.ble;

import com.vivek.wo.ble.internal.BluetoothException;

public interface OnActionListener {

    void onSuccess(Object... args);

    void onFailure(BluetoothException exception);
}
