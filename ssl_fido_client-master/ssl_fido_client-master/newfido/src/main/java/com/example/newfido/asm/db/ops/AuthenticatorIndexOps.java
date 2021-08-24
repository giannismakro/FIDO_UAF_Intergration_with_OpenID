package com.example.newfido.asm.db.ops;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;


import java.util.ArrayList;
import java.util.List;

import com.example.newfido.asm.db.AuthenticatorIndexContract;
import com.example.newfido.asm.db.models.AuthenticatorIndex;


/**
 * Created by sorin.teican on 12-Jan-17.
 */
 
public class AuthenticatorIndexOps {

    private static long insertAuthenticatorIndex(final SQLiteOpenHelper _db, final AuthenticatorIndex authenticatorIndex) {
        SQLiteDatabase db = _db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID, authenticatorIndex.AAID);
        values.put(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_INDEX, authenticatorIndex.index);
        values.put(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_TYPE, authenticatorIndex.type);

        return db.insert(AuthenticatorIndexContract.AuthenticatorIndexEntry.TABLE_NAME, null, values);
    }

    private static AuthenticatorIndex getAuthenticatorIndex(final SQLiteOpenHelper _db, final String AAID) {
        SQLiteDatabase db = _db.getReadableDatabase();

        String[] projection = {
                AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID,
                AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_INDEX,
                AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_TYPE,
        };

        String selection = AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID + " = ?";
        String[] selectionArgs = { AAID };

        Cursor c = db.query(
                AuthenticatorIndexContract.AuthenticatorIndexEntry.TABLE_NAME,  // The table to query.
                projection,                                 // The columns to return.
                selection,                                  // The columns for the WHERE clause.
                selectionArgs,                              // The values for the WHERE clause.
                null,                                       // don`t group the rows.
                null,                                       // don`t filter by row groups.
                null                                        // the sort order.
        );

        if (!c.moveToFirst())
            return null;

        AuthenticatorIndex index = new AuthenticatorIndex();
        index.AAID = AAID;
        index.index = c.getInt(c.getColumnIndex(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_INDEX));
        index.type = c.getInt(c.getColumnIndex(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_TYPE));

        c.close();

        return index;
    }

    private static List<AuthenticatorIndex> getAllIndexes(final SQLiteOpenHelper _db) {
        SQLiteDatabase db = _db.getReadableDatabase();

        String[] projection = {
                AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID,
                AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_INDEX,
                AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_TYPE,
        };

        Cursor c = db.query(
                AuthenticatorIndexContract.AuthenticatorIndexEntry.TABLE_NAME,  // The table to query.
                projection,                                 // The columns to return.
                null,                                  // The columns for the WHERE clause.
                null,                              // The values for the WHERE clause.
                null,                                       // don`t group the rows.
                null,                                       // don`t filter by row groups.
                null                                        // the sort order.
        );

        List<AuthenticatorIndex> indexes = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                AuthenticatorIndex index = new AuthenticatorIndex();
                index.AAID = c.getString(c.getColumnIndex(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID));
                index.index = c.getInt(c.getColumnIndex(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_INDEX));
                index.type = c.getInt(c.getColumnIndex(AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_TYPE));

                indexes.add(index);
            } while (c.moveToNext());
        }
		
		c.close();

        return indexes;
    }

    private static void deleteIndex(final SQLiteOpenHelper _db, final String AAID) {
        SQLiteDatabase db = _db.getWritableDatabase();

        String selection = AuthenticatorIndexContract.AuthenticatorIndexEntry.COLUMN_NAME_AAID + " LIKE ?";
        String[] selectionArgs = { AAID };

        db.delete(AuthenticatorIndexContract.AuthenticatorIndexEntry.TABLE_NAME, selection, selectionArgs);
    }

    public static class InsertIndexOp extends AsyncTask<AuthenticatorIndex, Void, Long> {

        private SQLiteOpenHelper _db;
        private boolean _done;
        private Long _result;

        public InsertIndexOp(final SQLiteOpenHelper db) {
            _db = db;
        }



        @Override
        protected Long doInBackground(AuthenticatorIndex... params) {
            Long id = insertAuthenticatorIndex(_db, params[0]);
            _result = id;
            _done = true;
            return id;
        }

        @Override
        protected void onPostExecute(Long result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() { return _done; }

        public long getResult() { return _result; }
    }

    public static class GetIndexOp extends AsyncTask<String, Void, AuthenticatorIndex> {

        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private AuthenticatorIndex _result = null;

        public GetIndexOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected AuthenticatorIndex doInBackground(String... params) {
            _done = true;
            return null;
        }

        @Override
        protected void onPostExecute(AuthenticatorIndex result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() { return _done; }

        public AuthenticatorIndex getResult() { return _result; }
    }

    public static class GetAllIndexesOp extends AsyncTask<Void, Void, List<AuthenticatorIndex>> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private List<AuthenticatorIndex> _result = null;

        public GetAllIndexesOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected List<AuthenticatorIndex> doInBackground(Void... params) {
            _result = getAllIndexes(_db);
            _done = true;
            return _result;
        }

        @Override
        protected void onPostExecute(List<AuthenticatorIndex> result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() {
            return _done;
        }

        public List<AuthenticatorIndex> getResult() {
            return _result;
        }
    }

    public static class DeleteIndexOp extends AsyncTask<String, Void, Void> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;

        public DeleteIndexOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Void doInBackground(String... params) {
            deleteIndex(_db, params[0]);
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
