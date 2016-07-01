package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.DropBoxManager;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.sam_chordas.android.stockhawk.data.HistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
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
                                    Integer.parseInt(String.valueOf(time)));
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

}
