package com.vivek.wo.ble.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.vivek.wo.ble.internal.BluetoothComms
import com.vivek.wo.ble.internal.BluetoothDeviceExtend
import com.vivek.wo.ble.internal.BluetoothException
import com.vivek.wo.ble.internal.OnActionListener
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
        bluetoothComms.connect(object : OnActionListener {
            override fun onSuccess(vararg args: Any?) {
                runOnUiThread {
                    device_btn_connect.setText(getString(R.string.device_disconnect))
                    device_btn_connect.isEnabled = true
                }
            }

            override fun onFailure(exception: BluetoothException?) {
                if (exception?.reasonCode == BluetoothException.BLUETOOTH_FUNCTION_TIMEOUT) {
                    runOnUiThread {
                        Toast.makeText(this@DeviceActivity, "Connect Timeout",
                                Toast.LENGTH_SHORT).show()
                        device_btn_connect.setText(getString(R.string.device_connect))
                        device_btn_connect.isEnabled = true
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DeviceActivity, "Connect failure",
                                Toast.LENGTH_SHORT).show()
                        device_btn_connect.setText(getString(R.string.device_connect))
                        device_btn_connect.isEnabled = true
                    }
                }
            }

        }).invoke()
    }


}