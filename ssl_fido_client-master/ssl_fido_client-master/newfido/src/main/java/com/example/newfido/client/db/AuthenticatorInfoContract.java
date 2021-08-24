package com.example.newfido.client.db;

import android.provider.BaseColumns;

/**
 * Created by sorin.teican on 20-Feb-17.
 */
 

public class AuthenticatorInfoContract {

    private AuthenticatorInfoContract() {}

    public static class AuthenticatorInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "authenticatorinfo";
        public static final String COLUMN_NAME_AUTHINFO = "authinfo";
    }
}
