package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by amit on 26-06-2016.
 */
public class HistoryColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DATE = "date";
    @DataType(DataType.Type.TEXT) @References(table = QuoteDatabase.QUOTES,column = QuoteColumns.SYMBOL) @NotNull
    public static final String SYMBOL = "symbol";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String CLOSEPRICE = "close_price";
}
