package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;
 //  public static Context context;

    public static String BID = "Bid";
    public static String CHANGE ="Change";


  public static ArrayList quoteJsonToContentVals(String JSON, Context context){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
      ContentProviderOperation cpo;

      try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
            cpo = buildBatchOperation(jsonObject, context);
            if (cpo != null)
          batchOperations.add(cpo);
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
                cpo = buildBatchOperation(jsonObject,context);
                if (cpo != null)
              batchOperations.add(cpo);
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, Context context) throws JSONException{
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);

      Boolean bool;
      Log.v(LOG_TAG,"jsonObject "+jsonObject);
      Log.v(LOG_TAG, "bool " + (jsonObject.getString(BID).equals("null") && jsonObject.getString(CHANGE).equals("null")));


      if (!jsonObject.getString(BID).equals("null") && !jsonObject.getString(CHANGE).equals("null")) {

          String change = jsonObject.getString(CHANGE);
          builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
          builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
          builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
          builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                  jsonObject.getString("ChangeinPercent"), true));
          builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
          builder.withValue(QuoteColumns.ISCURRENT, 1);
          if (change.charAt(0) == '-') {
              builder.withValue(QuoteColumns.ISUP, 0);
          } else {
              builder.withValue(QuoteColumns.ISUP, 1);
          }
      }
          else {
    //      MyStocksActivity activity = new MyStocksActivity();
     //     activity.showToast("This stock does not exist");
        //  Toast.makeText(context, "This stock does not exist", Toast.LENGTH_SHORT).show();
          return null;
      }


      return builder.build();
  }
}
