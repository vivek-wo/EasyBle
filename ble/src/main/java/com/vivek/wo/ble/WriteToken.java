package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;

public abstract class WriteToken extends Token {
    public static final int WRITE_TYPE_DEFAULT = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    public static final int WRITE_TYPE_NO_RESPONSE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
    protected BluetoothGattCharacteristic characteristic;
    protected byte[] data;

    public WriteToken(BluetoothGattCharacteristic characteristic) {
        this(null, characteristic);
    }

    public WriteToken(Handler handler, BluetoothGattCharacteristic characteristic) {
        super(handler);
        this.characteristic = characteristic;
    }

    public WriteToken setTimeout(long timeout) {
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
        if (writeType != WRITE_TYPE_DEFAULT || writeType != WRITE_TYPE_NO_RESPONSE) {
            throw new IllegalArgumentException("Argument writeType not supported. ");
        }
        characteristic.setWriteType(writeType);
        return this;
    }

    @Override
    protected boolean onRequestPrepared() {
        if (isWriteTypeNoResponse()) {
            return true;
        }
        return super.onRequestPrepared();
    }

    private boolean isWriteTypeNoResponse() {
        return this.characteristic.getWriteType() == WRITE_TYPE_NO_RESPONSE;
    }

    public void write(byte[] data) {
        this.data = data;
        boolean isPrepared = onRequestPrepared();
        if (isPrepared) {
            invoke();
            if (isWriteTypeNoResponse()) {
                onRequestFinished(false);
            }
        }
    }

    @Override
    protected void onRequestFinished(boolean isTimeout) {
        if (isWriteTypeNoResponse()) {
            return;
        }
        super.onRequestFinished(isTimeout);
    }

    public void callback(BluetoothException exception, byte[] data, String characteristicUuid) {
        callbackRequest(exception, data, characteristicUuid);
    }

    @Override
    public void run() {
        callbackTimeout("Write");
    }
}
