package com.sam_chordas.android.stockhawk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by amit on 21-06-2016.
 */
public class StockNotFound extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        Toast.makeText(context,context.getString(R.string.bad_stock_input),Toast.LENGTH_SHORT).show();
    }
}
