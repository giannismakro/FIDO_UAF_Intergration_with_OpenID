package com.example.newfido.asm.db;

import android.provider.BaseColumns;

/**
 * Created by sorin.teican on 12-Jan-17.
 */
 

public class KeyHandleContract {

    private KeyHandleContract() {}

    public static class KeyHandleEntry implements BaseColumns {
        public static final String TABLE_NAME = "keyhandle";
        public static final String COLUMN_NAME_CALLERID = "callerid";
        public static final String COLUMN_NAME_APPID = "appid";
        public static final String COLUMN_NAME_TAG_KEYHANDLE = "keyhandle";
        public static final String COLUMN_NAME_TAG_KEYID = "keyid";
        public static final String COLUMN_NAME_CURRENT_TIMESTAMP = "currenttimestamp";
    }
}
