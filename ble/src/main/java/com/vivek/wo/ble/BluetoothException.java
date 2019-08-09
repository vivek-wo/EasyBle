package com.vivek.wo.ble;

public class BluetoothException extends RuntimeException {
    public static final int BLUETOOTH_CALLBACK_TIMEOUT = -1000;//蓝牙请求响应超时

//    public static final int BLUETOOTH_REMOTEDEVICE_NOIFOUND = -1;//远程设备无法找到
//    public static final int BLUETOOTH_REMOTEDEVICE_NOICONNECTED = -2;//远程设备未连接

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
