package com.vivek.wo.ble.demo

object Hex {
    fun byteToHex(data: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (data == null || data.size <= 0) {
            return null
        }
        for (i in 0..data.size - 1) {
            val v = data[i].toInt() and 0xFF
            val hv = Integer.toHexString(v).toUpperCase()
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append("$hv ")
        }
        return stringBuilder.toString()
    }
}