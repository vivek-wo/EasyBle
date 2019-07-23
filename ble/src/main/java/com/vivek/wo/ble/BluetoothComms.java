package com.vivek.wo.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.vivek.wo.ble.internal.BluetoothException;
import com.vivek.wo.ble.internal.GattComms;
import com.vivek.wo.ble.token.ConnectToken;
import com.vivek.wo.ble.token.ReadToken;
import com.vivek.wo.ble.token.Token;
import com.vivek.wo.ble.token.WriteToken;

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
        mTokenMap.put(cls, token);
    }

    private Token removeBluetoothOperationToken(Class<? extends Token> cls) {
        Token token;
        token = mTokenMap.remove(cls);
        return token;
    }

    private boolean isExistedBluetoothOperationToken(Class<? extends Token> cls) {
        return mTokenMap.containsKey(cls);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            if (isDisconnected()) {
//        断开连接
            } else {
//        连接失败
                ConnectToken connectToken = (ConnectToken) removeBluetoothOperationToken(
                        ConnectToken.class);
                if (connectToken == null) {
                    return;
                }
                connectToken.callback(new BluetoothException(status,
                        "Connect callback failure! "));
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        ConnectToken connectToken = (ConnectToken) removeBluetoothOperationToken(ConnectToken.class);
        if (connectToken == null) {
            return;
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
//        连接并检索服务特征成功
            connectToken.callback(null);
        } else {
//        连接检索服务特征失败
            connectToken.callback(new BluetoothException(status,
                    "Connect callback discovered services failure! "));
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        byte[] data = null;
        ReadToken readToken = (ReadToken) removeBluetoothOperationToken(ReadToken.class);
        if (readToken == null) {
            return;
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            //读回调
            data = characteristic.getValue();
            readToken.callback(null, data, characteristic.getUuid().toString());
        } else {
            readToken.callback(new BluetoothException(status, "Read callback failure! "),
                    null, null);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        //写回调
        byte[] data = null;
        WriteToken writeToken = (WriteToken) removeBluetoothOperationToken(WriteToken.class);
        if (writeToken == null) {
            return;
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            //读回调
            data = characteristic.getValue();
            writeToken.callback(null, data, characteristic.getUuid().toString());
        } else {
            writeToken.callback(new BluetoothException(status, "Write callback failure! "),
                    null, null);
        }
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
            protected boolean onRequestPrepared() {
                if (isExistedBluetoothOperationToken(ConnectToken.class)) {
                    //已存在连接操作
                    return false;
                }
                putBluetoothOperationToken(ConnectToken.class, this);
                return super.onRequestPrepared();
            }

            @Override
            public Object invoke() {
                innerConnect(bluetoothDeviceExtend.getBluetoothDevice(), false);
                return null;
            }

            @Override
            protected void onRequestFinished(boolean isTimeout) {
                super.onRequestFinished(isTimeout);
                if (isTimeout) {
                    innerDisconnect();
                }
                removeBluetoothOperationToken(ConnectToken.class);
            }
        };
        return connectToken;
    }

    public ReadToken createReadToken(String serviceUuid, String characteristicUuid) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUuid);
        final BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUuid);
        ReadToken readToken = new ReadToken() {
            @Override
            protected boolean onRequestPrepared() {
                if (isExistedBluetoothOperationToken(ReadToken.class)) {
                    return false;
                }
                putBluetoothOperationToken(ReadToken.class, this);
                return super.onRequestPrepared();
            }

            @Override
            protected Object invoke() {
                return innerRead(characteristic);
            }

            @Override
            protected void onRequestFinished(boolean isTimeout) {
                super.onRequestFinished(isTimeout);
                removeBluetoothOperationToken(ConnectToken.class);
            }
        };
        return readToken;
    }

    public WriteToken createWriteToken(String serviceUuid, String characteristicUuid,
                                       byte[] data) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUuid);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUuid);
        WriteToken writeToken = new WriteToken(characteristic, data) {
            @Override
            protected boolean onRequestPrepared() {
                if (isExistedBluetoothOperationToken(WriteToken.class)) {
                    return false;
                }
                putBluetoothOperationToken(WriteToken.class, this);
                return super.onRequestPrepared();
            }

            @Override
            protected Object invoke() {
                return innerWrite(characteristic, data);
            }

            @Override
            protected void onRequestFinished(boolean isTimeout) {
                super.onRequestFinished(isTimeout);
                removeBluetoothOperationToken(WriteToken.class);
            }
        };
        return writeToken;
    }

    public void createNotifyToken(String serviceUuid, String characteristicUuid,
                                  String descriptorUuid, boolean enable, boolean isIndication) {
        BluetoothGattService gattService = getBluetoothGattService(serviceUuid);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(
                gattService, characteristicUuid);
        BluetoothGattDescriptor descriptor = getBluetoothGattDescriptor(
                characteristic, descriptorUuid);
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

    private BluetoothGattService getBluetoothGattService(String serviceUuid) {
        return getBluetoothGatt().getService(UUID.fromString(serviceUuid));
    }

    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(
            BluetoothGattService service, String characteristicUuid) {
        return service.getCharacteristic(UUID.fromString(characteristicUuid));
    }

    private BluetoothGattDescriptor getBluetoothGattDescriptor(
            BluetoothGattCharacteristic characteristic, String descriptorUuid) {
        return characteristic.getDescriptor(UUID.fromString(descriptorUuid));
    }

}
