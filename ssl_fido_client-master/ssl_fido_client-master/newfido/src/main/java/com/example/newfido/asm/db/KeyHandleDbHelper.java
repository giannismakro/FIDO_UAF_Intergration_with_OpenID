package com.example.newfido.asm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sorin.teican on 12-Jan-17.
 */
 

public class KeyHandleDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "KeyHandle.db";

    private static final String TEXT_TYPE = "VARCHAR(255)";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_KEYHANDLE_TABLE =
            "CREATE TABLE " + KeyHandleContract.KeyHandleEntry.TABLE_NAME + " (" +
                    KeyHandleContract.KeyHandleEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP + " " +
                    KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID + " TEXT" + COMMA_SEP + " " +
                    KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID + " TEXT" + COMMA_SEP + " " +
                    KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYHANDLE + " TEXT" + COMMA_SEP + " " +
                    KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID + " TEXT" + COMMA_SEP + " " +
                    KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CURRENT_TIMESTAMP + " INTEGER" + " )";

    private static final String SQL_DELETE_KEYHANDLE_TABLE =
            "DROP TABLE IF EXISTS " + KeyHandleContract.KeyHandleEntry.TABLE_NAME;

    public KeyHandleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_KEYHANDLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_KEYHANDLE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
