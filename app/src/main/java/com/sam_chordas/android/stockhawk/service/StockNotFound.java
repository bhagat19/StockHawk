package com.sam_chordas.android.stockhawk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by amit on 21-06-2016.
 */
public class StockNotFound extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        Toast.makeText(context,"This stock does not exist",Toast.LENGTH_SHORT).show();
    }
}
