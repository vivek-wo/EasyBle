package com.vivek.wo.ble;

public class BluetoothException extends Exception {
    /**
     * 搜索超时
     */
    public static final int BLUETOOTH_SCAN_TIMEOUT = -5;

    /**
     * 方法执行超时
     */
    public static final int BLUETOOTH_FUNCTION_TIMEOUT = -1;
    /**
     * 方法执行失败
     */
    public static final int BLUETOOTH_EXECUTE_FAILURE = -2;

    /**
     * 无蓝牙设备
     */
    public static final int BLUETOOTH_REMOTEDEVICE_NOIFOUND = -10;

    /**
     * 远程设备无连接
     */
    public static final int BLUETOOTH_REMOTEDEVICE_NOICONNECTED = -11;

    private int reasonCode;

    public BluetoothException(int reasonCode) {
        super();
        this.reasonCode = reasonCode;
    }

    public BluetoothException(String detailMessage) {
        super(detailMessage);
    }

    public BluetoothException(int reasonCode, String detailMessage) {
        super(detailMessage);
        this.reasonCode = reasonCode;
    }

    public BluetoothException(Throwable throwable) {
        super(throwable);
    }

    public int getReasonCode() {
        return this.reasonCode;
    }
}
