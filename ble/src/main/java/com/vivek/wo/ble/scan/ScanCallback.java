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
import android.os.Handler;
import android.os.Looper;

import com.vivek.wo.ble.BluetoothDeviceExtend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        this(((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter(), scanCallback);
    }

    public ScanCallback(BluetoothAdapter bluetoothAdapter, IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        this.bluetoothAdapter = bluetoothAdapter;
        bleDeviceFoundMap = new LinkedHashMap<>();
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
    public ScanCallback addScanFilter(ScanFilter scanFilter) {
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
    public boolean isScanning() {
        return atomicBoolean.get();
    }

    /**
     * 搜索
     */
    public final void scan() {
        if (atomicBoolean.compareAndSet(false, true)) {
            if (LOLLIPOP()) {
                if (scanSettingsBuilder == null) {
                    scanSettingsBuilder = new ScanSettings.Builder();
                }
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

    /**
     * @return
     */
    public List<BluetoothDeviceExtend> getLastScanFinishedDeviceList() {
        return new ArrayList<>(bleDeviceFoundMap.values());
    }

    /**
     * @return
     */
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
