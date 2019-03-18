package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    BluetoothDeviceExtend bluetoothDeviceExtend;
    BluetoothCommObserver bluetoothCommObserver;

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        this(context, bluetoothDeviceExtend, null);
    }

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend, BluetoothCommObserver observer) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
        this.bluetoothCommObserver = observer;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
    }

    public FunctionProxy connect() {
        return connect(null);
    }

    public FunctionProxy connect(OnActionListener listener) {
        return new FunctionProxyImpl() {
            @Override
            public Object invoke(Object... args) {
                connect(bluetoothDeviceExtend.getBluetoothDevice(), false);
                return true;
            }
        }.listen(listener);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        //读回调
    }

    public FunctionProxy read(String serviceUUID, String characteristicUUIDString) {
        return read(serviceUUID, characteristicUUIDString, null);
    }

    public FunctionProxy read(String serviceUUIDString, String characteristicUUIDString,
                              OnActionListener listener) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        return new FunctionProxyImpl(gattService, characteristic, null) {
            @Override
            public Object invoke(Object... args) {
                return read(this.characteristic);
            }
        }.listen(listener);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        //写回调
    }

    public FunctionProxy write(String serviceUUIDString, String characteristicUUIDString, byte[] data) {
        return write(serviceUUIDString, characteristicUUIDString, data, null);
    }

    public FunctionProxy write(String serviceUUIDString, String characteristicUUIDString, byte[] data,
                               OnActionListener listener) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        return new FunctionProxyImpl(gattService, characteristic, null) {
            @Override
            public Object invoke(Object... args) {
                return null;
            }
        }.listen(listener);
    }

    public FunctionProxy notify(String serviceUUIDString, String characteristicUUIDString,
                                String descriptorUUIDString, boolean enable, boolean isIndication) {
        return notify(serviceUUIDString, characteristicUUIDString, descriptorUUIDString, enable,
                isIndication, null);
    }

    public FunctionProxy notify(String serviceUUIDString, String characteristicUUIDString,
                                String descriptorUUIDString, boolean enable, boolean isIndication,
                                OnActionListener listener) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        BluetoothGattDescriptor descriptor = getBluetoothGattDescriptor(
                characteristic, descriptorUUIDString);
        return null;
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    public FunctionProxy rssi() {
        return rssi(null);
    }

    public FunctionProxy rssi(OnActionListener listener) {
        return null;
    }

    public FunctionProxy disconnect(OnActionListener listener) {
        return null;
    }

    BluetoothGattService getBluetoothGattService(String serviceUUIDString) {
        return getBluetoothGatt().getService(UUID.fromString(serviceUUIDString));
    }

    BluetoothGattCharacteristic getBluetoothGattCharacteristic(BluetoothGattService service,
                                                               String characteristicUUIDString) {
        return service.getCharacteristic(UUID.fromString(characteristicUUIDString));
    }

    BluetoothGattDescriptor getBluetoothGattDescriptor(BluetoothGattCharacteristic characteristic,
                                                       String descriptorUUIDString) {
        return characteristic.getDescriptor(UUID.fromString(descriptorUUIDString));
    }

}
