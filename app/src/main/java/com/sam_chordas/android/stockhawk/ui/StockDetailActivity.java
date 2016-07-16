package com.sam_chordas.android.stockhawk.ui;

import android.app.IntentService;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import com.sam_chordas.android.stockhawk.data.HistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.HistUtils;
import com.sam_chordas.android.stockhawk.rest.HistoricalQuote;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
    static Context mContext;
  public static ArrayList<Entry> entryArrayList = new ArrayList<>();
    public static ArrayList<String> dateArray = new ArrayList<>();

    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        myReceiver = new MyReceiver();
        mContext = this;



        setContentView(R.layout.activity_line_graph);


        Intent intent = getIntent();

 //       Bundle bundle = intent.getExtras();
   //     HistoricalQuote item= bundle.getParcelable("entryList");


        /*
       HistoricalQuote item = intent.getExtras().getParcelable("HistoricalQuote");
        Log.v(LOG_TAG,"item in DetailActivity "+item);
      ArrayList<Entry> entryArrayList =new ArrayList<Entry>();
*/
        String symbol = intent.getStringExtra("symbol");
  //      entryArrayList = item.getEntryArrayList();
//        String url = HistUtils.buildHistoricalUrl(symbol);
        Log.v(LOG_TAG,"entryArrayList before asyncTask" +entryArrayList);

        //It will set value of entryArrayList through background network call
   //     new HistUtils().execute(url);

        name = intent.getStringExtra("Name");
        Log.v(LOG_TAG,"symbol,name"+symbol+name);
  //     HistoricalQuote item = new HistoricalQuote(entryArrayList);
        Log.v(LOG_TAG,"entryArrayList after asyncTask" +entryArrayList);


        chart = (LineChart) findViewById(R.id.linechart);
  //     PlotGraph(chart,entryArrayList,name);


        mServiceIntent = new Intent(this, StockIntentService.class);
        mServiceIntent.putExtra("tag", "history");
        mServiceIntent.putExtra("historicalSymbol", symbol);
       startService(mServiceIntent);



    }


    @Override
    public void onStop(){
        Log.v(LOG_TAG,"inside nStop");

        unregisterReceiver(myReceiver);
        super.onStop();
    }

    @Override
    public void onPause(){
        Log.v(LOG_TAG,"inside onPause");
        super.onPause();
    }

    public  void getDates(String symbol) {

        Cursor cursor = getContentResolver().query(QuoteProvider.History.CONTENT_URI,
                new String[]{HistoryColumns.DATE},
                HistoryColumns.SYMBOL + "= ?",
                new String[]{symbol},
                null);
   //     Log.v(LOG_TAG,"date cursor "+c);
        DatabaseUtils.dumpCursor(cursor);
  //      cursor.


        if (cursor.getCount() != 0){
            for (int i =0; i<cursor.getCount(); i++){
                String date = cursor.getString(i);
                dateArray.add(date.substring(8,10));
            }

        }

    }


    public static void PlotGraph(LineChart chart, ArrayList<Entry> entryArrayList,String name,
                                 ArrayList<String> dateArray) {



        LineDataSet entryDataSet = new LineDataSet(entryArrayList,name);
      //  entryDataSet.setAxisDependency();
        ArrayList<ILineDataSet> iLineDataSet = new ArrayList<ILineDataSet>();
        iLineDataSet.add(entryDataSet);


        ArrayList<String> xVals = HistUtils.concateDate(dateArray);



        LineData lineData = new LineData(xVals,iLineDataSet);
        chart.setData(lineData);
        chart.setDescription("Past 1 month Data");
        chart.invalidate();

    }
    @Override
    public void onResume(){
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StockTaskService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
    }


    public class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent){

           Bundle bundle = intent.getExtras();
           HistoricalQuote item= bundle.getParcelable("HistoricalQuote");
            String symbol = bundle.getString("symbol");
            StockDetailActivity.entryArrayList = item.getEntryArrayList();
            StockDetailActivity.dateArray = item.getDateArrayList();
            Log.v(LOG_TAG,"dateArrayList "+StockDetailActivity.dateArray);
            Log.v(LOG_TAG, "enterArrayList " + StockDetailActivity.entryArrayList);
         //   getDates(symbol);
            StockDetailActivity.PlotGraph(chart,StockDetailActivity.entryArrayList,name,StockDetailActivity.dateArray);

        }

    }


    }

