package com.vivek.wo.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public abstract class FunctionProxyImpl implements FunctionProxy {
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattDescriptor descriptor;
    private OnActionListener listener;
    private int timeout;

    FunctionProxyImpl() {

    }

    public FunctionProxyImpl(BluetoothGattService gattService,
                             BluetoothGattCharacteristic characteristic,
                             BluetoothGattDescriptor descriptor) {
        this.gattService = gattService;
        this.characteristic = characteristic;
        this.descriptor = descriptor;
    }

    @Override
    public void callback(Object... args) {

    }

    @Override
    public FunctionProxy listen(OnActionListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public FunctionProxy timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
}
