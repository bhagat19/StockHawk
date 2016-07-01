package com.sam_chordas.android.stockhawk.rest;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by amit on 30-06-2016.
 */
public class HistoricalQuote implements Parcelable {

    ArrayList<Entry> entryArrayList = new ArrayList<>();

    public HistoricalQuote(ArrayList<Entry> entryArrayList){
        this.entryArrayList = entryArrayList;

    }


    protected HistoricalQuote(Parcel in) {
        entryArrayList = (ArrayList) in.readValue(ArrayList.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(entryArrayList);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<HistoricalQuote> CREATOR = new Parcelable.Creator<HistoricalQuote>() {
        @Override
        public HistoricalQuote createFromParcel(Parcel in) {
            return new HistoricalQuote(in);
        }

        @Override
        public HistoricalQuote[] newArray(int size) {return new HistoricalQuote[size];
        }
    };
}
