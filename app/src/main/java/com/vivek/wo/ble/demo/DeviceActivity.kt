package com.vivek.wo.ble.demo

import android.os.Bundle
import android.widget.Toast
import com.vivek.wo.ble.BluetoothComms
import com.vivek.wo.ble.BluetoothDeviceExtend

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
        val token = bluetoothComms
                .connect(object {
                    override fun onConnected(token: Token?) {
                        runOnUiThread {
                            device_btn_connect.setText(getString(R.string.device_disconnect))
                            device_btn_connect.isEnabled = true
                        }
                    }

                    override fun onConnectFailure(token: Token?, status: Int) {
                        runOnUiThread {
                            Toast.makeText(this@DeviceActivity, "Connect failure",
                                    Toast.LENGTH_SHORT).show()
                            device_btn_connect.setText(getString(R.string.device_connect))
                            device_btn_connect.isEnabled = true
                        }
                    }

                    override fun onTimeout(token: Token?) {
                        runOnUiThread {
                            Toast.makeText(this@DeviceActivity, "Connect Timeout",
                                    Toast.LENGTH_SHORT).show()
                            device_btn_connect.setText(getString(R.string.device_connect))
                            device_btn_connect.isEnabled = true
                        }
                    }

                    override fun onDisconnected(token: Token?, isActiveDisconnect: Boolean) {
                        runOnUiThread {
                            Toast.makeText(this@DeviceActivity, "onDisconnected",
                                    Toast.LENGTH_SHORT).show()
                            device_btn_connect.setText(getString(R.string.device_connect))
                            device_btn_connect.isEnabled = true
                        }
                    }

                })
                .timeout(2 * 1000)
                .execute()
    }


}