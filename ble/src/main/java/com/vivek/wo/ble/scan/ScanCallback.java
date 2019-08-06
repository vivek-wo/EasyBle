package com.vivek.wo.ble.scan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;

import com.vivek.wo.ble.BluetoothDeviceExtend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanCallback implements ScanFilter {
    /**
     * 默认搜索时间
     */
    public static final int DEFAULT_SCANSECOND = 10;

    private BluetoothAdapter mBluetoothAdapter;
    private OnScanCallback mScanCallback;
    private int scanSecond = DEFAULT_SCANSECOND;
    private Map<String, BluetoothDeviceExtend> mBluetoothDeviceExtendMap;
    private List<BluetoothDeviceExtend> mBluetoothDeviceExtendList;
    //    是否正在搜索
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    // <android 5.0
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    // > android 5.0
    private List<android.bluetooth.le.ScanFilter> mScanFilterList;
    private ScanSettings.Builder mScanSettingsBuilder;
    private android.bluetooth.le.ScanCallback mLeScannerScanCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ScanCallback(Context context, OnScanCallback scanCallback) {
        this(((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter(), scanCallback);
    }

    public ScanCallback(BluetoothAdapter bluetoothAdapter, OnScanCallback scanCallback) {
        mScanCallback = scanCallback;
        mBluetoothAdapter = bluetoothAdapter;
        mBluetoothDeviceExtendMap = new LinkedHashMap<>();
        mBluetoothDeviceExtendList = new ArrayList<>();
    }

    /**
     * 设置搜索时间
     *
     * @param scanSecond
     * @return
     */
    public ScanCallback scanSecond(int scanSecond) {
        this.scanSecond = scanSecond;
        return this;
    }

    /**
     * ScanFilter过滤，21版本以上支持
     *
     * @param scanFilter
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanCallback addScanFilter(android.bluetooth.le.ScanFilter scanFilter) {
        if (mScanFilterList == null) {
            mScanFilterList = new ArrayList<>();
        }
        mScanFilterList.add(scanFilter);
        return this;
    }

    /**
     * ScanSettings配置，21版本以上支持
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanSettings.Builder getScanSettingsBuilder() {
        if (mScanSettingsBuilder == null) {
            mScanSettingsBuilder = new ScanSettings.Builder();
        }
        return mScanSettingsBuilder;
    }

    /**
     * 是否正在搜索
     *
     * @return
     */
    public boolean isScan() {
        return atomicBoolean.get();
    }

    /**
     * 搜索
     */
    public final void scan() {
        if (compareAndSet(false, true)) {
            clearCacheCollectionData();
            if (LOLLIPOP()) {
                leScannerScan();
            } else {
                leScan();
            }
            setupTimeoutTask();
        }
    }

    private void clearCacheCollectionData() {
        mBluetoothDeviceExtendMap.clear();
        mBluetoothDeviceExtendList.clear();
    }

    private Runnable mScanTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            timeout();
        }
    };

    private void setupTimeoutTask() {
        if (this.scanSecond > 0) {
            mHandler.postDelayed(mScanTimeoutRunnable, this.scanSecond * 1000);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void leScannerScan() {
        if (mScanSettingsBuilder == null) {
            mScanSettingsBuilder = new ScanSettings.Builder();
        }
        mLeScannerScanCallback = new android.bluetooth.le.ScanCallback() {
            @Override
            public final void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                onScanResultFond(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }
        };
        this.mBluetoothAdapter.getBluetoothLeScanner()
                .startScan(mScanFilterList, mScanSettingsBuilder.build(), mLeScannerScanCallback);
    }

    private void leScan() {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onScanResultFond(device, rssi, scanRecord);
            }
        };
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private boolean compareAndSet(boolean expect, boolean update) {
        return atomicBoolean.compareAndSet(expect, update);
    }

    /**
     * 停止搜索
     */
    public final void stop() {
        if (compareAndSet(true, false)) {
            mHandler.removeCallbacks(mScanTimeoutRunnable);
            stopScan();
        }
    }

    /**
     * 返回最近搜索结果列表
     *
     * @return
     */
    public List<BluetoothDeviceExtend> getLastScanFinishedDeviceList() {
        return mBluetoothDeviceExtendList;
    }

    /**
     * 返回最近搜索结果集合
     *
     * @return
     */
    public Map<String, BluetoothDeviceExtend> getLastScanFinishedDeviceMap() {
        return mBluetoothDeviceExtendMap;
    }

    private void stopScan() {
        if (LOLLIPOP()) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScannerScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void timeout() {
        if (compareAndSet(true, false)) {
            if (mScanCallback != null) {
                if (!mBluetoothDeviceExtendMap.isEmpty()) {
                    mScanCallback.onScanFinish(mBluetoothDeviceExtendList);
                } else {
                    mScanCallback.onScanTimeout();
                }
            }
            stopScan();
        }
    }

    private boolean LOLLIPOP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    private final void onScanResultFond(BluetoothDevice device, int rssi, byte[] scanRecord) {
        boolean isFilterResult = onFilter(device, rssi, scanRecord);
        if (isFilterResult) {
            BluetoothDeviceExtend bluetoothDeviceExtend;
            if (mBluetoothDeviceExtendMap.containsKey(device.getAddress())) {
                bluetoothDeviceExtend = mBluetoothDeviceExtendMap.get(device.getAddress());
                bluetoothDeviceExtend.setRssi(rssi);
                bluetoothDeviceExtend.setScanRecord(scanRecord);
            } else {
                bluetoothDeviceExtend = new BluetoothDeviceExtend(device, rssi, scanRecord);
                mBluetoothDeviceExtendMap.put(device.getAddress(), bluetoothDeviceExtend);
                mBluetoothDeviceExtendList.add(bluetoothDeviceExtend);
            }
            if (mScanCallback != null) {
                mScanCallback.onDeviceFound(bluetoothDeviceExtend, mBluetoothDeviceExtendList);
            }
        }
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return true;
    }

}
