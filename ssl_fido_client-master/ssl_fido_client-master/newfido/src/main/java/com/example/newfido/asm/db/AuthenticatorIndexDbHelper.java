package com.example.newfido.asm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sorin.teican on 12-Jan-17.
 */
 

public class AuthenticatorIndexDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AuthenticatorIndex.db";

    private static final String TEXT_TYPE = "VARCHAR(255)";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_INDEX_TABLE =
            "CREATE TABLE " + AuthenticatorIndexContract.AuthenticatorIndexEntry.TABLE_NAME + " (" +
                    AuthenticatorIndexContract.AuthenticatorIndexEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP + " " +
                    AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID + " " + TEXT_TYPE + COMMA_SEP + " " +
                    AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_INDEX + " INTEGER" + COMMA_SEP + " " +
                    AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_TYPE + " INTEGER" +  ");";

    private static final String SQL_DELETE_INDEX_TABLE =
            "DROP TABLE IF EXISTS " + AuthenticatorIndexContract.AuthenticatorIndexEntry.TABLE_NAME;

    public AuthenticatorIndexDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INDEX_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_INDEX_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
