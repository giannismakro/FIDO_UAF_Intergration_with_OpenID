package com.example.newfido.asm.db.ops;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;


import java.util.ArrayList;
import java.util.List;

import com.example.newfido.asm.db.KeyHandleContract;
import com.example.newfido.asm.db.models.KeyHandle;


/**
 * Created by sorin.teican on 12-Jan-17.
 */
 

public class KeyHandleOps {

    private static long insertKeyHandle(final SQLiteOpenHelper _db, final KeyHandle handle) {
        SQLiteDatabase db = _db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID, handle.CallerID);
        values.put(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID, handle.AppID);
        values.put(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYHANDLE, handle.TAG_KEYHANDLE);
        values.put(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID, handle.TAG_KEYID);
        values.put(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CURRENT_TIMESTAMP, handle.CurrentTimestamp);

        return db.insert(KeyHandleContract.KeyHandleEntry.TABLE_NAME, null, values);
    }

    private static KeyHandle getKeyHandle(final SQLiteOpenHelper _db, final String AppID, final String KeyID) {
        SQLiteDatabase db = _db.getWritableDatabase();

        String[] projection = {
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID,
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID,
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYHANDLE,
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID,
        };

        String selection = KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID + " = ? AND " +
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID + " = ?";
        String[] selectionArgs = {AppID, KeyID};

        Cursor c = db.query(
                KeyHandleContract.KeyHandleEntry.TABLE_NAME,  // The table to query.
                projection,                                 // The columns to return.
                selection,                                  // The columns for the WHERE clause.
                selectionArgs,                              // The values for the WHERE clause.
                null,                                       // don`t group the rows.
                null,                                       // don`t filter by row groups.
                null                                        // the sort order.
        );

        if (!c.moveToFirst())
            return null;

        KeyHandle keyHandle = new KeyHandle();
        keyHandle.CallerID = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID));
        keyHandle.AppID = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID));
        keyHandle.TAG_KEYHANDLE = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYHANDLE));
        keyHandle.TAG_KEYID = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID));
        keyHandle.CurrentTimestamp = c.getInt(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID));
		
		c.close();

        return keyHandle;
    }

    private static List<KeyHandle> getAllKeyHandles(final SQLiteOpenHelper _db) {
        SQLiteDatabase db = _db.getWritableDatabase();

        String[] projection = {
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID,
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID,
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYHANDLE,
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID,
        };

        Cursor c = db.query(
                KeyHandleContract.KeyHandleEntry.TABLE_NAME,  // The table to query.
                projection,                                 // The columns to return.
                null,                                  // The columns for the WHERE clause.
                null,                              // The values for the WHERE clause.
                null,                                       // don`t group the rows.
                null,                                       // don`t filter by row groups.
                null                                        // the sort order.
        );

        List<KeyHandle> keyHandles = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                KeyHandle keyHandle = new KeyHandle();
                keyHandle.CallerID = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID));
                keyHandle.AppID = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID));
                keyHandle.TAG_KEYHANDLE = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYHANDLE));
                keyHandle.TAG_KEYID = c.getString(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID));
                keyHandle.CurrentTimestamp = c.getInt(c.getColumnIndex(KeyHandleContract.KeyHandleEntry.COLUMN_NAME_CALLERID));

                keyHandles.add(keyHandle);
            } while (c.moveToNext());
        }
		
		c.close();

        return keyHandles;
    }

    private static void deleteKeyHandle(final SQLiteOpenHelper _db, final String AppID, final String KeyID) {
        SQLiteDatabase db = _db.getWritableDatabase();

        String selection = KeyHandleContract.KeyHandleEntry.COLUMN_NAME_APPID + " = ? AND " +
                KeyHandleContract.KeyHandleEntry.COLUMN_NAME_TAG_KEYID + " = ?";
        String[] selectionArgs = {AppID, KeyID};

        db.delete(KeyHandleContract.KeyHandleEntry.TABLE_NAME, selection, selectionArgs);
    }

    public static class InsertKeyHandleOp extends AsyncTask<KeyHandle, Void, Long> {
        private SQLiteOpenHelper _db;
        private boolean _done;
        private Long _result;

        public InsertKeyHandleOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Long doInBackground(KeyHandle... params) {
            Long id = insertKeyHandle(_db, params[0]);
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

    public static class GetKeyHandleOp extends AsyncTask<String, Void, KeyHandle> {

        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private KeyHandle _result = null;

        public GetKeyHandleOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected KeyHandle doInBackground(String... params) {
            _result = getKeyHandle(_db, params[0], params[1]);
            _done = true;
            return _result;
        }

        @Override
        protected void onPostExecute(KeyHandle result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() { return _done; }

        public KeyHandle getResult() { return _result; }
    }

    public static class GetAllKeyHandlesOp extends AsyncTask<Void, Void, List<KeyHandle>> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private List<KeyHandle> _result = null;

        public GetAllKeyHandlesOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected List<KeyHandle> doInBackground(Void... params) {
            _result = getAllKeyHandles(_db);
            _done = true;
            return _result;
        }

        @Override
        protected void onPostExecute(List<KeyHandle> result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() { return _done; }

        public List<KeyHandle> getResult() { return _result; }
    }

    public static class DeleteKeyHandleOp extends AsyncTask<String, Void, Void> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;

        public DeleteKeyHandleOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Void doInBackground(String... params) {
            deleteKeyHandle(_db, params[0], params[1]);
            _done = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            _done = true;
        }

        public boolean isDone() { return _done; }
    }
}
