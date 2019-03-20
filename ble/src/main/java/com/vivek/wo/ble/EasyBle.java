package com.vivek.wo.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.util.List;
import java.util.Map;

public class EasyBle {
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBluetoothStateChangedReceiver;

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
    public void setBluetoothStateChangedObserver() {
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
    public int enableBluetooth(Activity activity, int requestCode) throws BluetoothException {
        if (activity != null) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, requestCode);
            return 0;
        } else {
            throw new BluetoothException(new NullPointerException("Activity cannot be NULL."));
        }
    }

    /**
     * 蓝牙搜索
     */
    public void scan(OnScanCallback callback) {

    }

    /**
     * 停止搜索
     */
    public void stopScan() {
    }

    /**
     * 蓝牙设备连接
     */
    public void connect(BluetoothDeviceExtend bluetoothDeviceExtend,
                        OnActionListener listener) {
    }

    /**
     * 蓝牙MAC地址直接连接
     */
    public void connect(String deviceAddress, OnActionListener listener) {
    }

    /**
     * 蓝牙MAC地址，先搜索后连接
     */
    public void scanConnect(String deviceAddress, OnActionListener listener) {
    }

    /**
     * 根据蓝牙MAC地址断开当前连接
     */
    public void disconnect(String deviceAddress) {
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
    }

    /**
     * 读取数据
     */
    public void read(String deviceAddress, String serviceUUIDString,
                     String characteristicUUIDString, OnActionListener listener) {
    }

    /**
     * 写数据
     */
    public void write(String deviceAddress, String serviceUUIDString,
                      String characteristicUUIDString, byte[] data, OnActionListener listener) {
    }

    /**
     * 写数据
     */
    public void writeNoResp(String deviceAddress, String serviceUUIDString,
                            String characteristicUUIDString, byte[] data, OnActionListener listener) {
    }

    /**
     * 打开数据通知和关闭通知
     */
    public void notify(String deviceAddress, String serviceUUIDString,
                       String characteristicUUIDString, boolean enable, OnActionListener listener) {
    }

    /**
     * 读取RSSI
     */
    public void readRssi(String deviceAddress, OnActionListener listener) {
    }

    /**
     * 获取最后一次扫描的列表
     *
     * @return 蓝牙设备集合
     */
    public List<BluetoothDeviceExtend> getLastScanFinishedDeviceList() {
        return null;
    }


    public Map<String, BluetoothDeviceExtend> getLastScanFinishedDeviceMap() {
        return null;
    }

    /***
     * 获取已连接的设备列表
     *
     * @return 蓝牙设备集合
     */
    public List<BluetoothDeviceExtend> getConnectedDeviceList() {
        return null;
    }

    /**
     * 设置蓝牙监听
     */
    public void setBluetoothCommObserver(String deviceAddress) {
    }

    /**
     * 是否已经连接
     *
     * @return true 已连接
     */
    public boolean isConnected(String deviceAddress) {
        return false;
    }

    /**
     * 根据蓝牙设备MAC地址获取已连接的蓝牙设备
     *
     * @return
     */
    public BluetoothDeviceExtend getConnectedBluetoothDeviceExtend(String deviceAddress) {
        return null;
    }

    /**
     * 检查当前蓝牙状态 ,检查结果在监听器中返回
     */
    public void checkState() {
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    public List<BluetoothGattService> getGattServiceList(String deviceAddress) {
        return null;
    }

    /**
     * 获取某个服务的特征值列表
     *
     * @return
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(
            String deviceAddress, BluetoothGattService gattService) {
        return null;
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @return
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(
            String deviceAddress, BluetoothGattCharacteristic gattCharacteristic) {
        return null;
    }

}
