package com.example.newfido.authenticator.db;

import android.provider.BaseColumns;

/**
 * Created by sorin.teican on 02-Nov-16.
 */
 

public class CountersContract {

    private CountersContract() {}

    public static class CountersEntry implements BaseColumns {
        public static final String TABLE_NAME = "regcounters";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_CONTEXT = "context";
    }
}
