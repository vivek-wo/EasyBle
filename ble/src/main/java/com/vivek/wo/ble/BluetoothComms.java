package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.vivek.wo.ble.internal.BluetoothException;
import com.vivek.wo.ble.internal.GattComms;
import com.vivek.wo.ble.token.ConnectToken;
import com.vivek.wo.ble.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothComms extends GattComms {
    private static final String TAG = "BluetoothComms";
    private BluetoothDeviceExtend bluetoothDeviceExtend;
    private Map<Class<? extends Token>, Token> mTokenMap = new HashMap<>();

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

    private void putBluetoothOperationToken(Class<? extends Token> cls, Token token) {

    }

    private Token removeBluetoothOperationToken(Class<? extends Token> cls) {
        Token token = null;
        return token;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (isDisconnected()) {
//        断开连接
            } else {
//        连接失败
                ConnectToken connectToken = (ConnectToken) mTokenMap.remove(ConnectToken.class);
                if (connectToken == null) {
                    return;
                }
                connectToken.callback(false, new BluetoothException(status,
                        "Connect callback failure! "));
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        ConnectToken connectToken = (ConnectToken) mTokenMap.remove(ConnectToken.class);
        if (connectToken == null) {
            return;
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
//        连接并检索服务特征成功
            connectToken.callback(true, null);
        } else {
//        连接检索服务特征失败
            connectToken.callback(false, new BluetoothException(status,
                    "Connect callback discovered services failure! "));
        }
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

    public ConnectToken createConnectToken() {
        ConnectToken connectToken = new ConnectToken() {
            @Override
            public Object invoke() {
                if (mTokenMap.containsKey(ConnectToken.class)) {
                    //已存在连接操作
                }
                putBluetoothOperationToken(ConnectToken.class, this);
                innerConnect(bluetoothDeviceExtend.getBluetoothDevice(), false);
                return null;
            }
        };
        return connectToken;
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
