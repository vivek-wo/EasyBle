package com.vivek.wo.ble.comms;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class FunctionUuidToken extends Token {
    private BluetoothComms mBluetoothComms;
    private String serviceUUID;
    private String characteristicUUID;
    private String descriptorUUID;

    public FunctionUuidToken(String tokenContext, BluetoothComms bluetoothComms) {
        this(null, null, tokenContext, bluetoothComms);
    }

    public FunctionUuidToken(String serviceUUID, String characteristicUUID,
                             String tokenContext, BluetoothComms bluetoothComms) {
        this(serviceUUID, characteristicUUID, null, tokenContext, bluetoothComms);
    }

    public FunctionUuidToken(String serviceUUID, String characteristicUUID, String descriptorUUID,
                             String tokenContext, BluetoothComms bluetoothComms) {
        super(tokenContext);
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.descriptorUUID = descriptorUUID;
        mBluetoothComms = bluetoothComms;
    }

    @Override
    public void run() {
        super.run();
        mBluetoothComms.onTimeoutCallback(this);
    }

    @Override
    public Token execute() {
        mBluetoothComms.execute(this);
        return super.execute();
    }

    @Override
    public void invoke() {
        initUUID();
        super.invoke();
    }

    void initUUID() {
        BluetoothGattService bluetoothGattService = null;
        if (this.serviceUUID != null) {
            bluetoothGattService = mBluetoothComms.getBluetoothGatt()
                    .getService(UUID.fromString(this.serviceUUID));
        }
        BluetoothGattCharacteristic characteristic = null;
        if (bluetoothGattService != null && this.characteristicUUID != null) {
            characteristic = bluetoothGattService.
                    getCharacteristic(UUID.fromString(this.characteristicUUID));
        }
        BluetoothGattDescriptor descriptor = null;
        if (characteristic != null && this.descriptorUUID != null) {
            descriptor = characteristic.
                    getDescriptor(UUID.fromString(this.descriptorUUID));
        }
        args(characteristic, descriptor);
    }

}
