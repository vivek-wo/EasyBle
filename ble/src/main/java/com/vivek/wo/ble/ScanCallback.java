package com.vivek.wo.ble;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanCallback implements ScanFilter {
    /**
     * 默认搜索时间
     */
    static final int DEFAULT_SCANSECOND = 10;
    private BluetoothAdapter bluetoothAdapter;
    /**
     * 是否正在搜索
     */
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private BluetoothAdapter.LeScanCallback leScanCallback;
    /**
     * {@link android.bluetooth.le.ScanFilter}
     */
    private List<android.bluetooth.le.ScanFilter> scanFilterList;
    /**
     * {@link ScanSettings}
     */
    private ScanSettings.Builder scanSettingsBuilder;
    private android.bluetooth.le.ScanCallback leScannerScanCallback;
    /**
     * 蓝牙搜索回调
     */
    private OnScanCallback scanCallback;

    /**
     * 搜索时间
     */
    private int scanSecond = DEFAULT_SCANSECOND;

    /**
     * 蓝牙搜索集合
     */
    private Map<String, BluetoothDeviceExtend> bluetoothDeviceExtendMap;
    /**
     * 蓝牙搜索列表
     */
    private List<BluetoothDeviceExtend> bluetoothDeviceExtendList;

    private Handler handler = new Handler(Looper.myLooper());

    public ScanCallback(Context context, OnScanCallback scanCallback) {
        this(((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter(), scanCallback);
    }

    public ScanCallback(BluetoothAdapter bluetoothAdapter, OnScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        this.bluetoothAdapter = bluetoothAdapter;
        bluetoothDeviceExtendMap = new LinkedHashMap<>();
        bluetoothDeviceExtendList = new ArrayList<>();
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
        if (scanFilterList == null) {
            scanFilterList = new ArrayList<>();
        }
        scanFilterList.add(scanFilter);
        return this;
    }

    /**
     * ScanSettings配置，21版本以上支持
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanSettings.Builder getScanSettingsBuilder() {
        if (scanSettingsBuilder == null) {
            scanSettingsBuilder = new ScanSettings.Builder();
        }
        return scanSettingsBuilder;
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
            if (LOLLIPOP()) {
                leScannerScan();
            } else {
                leScan();
            }
            setupTimeoutTask();
        }
    }

    private Runnable scanTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            timeout();
        }
    };

    private void setupTimeoutTask() {
        if (this.scanSecond > 0) {
            handler.postDelayed(scanTimeoutRunnable, this.scanSecond * 1000);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void leScannerScan() {
        if (scanSettingsBuilder == null) {
            scanSettingsBuilder = new ScanSettings.Builder();
        }
        leScannerScanCallback = new android.bluetooth.le.ScanCallback() {
            @Override
            public final void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                onScanResultFond(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }
        };
        this.bluetoothAdapter.getBluetoothLeScanner()
                .startScan(scanFilterList, scanSettingsBuilder.build(), leScannerScanCallback);
    }

    private void leScan() {
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onScanResultFond(device, rssi, scanRecord);
            }
        };
        this.bluetoothAdapter.startLeScan(leScanCallback);
    }

    private boolean compareAndSet(boolean expect, boolean update) {
        return atomicBoolean.compareAndSet(expect, update);
    }

    /**
     * 停止搜索
     */
    public final void stop() {
        if (compareAndSet(true, false)) {
            handler.removeCallbacks(scanTimeoutRunnable);
            stopScan();
        }
    }

    /**
     * 返回最近搜索结果列表
     *
     * @return
     */
    public List<BluetoothDeviceExtend> getLastScanFinishedDeviceList() {
        return bluetoothDeviceExtendList;
    }

    /**
     * 返回最近搜索结果集合
     *
     * @return
     */
    public Map<String, BluetoothDeviceExtend> getLastScanFinishedDeviceMap() {
        return bluetoothDeviceExtendMap;
    }

    private void stopScan() {
        if (LOLLIPOP()) {
            this.bluetoothAdapter.getBluetoothLeScanner().stopScan(leScannerScanCallback);
        } else {
            this.bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private void timeout() {
        if (compareAndSet(true, false)) {
            if (scanCallback != null) {
                if (!bluetoothDeviceExtendMap.isEmpty()) {
                    scanCallback.onScanFinish(bluetoothDeviceExtendList);
                } else {
                    scanCallback.onScanTimeout();
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
            if (bluetoothDeviceExtendMap.containsKey(device.getAddress())) {
                bluetoothDeviceExtend = bluetoothDeviceExtendMap.get(device.getAddress());
                bluetoothDeviceExtend.setRssi(rssi);
                bluetoothDeviceExtend.setScanRecord(scanRecord);
            } else {
                bluetoothDeviceExtend = new BluetoothDeviceExtend(device, rssi, scanRecord);
                bluetoothDeviceExtendMap.put(device.getAddress(), bluetoothDeviceExtend);
            }
            if (scanCallback != null) {
                bluetoothDeviceExtendList.clear();
                bluetoothDeviceExtendList.addAll(bluetoothDeviceExtendMap.values());
                scanCallback.onDeviceFound(bluetoothDeviceExtend, bluetoothDeviceExtendList);
            }
        }
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return true;
    }

}
