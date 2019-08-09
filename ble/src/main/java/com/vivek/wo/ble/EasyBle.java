package com.vivek.wo.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EasyBle {
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothStateObserver mStateObserver;
    private BroadcastReceiver mBluetoothStateChangedReceiver;
    private ScanCallback mScanCallback;
    private Map<String, BluetoothComms> mConnectedDeviceExtendMap = new HashMap<>();

    private EasyBle() {
    }

    private static class EasyBleHolder {
        private static final EasyBle instance = new EasyBle();
    }

    public static EasyBle getInstance() {
        return EasyBleHolder.instance;
    }

    /**
     * 设置蓝牙状态监听器
     */
    public void setBluetoothStateObserver(BluetoothStateObserver stateObserver) {
        mStateObserver = stateObserver;
    }

    /**
     * 初始化蓝牙
     *
     * @param context
     */
    public void init(Context context) {
        if (this.mContext == null && context != null) {
            this.mContext = context.getApplicationContext();
            mBluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                return;
            }
            registerReceiver();
        }
    }

    /**
     * 反初始化
     */
    public void uninit() {
        if (this.mContext != null) {
            if (mBluetoothAdapter != null) {
                unregisterReceiver();
                disconnectAll();
            }
            this.mContext = null;
        }
    }

    private void unregisterReceiver() {
        if (mBluetoothStateChangedReceiver != null) {
            mContext.unregisterReceiver(mBluetoothStateChangedReceiver);
            mBluetoothStateChangedReceiver = null;
        }
    }

    private void registerReceiver() {
        mBluetoothStateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    if (mStateObserver != null) {
                        mStateObserver.onStateChanged(state);
                    }
                }
            }
        };
        mContext.registerReceiver(mBluetoothStateChangedReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**
     * 是否支持BLE
     *
     * @return true 支持
     */
    public boolean isSupportBle() {
        return mContext.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 是否支持蓝牙
     *
     * @return true 支持
     */
    public boolean isSupportBluetooth() {
        return mBluetoothAdapter != null;
    }

    /**
     * 蓝牙是否打开
     *
     * @return
     */
    public boolean isEnabled() {
        return mBluetoothAdapter != null ? mBluetoothAdapter.isEnabled() : false;
    }

    /**
     * 打开蓝牙
     *
     * @param activity    上下文
     * @param requestCode If >= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @throws BluetoothException
     */
    public void enableBluetooth(Activity activity, int requestCode) {
        if (activity == null) {
            throw new NullPointerException("Activity cannot be NULL.");
        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    private void checkBluetoothAdapterNULL() throws BluetoothException {
        if (mBluetoothAdapter == null) {
            throw new BluetoothException(new NullPointerException("BluetoothAdapter NULL."));
        }
    }

    /**
     * 蓝牙搜索
     *
     * @param callback
     * @throws BluetoothException
     */
    public void scan(OnScanCallback callback) {
        checkBluetoothAdapterNULL();
        if (mScanCallback == null) {
            mScanCallback = new ScanCallback(mBluetoothAdapter, callback);
        }
        mScanCallback.scan();
    }

    /**
     * 停止搜索
     */
    public void stopScan() {
        if (mScanCallback != null) {
            mScanCallback.stop();
        }
    }

    /**
     * 是否正在搜索
     *
     * @return
     */
    public boolean isScan() {
        return mScanCallback != null && mScanCallback.isScan();
    }

    /**
     * 蓝牙设备连接
     */
    public void connect(final BluetoothDeviceExtend bluetoothDeviceExtend,
                        final OnActionListener listener) {
        final BluetoothComms bluetoothComms;
        if (mConnectedDeviceExtendMap.containsKey(
                bluetoothDeviceExtend.getDeviceAddress())) {
            bluetoothComms = mConnectedDeviceExtendMap.get(bluetoothDeviceExtend.getDeviceAddress());
            bluetoothComms.setBluetoothDeviceExtend(bluetoothDeviceExtend);
        } else {
            bluetoothComms = new BluetoothComms(mContext, bluetoothDeviceExtend);
        }
        bluetoothComms.createConnectToken().setOnActionListener(new OnActionListener() {
            @Override
            public void onSuccess(Object... args) {
                if (!mConnectedDeviceExtendMap.containsKey(
                        bluetoothDeviceExtend.getDeviceAddress())) {
                    mConnectedDeviceExtendMap.put(
                            bluetoothDeviceExtend.getDeviceAddress(), bluetoothComms);
                }
                if (listener != null) {
                    listener.onSuccess(args);
                }
            }

            @Override
            public void onFailure(BluetoothException exception) {
                if (listener != null) {
                    listener.onFailure(exception);
                }
            }
        }).connect();
    }

    /**
     * 蓝牙MAC地址直接连接
     */
    public void connect(final String deviceAddress, final OnActionListener listener) {
        final BluetoothComms bluetoothComms;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        } else {
            if (!mBluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                throw new IllegalArgumentException("DeviceAddress Invalid");
            }
            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            if (bluetoothDevice == null) {
                //back
                throw new BluetoothException("DeviceAddress device can not found");
            }
            bluetoothComms = new BluetoothComms(mContext,
                    new BluetoothDeviceExtend(bluetoothDevice));
        }
        bluetoothComms.createConnectToken().setOnActionListener(new OnActionListener() {
            @Override
            public void onSuccess(Object... args) {
                if (!mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
                    mConnectedDeviceExtendMap.put(deviceAddress, bluetoothComms);
                }
                if (listener != null) {
                    listener.onSuccess(args);
                }
            }

            @Override
            public void onFailure(BluetoothException exception) {
                if (listener != null) {
                    listener.onFailure(exception);
                }
            }
        }).connect();
    }

    /**
     * 蓝牙MAC地址，先搜索后连接
     */
    public void scanConnect(String deviceAddress, final OnActionListener listener) {
        new SingleFilterScanCallback(mBluetoothAdapter,
                new OnScanCallback() {
                    @Override
                    public void onDeviceFound(BluetoothDeviceExtend bluetoothDeviceExtend, List<BluetoothDeviceExtend> result) {
                        connect(bluetoothDeviceExtend, listener);
                    }

                    @Override
                    public void onScanFinish(List<BluetoothDeviceExtend> result) {
                    }

                    @Override
                    public void onScanTimeout() {
                        if (listener != null) {
                            BluetoothException exception = new BluetoothException(
                                    "Scan timeout.");
                            listener.onFailure(exception);
                        }
                    }
                })
                .setDeviceAddress(deviceAddress)
                .scan();
    }

    /**
     * 根据蓝牙MAC地址断开当前连接
     */
    public void disconnect(String deviceAddress) throws BluetoothException {
        BluetoothComms bluetoothComms;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.remove(deviceAddress);
            bluetoothComms.createDisconnectToken().disconnect();
        }
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        Iterator<BluetoothComms> iterator = mConnectedDeviceExtendMap.values().iterator();
        while (iterator.hasNext()) {
            BluetoothComms bluetoothComms = iterator.next();
            bluetoothComms.createDisconnectToken().disconnect();
        }
    }

    /**
     * 读取数据
     */
    public void read(String deviceAddress, String serviceUuid,
                     String characteristicUuid, final OnActionListener listener) {
        BluetoothComms bluetoothComms = null;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        }
        if (bluetoothComms == null || !bluetoothComms.isConnected()) {
            //无连接
            return;
        }
        ReadToken readToken = bluetoothComms.createReadToken(serviceUuid, characteristicUuid);
        if (listener != null) {
            readToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    listener.onSuccess(args);
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    listener.onFailure(exception);
                }
            });
        }
        readToken.read();
    }

    /**
     * 写数据
     */
    public void write(String deviceAddress, String serviceUuid,
                      String characteristicUuid, byte[] data, final OnActionListener listener) {
        BluetoothComms bluetoothComms = null;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        }
        if (bluetoothComms == null || !bluetoothComms.isConnected()) {
            //无连接
            return;
        }
        WriteToken writeToken = bluetoothComms.createWriteToken(serviceUuid, characteristicUuid);
        if (listener != null) {
            writeToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    listener.onSuccess(args);
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    listener.onFailure(exception);
                }
            });
        }
        writeToken.write(data);
    }

    /**
     * 打开数据通知和关闭通知
     */
    public void notify(String deviceAddress, String serviceUuid,
                       String characteristicUuid, String descriptorUuid,
                       boolean enable, boolean isIndication, final OnActionListener listener) {
        BluetoothComms bluetoothComms = null;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        }
        if (bluetoothComms == null || !bluetoothComms.isConnected()) {
            //无连接
            return;
        }
        NotifyToken notifyToken = bluetoothComms.createNotifyToken(serviceUuid, characteristicUuid,
                descriptorUuid);
        if (listener != null) {
            notifyToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    listener.onSuccess(args);
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    listener.onFailure(exception);
                }
            });
        }
        notifyToken.setIndication(isIndication).notify(enable);
    }

    /**
     * 读取RSSI
     */
    public void readRssi(String deviceAddress, final OnActionListener listener) {
        BluetoothComms bluetoothComms = null;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        }
        if (bluetoothComms == null || !bluetoothComms.isConnected()) {
            //无连接
            return;
        }
        RssiToken rssiToken = bluetoothComms.createRssiToken();
        if (listener != null) {
            rssiToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    listener.onSuccess(args);
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    listener.onFailure(exception);
                }
            });
        }
        rssiToken.readRssi();
    }

    /**
     * 设置MTU
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setMTU(String deviceAddress, int mtu, final OnActionListener listener) {
        BluetoothComms bluetoothComms = null;
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        }
        if (bluetoothComms == null || !bluetoothComms.isConnected()) {
            //无连接
            return;
        }
        MTUToken mtuToken = bluetoothComms.createMTUToken();
        if (listener != null) {
            mtuToken.setOnActionListener(new OnActionListener() {
                @Override
                public void onSuccess(Object... args) {
                    listener.onSuccess(args);
                }

                @Override
                public void onFailure(BluetoothException exception) {
                    listener.onFailure(exception);
                }
            });
        }
        mtuToken.setMTU(mtu);
    }

    /**
     * 获取最后一次扫描的列表
     *
     * @return 蓝牙设备集合
     */
    public List<BluetoothDeviceExtend> getLastScanFinishedDeviceList() {
        if (mScanCallback != null) {
            return mScanCallback.getLastScanFinishedDeviceList();
        }
        return null;
    }


    /**
     * 获取最后一次扫描的集合
     *
     * @return
     */
    public Map<String, BluetoothDeviceExtend> getLastScanFinishedDeviceMap() {
        if (mScanCallback != null) {
            return mScanCallback.getLastScanFinishedDeviceMap();
        }
        return null;
    }

    /***
     * 获取已连接的设备列表
     *
     * @return 蓝牙设备集合
     */
    public List<BluetoothComms> getConnectedDeviceList() {
        return new ArrayList<>(mConnectedDeviceExtendMap.values());
    }

    /**
     * 设置蓝牙监听
     */
    public void addBluetoothCommObserver(String deviceAddress, GattCommsObserver commObserver) {
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
            bluetoothComms.addGattCommsObserver(commObserver);
        }
    }

    /**
     * 取消蓝牙监听
     */
    public void removeBluetoothCommObserver(String deviceAddress, GattCommsObserver commObserver) {
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
            bluetoothComms.removeGattCommsObserver(commObserver);
        }
    }

    /**
     * 是否已经连接
     *
     * @return true 已连接
     */
    public boolean isConnected(String deviceAddress) {
        BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        return bluetoothComms != null && bluetoothComms.isConnected();
    }

    /**
     * 根据蓝牙设备MAC地址获取已连接的蓝牙设备
     *
     * @return
     */
    public BluetoothDeviceExtend getConnectedBluetoothDeviceExtend(String deviceAddress) {
        BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
        return bluetoothComms != null && bluetoothComms.isConnected() ? bluetoothComms.getBluetoothDeviceExtend() : null;
    }

    /**
     * 检查当前蓝牙状态 ,检查结果在监听器中返回
     */
    public void checkState() {
        mBluetoothAdapter.getState();
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    public List<BluetoothGattService> getGattServiceList(String deviceAddress) {
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
            return bluetoothComms.getGattServiceList();
        }
        return null;
    }

    /**
     * 获取某个服务的特征值列表
     *
     * @return
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(
            String deviceAddress, BluetoothGattService gattService) {
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
            return bluetoothComms.getGattCharacteristicList(gattService);
        }
        return null;
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @return
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(
            String deviceAddress, BluetoothGattCharacteristic gattCharacteristic) {
        if (mConnectedDeviceExtendMap.containsKey(deviceAddress)) {
            BluetoothComms bluetoothComms = mConnectedDeviceExtendMap.get(deviceAddress);
            return bluetoothComms.getGattDescriptorList(gattCharacteristic);
        }
        return null;
    }

    public interface BluetoothStateObserver {

        /**
         * 蓝牙状态改变监听
         *
         * <p>
         * {@link android.bluetooth.BluetoothAdapter#STATE_OFF},
         * {@link android.bluetooth.BluetoothAdapter#STATE_ON},
         * {@link android.bluetooth.BluetoothAdapter#STATE_TURNING_OFF},
         * {@link android.bluetooth.BluetoothAdapter#STATE_TURNING_ON}
         * </P>
         *
         * @param state
         */
        void onStateChanged(int state);
    }

}
