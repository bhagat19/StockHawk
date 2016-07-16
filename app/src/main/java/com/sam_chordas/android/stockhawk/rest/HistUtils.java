package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.data.HistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by amit on 26-06-2016.
 */
public class HistUtils  {

    final static String LOG_TAG = HistUtils.class.getSimpleName();
    public static StringBuilder historicalBuilder = new StringBuilder();
    public static OkHttpClient client = new OkHttpClient();
    public static ArrayList<Entry> arrayList = new ArrayList<>();

    public static String CLOSE_PRICE;

    public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(String JSON, Context context){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        ContentProviderOperation cpo;

        try {
            if (JSON != null) {
                JSONObject jsonObject = new JSONObject(JSON);
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results").
                            getJSONObject("quote");
                    cpo = buildBatchOperation(jsonObject, context);
                    batchOperations.add(cpo);
                } else {
                    JSONArray resultsArray = jsonObject.getJSONObject("results").
                            getJSONArray("quote");
                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            cpo = buildBatchOperation(jsonObject, context);
                            batchOperations.add(cpo);
                        }
                    }
                }
            }else
            {return null;}
        }catch (JSONException e){
        e.printStackTrace();
        }
        return batchOperations;
    }

    public static ArrayList<String> ComputeDateArrayForXvalues(String JSON){
        try {
            if (JSON != null) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                JSONObject jsonObject = new JSONObject(JSON);
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                Log.v(LOG_TAG, "count from HistUtil " + count);

                ArrayList<String> xVals = new ArrayList<String>(count);
                //        jsonObject = jsonObject.getJSONObject("results");
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results").
                            getJSONObject("quote");
                    String date = jsonObject.getString("Date");

                    xVals.add(date);
                } else {
                    JSONArray entryArray = jsonObject.getJSONObject("results").
                            getJSONArray("quote");

                    for (int i = 0; i < entryArray.length(); i++) {
                        String date = entryArray.getJSONObject(i).getString("Date");
                        xVals.add(date);
                    }

                }
                return xVals;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
         return null;

    }

    public static ArrayList<String> concateDate(ArrayList<String> dateArray) {
        if (!dateArray.isEmpty()) {
            ArrayList<String> concatedDateArrayList = new ArrayList<String>(dateArray.size());

            for (int i = dateArray.size() -1; i >0; i--) {
                String date = dateArray.get(i).substring(8, 10);
                concatedDateArrayList.add(date);
            }
            return concatedDateArrayList;
        }
        return null;
    }

    public static ArrayList<Entry> ComputeHistoricalData(String JSON) {


        try {
            if (JSON != null) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                JSONObject jsonObject = new JSONObject(JSON);
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                Log.v(LOG_TAG,"count from HistUtil "+count);

                ArrayList<Entry> yVals = new ArrayList<Entry>(count);
        //        jsonObject = jsonObject.getJSONObject("results");
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results").
                            getJSONObject("quote");
                    Entry entry = new Entry(Float.parseFloat(jsonObject.getString("Close")),
                            1);
                    yVals.add(entry);
                } else {
                    JSONArray entryArray = jsonObject.getJSONObject("results").
                            getJSONArray("quote");

                    for (int i= entryArray.length() -1; i>0; i--){
                 //       Date date = new Date();
                        try {

                           String date = entryArray.getJSONObject(i).getString("Date");
                            Date entryDate = formatter.parse(date);
                     //       Log.v(LOG_TAG,"entryDate "+Integer.parseInt(entryDate.toString()));
                            long time = (entryDate.getTime())/(long) 1000;

                       //     entryDate.getTime();
                       //     long longDate =
                            Entry entry = new Entry(Float.parseFloat(entryArray.getJSONObject(i).getString("Close")),
                                    entryArray.length()-1-i);
                            yVals.add(entry);

                        }catch(ParseException | NumberFormatException pe){
                            pe.printStackTrace();
                        }
                    }
                }
             return yVals;
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, Context context) throws JSONException{
/*
        ContentProviderOperation.Builder dBuilder = ContentProviderOperation.newDelete(
                QuoteProvider.History.CONTENT_URI);

        dBuilder.withValue(HistoryColumns.CLOSEPRICE, jsonObject.getString("Close"));
        dBuilder.withValue(HistoryColumns.DATE, jsonObject.getString("Date"));
        dBuilder.withValue(HistoryColumns.SYMBOL, jsonObject.getString("Symbol"));

        */


        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
               QuoteProvider.History.CONTENT_URI);


        if (jsonObject != null)
        {
            builder.withValue(HistoryColumns.CLOSEPRICE, jsonObject.getString("Close"));
            builder.withValue(HistoryColumns.DATE, jsonObject.getString("Date"));
            builder.withValue(HistoryColumns.SYMBOL, jsonObject.getString("Symbol"));
        }


        return builder.build();
    }

    public static String buildHistoricalUrl(String symbol){

        historicalBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
        try {
            historicalBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                    + "in ( \'" + symbol + "\') and startDate= \'2016-06-25\' and endDate=\'2016-06-30\'", "UTF-8"));
            //   historicalBuilder.append(URLEncoder.encode("\""+symbol+"\")", "UTF-8"));


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        historicalBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");
        Log.v(LOG_TAG, "historicalUrl " + historicalBuilder.toString());

        return historicalBuilder.toString();
    }



}