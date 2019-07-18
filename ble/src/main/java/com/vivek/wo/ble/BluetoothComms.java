package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.vivek.wo.ble.internal.GattComms;

import java.util.List;
import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    private BluetoothDeviceExtend bluetoothDeviceExtend;

    public BluetoothComms(Context context) {
        this(context, null);
    }

    public BluetoothComms(Context context, BluetoothDeviceExtend bluetoothDeviceExtend) {
        super(context);
        this.bluetoothDeviceExtend = bluetoothDeviceExtend;
    }

    public BluetoothDeviceExtend getBluetoothDeviceExtend() {
        return bluetoothDeviceExtend;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
//        if (mConnectState == ConnectStateEnum.STATE_DISCONNECTED) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
        //断开连接
//            } else {
        //连接失败
//            }
//        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
//        if (mConnectState == ConnectStateEnum.STATE_CONNECTED) {
        //连接并检索服务特征成功
//        } else {
        //连接检索服务特征失败
//        }
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

    public void connect() {
        connect(null);
    }

    public void connect(OnActionListener listener) {

        connect(bluetoothDeviceExtend.getBluetoothDevice(), false);
    }

    public void read(String serviceUUIDString, String characteristicUUIDString) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
    }

    public void write(String serviceUUIDString, String characteristicUUIDString,
                      byte[] data) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
    }

    public void notify(String serviceUUIDString, String characteristicUUIDString,
                       String descriptorUUIDString, boolean enable, boolean isIndication) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUUIDString);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUUIDString);
        BluetoothGattDescriptor descriptor = getBluetoothGattDescriptor(
                characteristic, descriptorUUIDString);
    }

    public void rssi() {
    }

    public void disconnect(OnActionListener listener) {
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
