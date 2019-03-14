package com.vivek.wo.ble.comms;

import com.vivek.wo.ble.BluetoothComms;

public class FunctionToken extends Token {
    private BluetoothComms mBluetoothComms;

    public FunctionToken(String tokenContext, BluetoothComms bluetoothComms) {
        super(tokenContext);
        mBluetoothComms = bluetoothComms;
    }

    @Override
    public void run() {
        super.run();
        mBluetoothComms.onTimeoutCallback(this);
    }

    @Override
    public Token execute() {
        mBluetoothComms.execute(this);
        return super.execute();
    }

}
