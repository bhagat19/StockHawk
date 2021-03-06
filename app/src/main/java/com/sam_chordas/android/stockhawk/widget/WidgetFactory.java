package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.mikephil.charting.data.Entry;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.HistUtils;
import com.sam_chordas.android.stockhawk.rest.HistoricalQuote;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.ArrayList;

/**
 * Created by amit on 04-07-2016.
 */
public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    final String LOG_TAG = WidgetFactory.class.getSimpleName();
    Intent mServiceIntent;
    Cursor mCursor;
    Context mContext;
    Intent mIntent;
    ArrayList<Entry>  entryArrayList = new ArrayList<>();

    static final String[] STOCK_COLUMNS ={
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISCURRENT,
            QuoteColumns.NAME,
            QuoteColumns.ISUP
    };

    static final int INDEX_QUOTE_ID=0;
    static final int INDEX_QUOTE_SYMBOL =1;
    static final int INDEX_QUOTE_BIDPRICE =2;
    static final int INDEX_QUOTE_PERCENT_CHANGE =3;
    static final int INDEX_QUOTE_CHANGE =4;
    static final int INDEX_QUOTE_ISCURRENT =5;
    static final int INDEX_QUOTE_NAME=6;
    static final int INDEX_QUOTE_ISUP=7;

    public WidgetFactory(Context context, Intent intent){
        this.mContext = context;
        this.mIntent = intent;
    }
    @Override
    public void onDataSetChanged(){
        if (mCursor != null){
            mCursor.close();
        }
        //http://stackoverflow.com/questions/13187284/android-permission-denial-in-widget-remoteviewsfactory-for-content
        final long token = Binder.clearCallingIdentity();
        try {
             mCursor = mContext.getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    STOCK_COLUMNS,
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }


    };


    @Override
    public void onCreate(){};

    @Override
    public int getCount(){
        return mCursor == null ? 0 : mCursor.getCount();

    }

    @Override
    public int getViewTypeCount(){
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position){
        if (position == AdapterView.INVALID_POSITION ||
            mCursor == null || !mCursor.moveToPosition(position)) {
        return null;
    }
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        if(mCursor.moveToPosition(position)) {

            String symbol = mCursor.getString(INDEX_QUOTE_SYMBOL);
            String bidPrice = mCursor.getString(INDEX_QUOTE_BIDPRICE);
            String change = mCursor.getString(INDEX_QUOTE_CHANGE);
            Log.v(LOG_TAG,"data "+symbol+bidPrice+change);

            remoteViews.setTextViewText(R.id.stock_symbol, symbol);
            remoteViews.setTextViewText(R.id.bid_price, bidPrice);
            remoteViews.setTextViewText(R.id.change, change);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                remoteViews.setContentDescription(R.id.stock_symbol, mContext.getString(R.string.a11y_stock_symbol, symbol));
                remoteViews.setContentDescription(R.id.bid_price, mContext.getString(R.string.a11y_bid_price, bidPrice));
                remoteViews.setContentDescription(R.id.change, mContext.getString(R.string.a11y_change, change));
            }

            if (mCursor.getString(INDEX_QUOTE_ISUP).equals("1")) {
                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }






        //    entryArrayList = HistUtils.buildHistoricalUrl(mCursor.getString(INDEX_QUOTE_SYMBOL));
          //  HistoricalQuote item = new HistoricalQuote(entryArrayList);
            Intent newIntent = new Intent();

    //        newIntent.putExtra("tag", "history");
     //       newIntent.putExtra("historicalSymbol", symbol);


            newIntent.putExtra("symbol", mCursor.getString(INDEX_QUOTE_SYMBOL));
            newIntent.putExtra("Name", mCursor.getString(INDEX_QUOTE_NAME));

         //   newIntent.putExtra("entryList",item);
          //  newIntent.setAction(StockTaskService.MY_ACTION);
           // mContext.sendBroadcast(newIntent);



    //        newIntent.putExtra("tag", "history");
    //        mServiceIntent.putExtra("historicalSymbol", symbol);
            Log.v(LOG_TAG, "symbol, Name" + mCursor.getString(INDEX_QUOTE_SYMBOL) + mCursor.getString(INDEX_QUOTE_NAME));
            // In setOnClickFillIntent method, the ID to be passed is of the Rootview
            // of the layout passed in the remote view - above, i.e. rootview of the list_item_quote.
            remoteViews.setOnClickFillInIntent(R.id.widget_list_item, newIntent);
      //      mContext.startActivity(newIntent);


        }

        return  remoteViews;

    }



    @Override
    public void onDestroy(){
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public RemoteViews getLoadingView(){ return  null;
    }

    @Override
    public long getItemId(int position) {
        if (mCursor.moveToPosition(position))
            return mCursor.getLong(INDEX_QUOTE_ID);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


}
