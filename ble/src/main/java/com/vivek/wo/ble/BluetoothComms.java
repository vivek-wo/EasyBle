package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    private BluetoothDeviceExtend bluetoothDeviceExtend;
    private BluetoothCommObserver bluetoothCommObserver;
    private Map<String, BluetoothMethodToken> invokedMethodTokenMaps =
            new LinkedHashMap<>(4);

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        this(context, bluetoothDeviceExtend, null);
    }

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend,
                          BluetoothCommObserver observer) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
        this.bluetoothCommObserver = observer;
    }

    public void setBluetoothCommObserver(BluetoothCommObserver bluetoothCommObserver) {
        this.bluetoothCommObserver = bluetoothCommObserver;
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

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        byte[] data = null;
        if (status == BluetoothGatt.GATT_FAILURE) {
            //读回调
            data = characteristic.getValue();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        //写回调
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    public MethodToken connect() {
        return new BluetoothMethodToken("connect-token", this) {
            @Override
            Object proxyMethod(Object... args) {
                invokedMethodTokenMaps.put(getContextHandler(), this);
                connect(bluetoothDeviceExtend.getBluetoothDevice(), false);
                return true;
            }
        };
    }

    public MethodToken read(String serviceUUIDString, String characteristicUUIDString) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        return new BluetoothMethodToken("read-token", this) {
            @Override
            Object proxyMethod(Object... args) {
                return read(this.characteristic);
            }
        }.setCharacteristic(characteristic);
    }

    public MethodToken write(String serviceUUIDString, String characteristicUUIDString,
                             byte[] data) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        return new BluetoothMethodToken("write-token", this) {
            @Override
            Object proxyMethod(Object... args) {
                return write(this.characteristic, (byte[]) args[0]);
            }
        }.setCharacteristic(characteristic).parameterArgs(data);
    }

    public MethodToken notify(String serviceUUIDString, String characteristicUUIDString,
                              String descriptorUUIDString, boolean enable, boolean isIndication) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        BluetoothGattDescriptor descriptor = getBluetoothGattDescriptor(
                characteristic, descriptorUUIDString);
        return new BluetoothMethodToken("notify-token", this) {
            @Override
            Object proxyMethod(Object... args) {
                return enable(this.characteristic, this.descriptor,
                        (boolean) args[0], (boolean) args[1]);
            }
        }.setCharacteristic(characteristic).setDescriptor(descriptor).parameterArgs(enable, isIndication);
    }

    public MethodToken rssi() {
        return new BluetoothMethodToken("rssi-token", this) {
            @Override
            Object proxyMethod(Object... args) {
                return readRemoteRssi();
            }
        };
    }

    public MethodToken disconnect(OnActionListener listener) {
        return new BluetoothMethodToken("disconnect-token", this) {
            @Override
            Object proxyMethod(Object... args) {
                disconnect();
                return true;
            }
        };
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
