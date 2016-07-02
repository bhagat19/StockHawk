package com.sam_chordas.android.stockhawk.rest;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class HistoricalQuote implements Parcelable {

    ArrayList<Entry> entryArrayList = new ArrayList<>();

    public HistoricalQuote(ArrayList<Entry> entryArrayList){
        this.entryArrayList = entryArrayList;

    }

    public ArrayList<Entry> getEntryArrayList() {
        return entryArrayList;
    }

    protected HistoricalQuote(Parcel in) {
        if (in.readByte() == 0x01) {
            entryArrayList = new ArrayList<Entry>();
            in.readList(entryArrayList, Entry.class.getClassLoader());
        } else {
            entryArrayList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (entryArrayList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(entryArrayList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<HistoricalQuote> CREATOR = new Parcelable.Creator<HistoricalQuote>() {
        @Override
        public HistoricalQuote createFromParcel(Parcel in) {
            return new HistoricalQuote(in);
        }

        @Override
        public HistoricalQuote[] newArray(int size) {
            return new HistoricalQuote[size];
        }
    };
}
