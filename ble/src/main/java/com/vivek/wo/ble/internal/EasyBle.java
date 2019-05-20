package com.vivek.wo.ble.internal;

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

import com.vivek.wo.ble.MethodQueueHandler;
import com.vivek.wo.ble.OnScanCallback;
import com.vivek.wo.ble.ScanCallback;
import com.vivek.wo.ble.SingleFilterScanCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EasyBle {
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private MethodQueueHandler mMethodQueueHandler;
    private BluetoothStateObserver mStateObserver;
    private BroadcastReceiver mBluetoothStateChangedReceiver;
    private ScanCallback mScanCallback;

    private Map<String, BluetoothComms> mConnectedDeviceExtendMap = new HashMap<>();

    private EasyBle() {
        mMethodQueueHandler = new MethodQueueHandler();
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
     * 销毁
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
     * @param requestCode
     */
    public void enableBluetooth(Activity activity, int requestCode) throws BluetoothException {
        if (activity == null) {
            throw new BluetoothException(new NullPointerException("Activity cannot be NULL."));
        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    private void checkBluetoothAdapterNULL() throws BluetoothException {
        if (mBluetoothAdapter == null) {
            throw new BluetoothException(new NullPointerException("BluetoothAdapter NULL"));
        }
    }

    /**
     * 蓝牙搜索
     *
     * @param callback
     * @throws BluetoothException
     */
    public void scan(OnScanCallback callback) throws BluetoothException {
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
    public void connect(BluetoothDeviceExtend bluetoothDeviceExtend,
                        OnActionListener listener) {
        BluetoothComms comms = new BluetoothComms(mContext, bluetoothDeviceExtend);
        comms.setMethodQueueHandler(mMethodQueueHandler);
        comms.connect(listener).invoke();
    }

    /**
     * 蓝牙MAC地址直接连接
     */
    public void connect(String deviceAddress, OnActionListener listener) throws BluetoothException {
        CommonMethod.checkBluetoothAddress(deviceAddress);
        BluetoothDevice device = CommonMethod.getRemoteDevice(mBluetoothAdapter, deviceAddress);
        BluetoothComms comms = new BluetoothComms(mContext, new BluetoothDeviceExtend(device));
        comms.setMethodQueueHandler(mMethodQueueHandler);
        comms.connect(listener).invoke();
    }

    /**
     * 蓝牙MAC地址，先搜索后连接
     */
    public void scanConnect(String deviceAddress, final OnActionListener listener) {
        new SingleFilterScanCallback(mBluetoothAdapter,
                new OnScanCallback() {
                    @Override
                    public void onDeviceFound(BluetoothDeviceExtend bluetoothDeviceExtend, List<BluetoothDeviceExtend> result) {
                        BluetoothComms comms = new BluetoothComms(mContext, bluetoothDeviceExtend);
                        comms.setMethodQueueHandler(mMethodQueueHandler);
                        comms.connect(listener).invoke();
                    }

                    @Override
                    public void onScanFinish(List<BluetoothDeviceExtend> result) {
                    }

                    @Override
                    public void onScanTimeout() {
                        if (listener != null) {
                            BluetoothException exception = new BluetoothException(
                                    BluetoothException.BLUETOOTH_SCAN_TIMEOUT,
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
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        comms.disconnect();
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        Iterator<BluetoothComms> iterator = mConnectedDeviceExtendMap.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().disconnect();
        }
    }

    /**
     * 读取数据
     */
    public void read(String deviceAddress, String serviceUUIDString,
                     String characteristicUUIDString, OnActionListener listener)
            throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        comms.read(serviceUUIDString, characteristicUUIDString, listener).invoke();
    }

    /**
     * 写数据
     */
    public void write(String deviceAddress, String serviceUUIDString,
                      String characteristicUUIDString, byte[] data, OnActionListener listener)
            throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        comms.write(serviceUUIDString, characteristicUUIDString, data, listener).invoke();
    }

    /**
     * 打开数据通知和关闭通知
     */
    public void notify(String deviceAddress, String serviceUUIDString,
                       String characteristicUUIDString, String descriptorUUIDString,
                       boolean enable, boolean isIndication, OnActionListener listener)
            throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        comms.notify(serviceUUIDString, characteristicUUIDString, descriptorUUIDString,
                enable, isIndication, listener).invoke();
    }

    /**
     * 读取RSSI
     */
    public void readRssi(String deviceAddress, OnActionListener listener)
            throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        comms.rssi(listener).invoke();
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
    public void setBluetoothCommObserver(String deviceAddress, BluetoothCommObserver commObserver)
            throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        comms.setBluetoothCommObserver(commObserver);
    }

    /**
     * 是否已经连接
     *
     * @return true 已连接
     */
    public boolean isConnected(String deviceAddress) {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        return comms != null && comms.isConnected();
    }

    /**
     * 根据蓝牙设备MAC地址获取已连接的蓝牙设备
     *
     * @return
     */
    public BluetoothDeviceExtend getConnectedBluetoothDeviceExtend(String deviceAddress) {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        return comms != null && comms.isConnected() ? comms.getBluetoothDeviceExtend() : null;
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
    public List<BluetoothGattService> getGattServiceList(String deviceAddress) throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        return null;
    }

    /**
     * 获取某个服务的特征值列表
     *
     * @return
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(
            String deviceAddress, BluetoothGattService gattService) throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        return null;
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @return
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(
            String deviceAddress, BluetoothGattCharacteristic gattCharacteristic) throws BluetoothException {
        BluetoothComms comms = mConnectedDeviceExtendMap.get(deviceAddress);
        CommonMethod.checkNotConnected(comms);
        return null;
    }

}
