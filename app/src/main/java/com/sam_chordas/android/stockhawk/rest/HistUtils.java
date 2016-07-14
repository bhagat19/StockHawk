package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.DropBoxManager;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.data.HistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
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
public class HistUtils {

    final static String LOG_TAG = HistUtils.class.getSimpleName();
    public static StringBuilder historicalBuilder = new StringBuilder();
    public static OkHttpClient client = new OkHttpClient();

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
                            Integer.parseInt(jsonObject.getString("Date")));
                    yVals.add(entry);
                } else {
                    JSONArray entryArray = jsonObject.getJSONObject("results").
                            getJSONArray("quote");

                    for (int i=0; i<entryArray.length(); i++){
                 //       Date date = new Date();
                        try {

                            String date = entryArray.getJSONObject(i).getString("Date");
                            Date entryDate = formatter.parse(date);
                     //       Log.v(LOG_TAG,"entryDate "+Integer.parseInt(entryDate.toString()));
                            long time = (entryDate.getTime())/(long) 1000;

                       //     entryDate.getTime();
                       //     long longDate =
                            Entry entry = new Entry(Float.parseFloat(entryArray.getJSONObject(i).getString("Close")),
                                    i);
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

    public static ArrayList<Entry> buildHistoricalUrl(String symbol) {

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

        String histUrlString;

        int hResult = GcmNetworkManager.RESULT_FAILURE;
        if (historicalBuilder != null) {
            String hResponse;
            histUrlString = historicalBuilder.toString();
            try {
                hResponse = fetchData(histUrlString);
                Log.v(LOG_TAG, "response from Api within HistUtils " + hResponse);
        //        hResult = GcmNetworkManager.RESULT_SUCCESS;
                ComputeHistoricalData(hResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }






    public static String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}