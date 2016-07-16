package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.HistUtils;
import com.sam_chordas.android.stockhawk.rest.HistoricalQuote;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;
  public  final static String MY_ACTION = "com.sam_chordas.android.stockhawk.ui.StockDetailActivity.HistQuote";
    //fetching data for past 30 days
    public static int DAYS_TO_FETCH_DATA_FOR = 30;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }

  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  @Override
  public int onRunTask(TaskParams params) {
      Cursor initQueryCursor;
      if (mContext == null) {
          mContext = this;
      }
      StringBuilder urlStringBuilder = new StringBuilder();
      StringBuilder historicalBuilder = new StringBuilder();
      try {
          // Base URL for the Yahoo query
          urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
          historicalBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
          urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                  + "in (", "UTF-8"));
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }
      if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
          isUpdate = true;
          initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                  new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                  null, null);
          if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
              // Init task. Populates DB with quotes for the symbols seen below
              try {
                  urlStringBuilder.append(
                          URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
              } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
              }
          } else if (initQueryCursor != null) {
              DatabaseUtils.dumpCursor(initQueryCursor);
              initQueryCursor.moveToFirst();
              for (int i = 0; i < initQueryCursor.getCount(); i++) {
                  mStoredSymbols.append("\"" +
                          initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                  initQueryCursor.moveToNext();
              }
              mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
              try {
                  urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
              } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
              }
          }
      } else if (params.getTag().equals("add")) {
          isUpdate = false;
          // get symbol from params.getExtra and build query
          String stockInput = params.getExtras().getString("symbol");

          try {
              urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
          }
      }
      //for historical data
      else if (params.getTag().equals("history")) {
          isUpdate = false;


          String symbol = params.getExtras().getString("symbol");
          Log.v(LOG_TAG, "inside history tag " + symbol);

          try {
              DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
              Date todayDateObj = new Date();


              Calendar c = Calendar.getInstance();
              c.setTime(todayDateObj);
              c.add(Calendar.DATE, -DAYS_TO_FETCH_DATA_FOR);
              Date start = c.getTime();


              String todayDate = df.format(todayDateObj).toString();
              String startDate = df.format(start).toString();

              historicalBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                      + "in ( \'" + symbol + "\') and startDate =  \'"+startDate+"\' and endDate=\'"+todayDate+"\'", "UTF-8"));
              //   historicalBuilder.append(URLEncoder.encode("\""+symbol+"\")", "UTF-8"));


          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
          }
          historicalBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                  + "org%2Falltableswithkeys&callback=");
          Log.v(LOG_TAG, "historicalUrl " + historicalBuilder.toString());

          String histUrlString;
          String hResponse;
          int hResult = GcmNetworkManager.RESULT_FAILURE;
          if (historicalBuilder != null) {
              histUrlString = historicalBuilder.toString();
              try {
                  hResponse = fetchData(histUrlString);
                  Log.v(LOG_TAG, "response from Api " + hResponse);
                  hResult = GcmNetworkManager.RESULT_SUCCESS;

                  ArrayList<Entry> arrayEntry = HistUtils.ComputeHistoricalData(hResponse);
                  ArrayList<String> dateArrayList = HistUtils.ComputeDateArrayForXvalues(hResponse);
                  Log.v(LOG_TAG,"inside Service "+arrayEntry);
                  Log.v(LOG_TAG,"inside Service "+dateArrayList);

                //parcelable HistoricalQuote item to send data to StockDetailActivity
                  HistoricalQuote item = new HistoricalQuote(arrayEntry,dateArrayList);
                  if (arrayEntry != null && arrayEntry.size() != 0 ){
                      Intent intent = new Intent();
                      intent.setAction(MY_ACTION);
             //         intent.putExtra("symbol",symbol);
                      intent.putExtra("HistoricalQuote", item);
                      mContext.sendBroadcast(intent);
                  }

                  ArrayList<ContentProviderOperation> contentProviderOperations = HistUtils.
                          quoteJsonToContentVals(hResponse, mContext);
                  try {
                      if (contentProviderOperations != null && contentProviderOperations.size() != 0) {
                          mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                  contentProviderOperations);
                        //Broadcast for widget receiver
                          Intent dataUpdatedIntent = new Intent(QuoteCursorAdapter.ACTION_DATA_UPDATED).setPackage(mContext.getPackageName());
                          mContext.sendBroadcast(dataUpdatedIntent);
                      }
                  } catch (RemoteException | OperationApplicationException e) {
                      e.printStackTrace();
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
              return hResult;

          }
      }

          // finalize the URL for the API query.
          urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                  + "org%2Falltableswithkeys&callback=");

          String urlString;
          String getResponse;
          int result = GcmNetworkManager.RESULT_FAILURE;

          if (urlStringBuilder != null) {
              urlString = urlStringBuilder.toString();
              Log.v(LOG_TAG, "url " + urlString);
              try {
                  getResponse = fetchData(urlString);
                  Log.v(LOG_TAG, "response " + getResponse);
                  result = GcmNetworkManager.RESULT_SUCCESS;
                  try {
                      ContentValues contentValues = new ContentValues();
                      // update ISCURRENT to 0 (false) so new data is current
                      if (isUpdate) {
                          contentValues.put(QuoteColumns.ISCURRENT, 0);
                          mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                  null, null);
                      }
                      ArrayList<ContentProviderOperation> contentProviderOperations = Utils.quoteJsonToContentVals(getResponse, mContext);

                      if (contentProviderOperations != null && contentProviderOperations.size() != 0) {
                          mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                  contentProviderOperations);
                      } else {
                          //stock does not exist
                          Intent intent = new Intent();
                          intent.setAction("com.sam_chordas.android.stockhawk.ui.MyStocksActivity.STOCK_NOT_FOUND");
                          mContext.sendBroadcast(intent);

                      }


                  } catch (RemoteException | OperationApplicationException e) {
                      Log.e(LOG_TAG, "Error applying batch insert", e);
                  }
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }

          return result;
      }}


