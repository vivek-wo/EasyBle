package com.vivek.wo.ble;

import android.util.Log;

public class PrintLog {
    public static void log(String tag, String log) {
        Log.w(tag, "++> " + log);
    }
}
