package com.vivek.wo.ble.scan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import com.vivek.wo.ble.comms.BluetoothDeviceExtend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallback extends android.bluetooth.le.ScanCallback implements IScanFilter {
    private static final int DEFAULT_SCANSECOND = 10;//默认搜索时间
    private Handler handler = new Handler(Looper.myLooper());
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private IScanCallback scanCallback;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    protected List<ScanFilter> scanFilterList;
    protected ScanSettings.Builder scanSettingsBuilder;

    private Map<String, BluetoothDeviceExtend> bleDeviceFoundMap;
    private List<BluetoothDeviceExtend> bluetoothDeviceExtendList;
    private int scanSecond = DEFAULT_SCANSECOND;

    public ScanCallback(Context context, IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        bleDeviceFoundMap = new LinkedHashMap<>();
        bluetoothDeviceExtendList = new ArrayList<>();
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (LOLLIPOP()) {
            scanFilterList = new ArrayList<>();
            scanSettingsBuilder = new ScanSettings.Builder();
        }
    }

    public ScanCallback(BluetoothAdapter bluetoothAdapter, IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        this.bluetoothAdapter = bluetoothAdapter;
        bleDeviceFoundMap = new LinkedHashMap<>();
        bluetoothDeviceExtendList = new ArrayList<>();
        if (LOLLIPOP()) {
            scanFilterList = new ArrayList<>();
            scanSettingsBuilder = new ScanSettings.Builder();
        }
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
     * UUID过滤，21版本以上支持
     *
     * @param serviceUUIDs
     * @return
     */
    public ScanCallback serviceUUID(String... serviceUUIDs) {
        if (serviceUUIDs != null) {
            for (String serviceUUID : serviceUUIDs) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(
                                UUID.fromString(serviceUUID)))
                        .build();
                scanFilterList.add(filter);
            }
        }
        return this;
    }

    /**
     * 配置，21版本以上支持
     *
     * @param settingBundle
     * @return
     */
    public ScanCallback scanSettings(Bundle settingBundle) {
        scanSettingsBuilder.setScanMode((int) settingBundle.getDouble("scanMode"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanSettingsBuilder.setNumOfMatches((int) settingBundle
                    .getDouble("numberOfMatches", ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT));
            scanSettingsBuilder.setMatchMode((int) settingBundle
                    .getDouble("matchMode", ScanSettings.MATCH_MODE_AGGRESSIVE));
            scanSettingsBuilder.setNumOfMatches((int) settingBundle
                    .getDouble("matchNumMaxAdvertisement", ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT));
        }
        return this;
    }

    /**
     * 是否正在搜索
     *
     * @return
     */
    public boolean isScanning() {
        return atomicBoolean.get();
    }

    /**
     * 搜索
     */
    public final void scan() {
        if (atomicBoolean.compareAndSet(false, true)) {
            if (LOLLIPOP()) {
                this.bluetoothAdapter.getBluetoothLeScanner()
                        .startScan(scanFilterList, scanSettingsBuilder.build(), this);
            } else {
                leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        onScanResultFond(device, rssi, scanRecord);
                    }
                };
                this.bluetoothAdapter.startLeScan(leScanCallback);
            }
            if (this.scanSecond > 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (atomicBoolean.compareAndSet(true, false)) {
                            if (scanCallback != null) {
                                if (!bleDeviceFoundMap.isEmpty()) {
                                    scanCallback.onScanFinish(bluetoothDeviceExtendList);
                                } else {
                                    scanCallback.onScanTimeout();
                                }
                            }
                            stopOrTimeoutScan();
                        }
                    }
                }, this.scanSecond * 1000);
            }
        }
    }

    public final void stop() {
        if (atomicBoolean.compareAndSet(true, false)) {
            handler.removeCallbacksAndMessages(null);
            stopOrTimeoutScan();
        }
    }

    public List<BluetoothDeviceExtend> getLastScanFinishedDeviceList() {
        return new ArrayList<>(bleDeviceFoundMap.values());
    }

    public Map<String, BluetoothDeviceExtend> getLastScanFinishedDeviceMap() {
        return bleDeviceFoundMap;
    }

    private void stopOrTimeoutScan() {
        if (LOLLIPOP()) {
            this.bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
        } else {
            this.bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private boolean LOLLIPOP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @Override
    public final void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        onScanResultFond(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
    }

    final void onScanResultFond(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (onFilter(device, rssi, scanRecord)) {
            BluetoothDeviceExtend bluetoothDeviceExtend;
            if (bleDeviceFoundMap.containsKey(device.getAddress())) {
                bluetoothDeviceExtend = bleDeviceFoundMap.get(device.getAddress());
                bluetoothDeviceExtend.setRssi(rssi);
                bluetoothDeviceExtend.setScanRecord(scanRecord);
            } else {
                bluetoothDeviceExtend = new BluetoothDeviceExtend(device, rssi, scanRecord);
                bleDeviceFoundMap.put(device.getAddress(), bluetoothDeviceExtend);
            }
            if (scanCallback != null) {
                bluetoothDeviceExtendList.clear();
                bluetoothDeviceExtendList.addAll(bleDeviceFoundMap.values());
                scanCallback.onDeviceFound(bluetoothDeviceExtend, bluetoothDeviceExtendList);
                if (this instanceof SingleFilterScanCallback) {
                    scanCallback.onScanFinish(bluetoothDeviceExtendList);
                }
            }
        }
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return true;
    }

}
