package com.vivek.wo.ble;

public class BluetoothException extends Exception {
    public static final int EXCEPTION_BLUETOOTH_FUNCTION_TIMEOUT = -1;
    public static final int EXCEPTION_BLUETOOTH_EXECUTE_FAILURE = -2;
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
}
