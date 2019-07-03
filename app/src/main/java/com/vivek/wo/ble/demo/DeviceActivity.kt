package com.vivek.wo.ble.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.vivek.wo.ble.BluetoothComms
import com.vivek.wo.ble.BluetoothDeviceExtend
import kotlinx.android.synthetic.main.activity_device.*

class DeviceActivity : AppCompatActivity() {
    private lateinit var bluetoothComms: BluetoothComms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        var bluetoothDeviceExtend = intent
                .getParcelableExtra<BluetoothDeviceExtend>("bluetoothDeviceExtend")
        device_txt_name.text = bluetoothDeviceExtend.deviceName
        device_txt_mac.text = bluetoothDeviceExtend.deviceAddress
        device_btn_connect.text = getString(R.string.device_connecting)
        device_btn_connect.isEnabled = false
        device_txt_record.text = Hex.byteToHex(bluetoothDeviceExtend.scanRecord)
        bluetoothComms = BluetoothComms(this, bluetoothDeviceExtend)
    }


}