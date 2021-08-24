package com.example.newfido.asm.db;

import android.provider.BaseColumns;

/**
 * Created by sorin.teican on 12-Jan-17.
 */
 
public class AuthenticatorIndexContract {

    private AuthenticatorIndexContract() {}

    public static class AuthenticatorIndexEntry implements BaseColumns {
        public static final String TABLE_NAME = "authenticatorindex";
        public static final String COLUMN_NAME_AAID = "aaid";
        public static final String COLUMN_NAME_INDEX = "indx";
        public static final String COLUMN_NAME_TYPE = "type";
    }
}
