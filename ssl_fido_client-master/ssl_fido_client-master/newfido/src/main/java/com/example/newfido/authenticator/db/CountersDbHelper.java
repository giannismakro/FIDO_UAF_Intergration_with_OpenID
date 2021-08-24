package com.example.newfido.authenticator.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sorin.teican on 02-Nov-16.
 */


public class CountersDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Counters.db";

    private static final String TEXT_TYPE = "VARCHAR(255)";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_COUNTERS_TABLE =
            "CREATE TABLE " + CountersContract.CountersEntry.TABLE_NAME + " (" +
                    CountersContract.CountersEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP + " " +
                    CountersContract.CountersEntry.COLUMN_NAME_TYPE + " " + TEXT_TYPE + COMMA_SEP + " " +
                    CountersContract.CountersEntry.COLUMN_NAME_VALUE + " INTEGER" + COMMA_SEP + " " +
                    CountersContract.CountersEntry.COLUMN_NAME_CONTEXT + " " + TEXT_TYPE + " )";

    private static final String SQL_DELETE_COUNTERS_TABLE =
            "DROP TABLE IF EXISTS " + CountersContract.CountersEntry.TABLE_NAME;

    public CountersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_COUNTERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_COUNTERS_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
