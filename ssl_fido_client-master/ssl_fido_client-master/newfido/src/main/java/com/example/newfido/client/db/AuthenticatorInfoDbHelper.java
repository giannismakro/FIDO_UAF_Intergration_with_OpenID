package com.example.newfido.client.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sorin.teican on 20-Feb-17.
 */
 

public class AuthenticatorInfoDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AuthenticatorInfo.db";

    private static final String SQL_CREATE_AUTHINFO_TABLE =
            "CREATE TABLE " + AuthenticatorInfoContract.AuthenticatorInfoEntry.TABLE_NAME + " (" +
                    AuthenticatorInfoContract.AuthenticatorInfoEntry._ID + " INTEGER PRIMARY KEY, " +
                    AuthenticatorInfoContract.AuthenticatorInfoEntry.COLUMN_NAME_AUTHINFO + " TEXT )";

    private static final String SQL_DELETE_AUTHINFO_TABLE =
            "DROP TABLE IF EXISTS " + AuthenticatorInfoContract.AuthenticatorInfoEntry.TABLE_NAME;

    public AuthenticatorInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_AUTHINFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_AUTHINFO_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
