package com.example.newfido.client.db.ops;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.example.newfido.client.db.AuthenticatorInfoContract;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sorin.teican on 20-Feb-17.
 */
 

public class AuthenticatorInfoOps {

    private static long insertAuthenticatorInfo(final SQLiteOpenHelper _db, final AuthenticatorInfo info) {
        SQLiteDatabase db = _db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AuthenticatorInfoContract.AuthenticatorInfoEntry.COLUMN_NAME_AUTHINFO, new Gson().toJson(info));

        return db.insert(AuthenticatorInfoContract.AuthenticatorInfoEntry.TABLE_NAME, null, values);
    }

    private static List<AuthenticatorInfo> getAllAuthenticatorsInfo(final SQLiteOpenHelper _db) {
        SQLiteDatabase db = _db.getReadableDatabase();

        String[] projection = { AuthenticatorInfoContract.AuthenticatorInfoEntry.COLUMN_NAME_AUTHINFO };

        Cursor c = db.query(
                AuthenticatorInfoContract.AuthenticatorInfoEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<AuthenticatorInfo> authenticatorsInfo = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                AuthenticatorInfo info = new Gson().fromJson(
                        c.getString(c.getColumnIndex(AuthenticatorInfoContract.AuthenticatorInfoEntry.COLUMN_NAME_AUTHINFO)), AuthenticatorInfo.class);
                authenticatorsInfo.add(info);
            } while (c.moveToNext());
        }
		
		c.close();

        return authenticatorsInfo;
    }

    public static class InsertAuthenticatorInfoOp extends AsyncTask<AuthenticatorInfo, Void, Long> {
        private SQLiteOpenHelper _db;
        private boolean _done;
        private Long _result;

        public InsertAuthenticatorInfoOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Long doInBackground(AuthenticatorInfo... params) {
            Long id = insertAuthenticatorInfo(_db, params[0]);
            _result = id;
            _done = true;
            return id;
        }

        @Override
        protected void onPostExecute(Long result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() {
            return _done;
        }

        public long getResult() {
            return _result;
        }
    }

    public static class GetAllAuthenticatorsInfoOp extends AsyncTask<Void, Void, List<AuthenticatorInfo>> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private List<AuthenticatorInfo> _result;

        public GetAllAuthenticatorsInfoOp(SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected List<AuthenticatorInfo> doInBackground(Void... params) {
            _result = getAllAuthenticatorsInfo(_db);
            _done = true;
            return _result;
        }

        @Override
        protected void onPostExecute(List<AuthenticatorInfo> result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() {
            return _done;
        }

        public List<AuthenticatorInfo> getResult() {
            return _result;
        }
    }

}
