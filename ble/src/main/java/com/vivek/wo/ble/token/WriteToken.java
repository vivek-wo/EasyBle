package com.vivek.wo.ble.token;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;

import com.vivek.wo.ble.OnActionListener;
import com.vivek.wo.ble.internal.BluetoothException;

public abstract class WriteToken extends Token {
    public static final int WRITE_TYPE_DEFAULT = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    public static final int WRITE_TYPE_NO_RESPONSE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
    protected BluetoothGattCharacteristic characteristic;
    protected byte[] data;

    public WriteToken(BluetoothGattCharacteristic characteristic, byte[] data) {
        this(null, characteristic, data);
    }

    public WriteToken(Handler handler, BluetoothGattCharacteristic characteristic, byte[] data) {
        super(handler);
        this.characteristic = characteristic;
        this.data = data;
    }

    public WriteToken setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public WriteToken setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        return this;
    }

    /**
     * Set the write type for this characteristic
     *
     * <p>Setting the write type of a characteristic determines how the
     * {@link BluetoothGatt#writeCharacteristic} function write this
     * characteristic.
     *
     * @param writeType The write type to for this characteristic. Can be one
     *                  of:
     *                  {@link #WRITE_TYPE_DEFAULT},
     *                  {@link #WRITE_TYPE_NO_RESPONSE}
     */
    public WriteToken setWriteType(int writeType) {
        characteristic.setWriteType(writeType);
        return this;
    }

    public void write() {
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    public void write(byte[] data) {
        this.data = data;
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
        }
    }

    private void resetRequest() {

    }

    public void callback(BluetoothException exception, byte[] data, String characteristicUuid) {
        callbackRequest(exception, data, characteristicUuid);
    }

    @Override
    public void run() {
        callbackTimeout("Write");
    }
}
