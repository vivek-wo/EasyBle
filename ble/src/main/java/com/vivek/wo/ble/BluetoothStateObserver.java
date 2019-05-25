package com.vivek.wo.ble;

public interface BluetoothStateObserver {

    /**
     * 蓝牙状态改变监听
     *
     * <p>
     * {@link android.bluetooth.BluetoothAdapter#STATE_OFF},
     * {@link android.bluetooth.BluetoothAdapter#STATE_ON},
     * {@link android.bluetooth.BluetoothAdapter#STATE_TURNING_OFF},
     * {@link android.bluetooth.BluetoothAdapter#STATE_TURNING_ON}
     * </P>
     *
     * @param state
     */
    void onStateChanged(int state);
}
