package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.List;
import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    private BluetoothDeviceExtend bluetoothDeviceExtend;
    private BluetoothCommObserver bluetoothCommObserver;
    private MethodQueueHandler methodQueueHandler;

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        this(context, bluetoothDeviceExtend, null);
    }

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend, BluetoothCommObserver observer) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
        this.bluetoothCommObserver = observer;
    }

    public void setBluetoothCommObserver(BluetoothCommObserver bluetoothCommObserver) {
        this.bluetoothCommObserver = bluetoothCommObserver;
    }

    public void setMethodQueueHandler(MethodQueueHandler handler) {
        this.methodQueueHandler = handler;
    }

    public BluetoothDeviceExtend getBluetoothDeviceExtend() {
        return bluetoothDeviceExtend;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
    }

    public MethodProxy connect() {
        return connect(null);
    }

    public MethodProxy connect(OnActionListener listener) {
        return new MethodProxyImpl() {
            @Override
            public Object proxyInvoke(Object... args) {
                connect(bluetoothDeviceExtend.getBluetoothDevice(), false);
                return true;
            }
        }.setMethodQueueHandler(methodQueueHandler).listen(listener);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        byte[] data = null;
        if (status == BluetoothGatt.GATT_FAILURE) {
            //读回调
            data = characteristic.getValue();
        }
        methodQueueHandler.callback(status, data);
    }

    public MethodProxy read(String serviceUUID, String characteristicUUIDString) {
        return read(serviceUUID, characteristicUUIDString, null);
    }

    public MethodProxy read(String serviceUUIDString, String characteristicUUIDString,
                            OnActionListener listener) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        return new MethodProxyImpl() {
            @Override
            public Object proxyInvoke(Object... args) {
                return read(this.characteristic);
            }
        }
                .setMethodQueueHandler(methodQueueHandler)
                .setCharacteristic(characteristic)
                .listen(listener);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        //写回调
        methodQueueHandler.callback(status);
    }

    public MethodProxy write(String serviceUUIDString, String characteristicUUIDString, byte[] data) {
        return write(serviceUUIDString, characteristicUUIDString, data, null);
    }

    public MethodProxy write(String serviceUUIDString, String characteristicUUIDString, byte[] data,
                             OnActionListener listener) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        return new MethodProxyImpl() {
            @Override
            public Object proxyInvoke(Object... args) {
                return write(characteristic, (byte[]) args[0]);
            }
        }
                .setMethodQueueHandler(methodQueueHandler)
                .setCharacteristic(characteristic)
                .listen(listener)
                .parameterArgs(data);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        methodQueueHandler.callback(status);
    }

    public MethodProxy notify(String serviceUUIDString, String characteristicUUIDString,
                              String descriptorUUIDString, boolean enable, boolean isIndication) {
        return notify(serviceUUIDString, characteristicUUIDString, descriptorUUIDString, enable,
                isIndication, null);
    }

    public MethodProxy notify(String serviceUUIDString, String characteristicUUIDString,
                              String descriptorUUIDString, boolean enable, boolean isIndication,
                              OnActionListener listener) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        BluetoothGattDescriptor descriptor = getBluetoothGattDescriptor(
                characteristic, descriptorUUIDString);
        return new MethodProxyImpl() {
            @Override
            public Object proxyInvoke(Object... args) {
                return enable(characteristic, descriptor, (boolean) args[0], (boolean) args[1]);
            }
        }
                .setMethodQueueHandler(methodQueueHandler)
                .setCharacteristic(characteristic)
                .setDescriptor(descriptor)
                .listen(listener)
                .parameterArgs(enable, isIndication);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        methodQueueHandler.callback(status, rssi);
    }

    public MethodProxy rssi() {
        return rssi(null);
    }

    public MethodProxy rssi(OnActionListener listener) {
        return new MethodProxyImpl() {
            @Override
            public Object proxyInvoke(Object... args) {
                return readRemoteRssi();
            }
        }.listen(listener);
    }

    public MethodProxy disconnect(OnActionListener listener) {
        return new MethodProxyImpl() {
            @Override
            public Object proxyInvoke(Object... args) {
                disconnect();
                return true;
            }
        }.listen(listener);
    }

    public List<BluetoothGattService> getGattServiceList() {
        return getBluetoothGatt().getServices();
    }

    public List<BluetoothGattCharacteristic> getGattCharacteristicList(
            BluetoothGattService gattService) {
        return gattService.getCharacteristics();
    }

    public List<BluetoothGattDescriptor> getGattDescriptorList(
            BluetoothGattCharacteristic gattCharacteristic) {
        return gattCharacteristic.getDescriptors();
    }

    private BluetoothGattService getBluetoothGattService(String serviceUUIDString) {
        return getBluetoothGatt().getService(UUID.fromString(serviceUUIDString));
    }

    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(
            BluetoothGattService service, String characteristicUUIDString) {
        return service.getCharacteristic(UUID.fromString(characteristicUUIDString));
    }

    private BluetoothGattDescriptor getBluetoothGattDescriptor(
            BluetoothGattCharacteristic characteristic, String descriptorUUIDString) {
        return characteristic.getDescriptor(UUID.fromString(descriptorUUIDString));
    }

}
