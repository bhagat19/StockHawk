package com.sam_chordas.android.stockhawk.ui;

import android.app.IntentService;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.HistoricalQuote;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.ArrayList;

/**
 * Created by amit on 23-06-2016.
 */
public class StockDetailActivity extends AppCompatActivity  {
    MyReceiver myReceiver;
    Intent mServiceIntent;
    static final int CURSOR_LOADER_ID =11;
    final String LOG_TAG = StockDetailActivity.class.getSimpleName();
    static String name;
    static LineChart chart;
  //  ArrayList<Entry> entryArrayList;

    public void onCreate(Bundle savedInstance){
        myReceiver = new MyReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StockTaskService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_line_graph);

        Intent intent = getIntent();
        /*
       HistoricalQuote item = intent.getExtras().getParcelable("HistoricalQuote");
        Log.v(LOG_TAG,"item in DetailActivity "+item);
      ArrayList<Entry> entryArrayList =new ArrayList<Entry>();
*/
        String symbol = intent.getStringExtra("symbol");

        name = intent.getStringExtra("name");
   //     Log.v(LOG_TAG,"entryArrayList,symbol,name"+entryArrayList+symbol+name);


        chart = (LineChart) findViewById(R.id.linechart);
//       PlotGraph(chart,entryArrayList,name);

/*
        mServiceIntent = new Intent(this, StockIntentService.class);
        mServiceIntent.putExtra("tag", "history");
        mServiceIntent.putExtra("historicalSymbol", symbol);
        startActivity(intent);
        */

    }

    public static void PlotGraph(LineChart chart, ArrayList<Entry> entryArrayList,String name){

        LineDataSet entryDataSet = new LineDataSet(entryArrayList,name);
      //  entryDataSet.setAxisDependency();
        ArrayList<ILineDataSet> iLineDataSet = new ArrayList<ILineDataSet>();
        iLineDataSet.add(entryDataSet);

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("1.Q"); xVals.add("2.Q"); xVals.add("3.Q"); xVals.add("4.Q");
        xVals.add("5.Q"); xVals.add("6.Q"); xVals.add("7.Q");

        LineData lineData = new LineData(xVals,iLineDataSet);
        chart.setData(lineData);
        chart.invalidate();

    }
    @Override
    public void onResume(){
        super.onResume();
    }


    public class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent){

           Bundle bundle = intent.getExtras();
           HistoricalQuote item= bundle.getParcelable("HistoricalQuote");
            ArrayList<Entry> enterArrayList = item.getEntryArrayList();
            Log.v(LOG_TAG,"enterArrayList "+enterArrayList);
            StockDetailActivity.PlotGraph(chart,enterArrayList,name);

        }

    }


    }

