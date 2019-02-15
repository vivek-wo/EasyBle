package com.vivek.wo.ble.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.vivek.wo.ble.PrintLog
import com.vivek.wo.ble.comms.BluetoothDeviceExtend
import com.vivek.wo.ble.scan.IScanCallback
import com.vivek.wo.ble.scan.ScanCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_list_main.view.*

class MainActivity : AppCompatActivity() {
    val PERMISSION_REQUESTCODE = 1001
    val PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    val deviceItemList = ArrayList<BluetoothDeviceExtend>()
    var recycleViewAdapter: RecycleViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_recyclerview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                        PERMISSION_ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(PERMISSION_ACCESS_COARSE_LOCATION), PERMISSION_REQUESTCODE)
            return
        }
        scanBLE()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISSION_REQUESTCODE == requestCode) {
            if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanBLE()
            } else {
                PrintLog.log("MainActivity", "PermissionRationale : " + ActivityCompat
                        .shouldShowRequestPermissionRationale(this, permissions[0]))
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                PERMISSION_ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "Please Open on Settings",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun scanBLE() {
        ScanCallback(this, object : IScanCallback {
            override fun onDeviceFound(bluetoothDeviceExtend: BluetoothDeviceExtend?,
                                       result: MutableList<BluetoothDeviceExtend>?) {
                if (bluetoothDeviceExtend!!.deviceName == null) {
                    return
                }
                if (!deviceItemList.contains(bluetoothDeviceExtend)) {
                    PrintLog.log("MainActivity", bluetoothDeviceExtend.toString())
                    deviceItemList.add(bluetoothDeviceExtend!!)
                }
                setAdapter()
            }

            override fun onScanFinish(result: MutableList<BluetoothDeviceExtend>?) {

            }

            override fun onScanTimeout() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Scan Timeout",
                            Toast.LENGTH_SHORT).show()
                }
            }

        }).scan()
    }

    fun setAdapter() {
        if (recycleViewAdapter == null) {
            recycleViewAdapter = RecycleViewAdapter(deviceItemList) { onItemClickListener(it) }
            main_recyclerview.layoutManager = LinearLayoutManager(this)
            main_recyclerview.adapter = recycleViewAdapter
        } else {
            recycleViewAdapter!!.notifyDataSetChanged()
        }
    }

    private fun onItemClickListener(bluetoothDeviceExtend: BluetoothDeviceExtend) {
        PrintLog.log("onItemClickListener", bluetoothDeviceExtend.toString())
        var intent = Intent(this@MainActivity,
                DeviceActivity::class.java)
        intent.putExtra("bluetoothDeviceExtend", bluetoothDeviceExtend)
        startActivity(intent)
    }

    class RecycleViewAdapter(private val deviceItemList: List<BluetoothDeviceExtend>,
                             private val onItemClickListener: (BluetoothDeviceExtend) -> Unit) :
            RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_main, parent, false)
            return ViewHolder(view, onItemClickListener)
        }

        override fun getItemCount(): Int = deviceItemList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(deviceItemList[position])
        }

        class ViewHolder(itemView: View?, val onItemClickListener: (BluetoothDeviceExtend) -> Unit) : RecyclerView.ViewHolder(itemView) {

            fun bind(deviceItem: BluetoothDeviceExtend) {
                itemView.itemlist_m_txt_rssi.text = deviceItem.rssi.toString()
                itemView.itemlist_m_txt_name.text = deviceItem.deviceName
                itemView.itemlist_m_txt_adress.text = deviceItem.deviceAddress
                if (deviceItem.isConnected) {
                    itemView.itemlist_m_txt_connectstatus.text = "Connected"
                } else {
                    itemView.itemlist_m_txt_connectstatus.text = "Disconnected"
                }
                itemView.setOnClickListener {
                    onItemClickListener(deviceItem)
                }
            }
        }
    }


}