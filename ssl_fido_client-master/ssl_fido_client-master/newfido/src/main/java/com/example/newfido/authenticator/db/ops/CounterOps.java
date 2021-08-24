package com.example.newfido.authenticator.db.ops;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;


import java.util.ArrayList;
import java.util.List;

import com.example.newfido.authenticator.db.CountersContract;
import com.example.newfido.authenticator.db.models.Counter;


/**
 * Created by sorin.teican on 02-Nov-16.
 */
 
public class CounterOps {

    private static long insertCounter(final SQLiteOpenHelper _db, final Counter counter) {
        SQLiteDatabase db = _db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CountersContract.CountersEntry.COLUMN_NAME_TYPE, counter.type);
        values.put(CountersContract.CountersEntry.COLUMN_NAME_VALUE, counter.value);
        values.put(CountersContract.CountersEntry.COLUMN_NAME_CONTEXT, counter.context);

        return db.insert(CountersContract.CountersEntry.TABLE_NAME, null, values);
    }

    private static Counter getCounter(final SQLiteOpenHelper _db, final String context) {
        SQLiteDatabase db = _db.getReadableDatabase();

        // Which columns to retrieve from the table.
        String[] projection = {
           CountersContract.CountersEntry.COLUMN_NAME_TYPE,
           CountersContract.CountersEntry.COLUMN_NAME_VALUE,
           CountersContract.CountersEntry.COLUMN_NAME_CONTEXT
        };

        // Filter results WHERE "context" = .
        String selection = CountersContract.CountersEntry.COLUMN_NAME_CONTEXT + " = ?";
        String[] selectionArgs = { context };

        Cursor c = db.query(
                CountersContract.CountersEntry.TABLE_NAME,  // The table to query.
                projection,                                 // The columns to return.
                selection,                                  // The columns for the WHERE clause.
                selectionArgs,                              // The values for the WHERE clause.
                null,                                       // don`t group the rows.
                null,                                       // don`t filter by row groups.
                null                                        // the sort order.
        );

        if (!c.moveToFirst())
            return null;

        Counter counter = new Counter();
        counter.type = c.getString(c.getColumnIndex(CountersContract.CountersEntry.COLUMN_NAME_TYPE));
        counter.value = c.getInt(c.getColumnIndex(CountersContract.CountersEntry.COLUMN_NAME_VALUE));
        counter.context = c.getString(c.getColumnIndex(CountersContract.CountersEntry.COLUMN_NAME_CONTEXT));
		
		c.close();

        return counter;
    }

    private static List<Counter> getAllCounters(final SQLiteOpenHelper _db) {
        SQLiteDatabase db = _db.getReadableDatabase();

        String[] projection = {
                CountersContract.CountersEntry.COLUMN_NAME_TYPE,
                CountersContract.CountersEntry.COLUMN_NAME_VALUE,
                CountersContract.CountersEntry.COLUMN_NAME_CONTEXT
        };

        Cursor c = db.query(
                CountersContract.CountersEntry.TABLE_NAME,  // The table to query.
                projection,                                 // The columns to return.
                null,                                       // The columns for the WHERE clause.
                null,                                       // The values for the WHERE clause.
                null,                                       // don`t group the rows.
                null,                                       // don`t filter by row groups.
                null                                        // the sort order.
        );

        List<Counter> counters = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Counter counter = new Counter();
                counter.type = c.getString(c.getColumnIndex(CountersContract.CountersEntry.COLUMN_NAME_TYPE));
                counter.value = c.getInt(c.getColumnIndex(CountersContract.CountersEntry.COLUMN_NAME_VALUE));
                counter.context = c.getString(c.getColumnIndex(CountersContract.CountersEntry.COLUMN_NAME_CONTEXT));

                counters.add(counter);
            } while (c.moveToNext());
        }
		
		c.close();

        return counters;
    }

    private static void incrementCounter(final SQLiteOpenHelper _db, final String context) {
        SQLiteDatabase db = _db.getReadableDatabase();

        String query = "UPDATE " + CountersContract.CountersEntry.TABLE_NAME +
                " SET " + CountersContract.CountersEntry.COLUMN_NAME_VALUE + " = " +
                CountersContract.CountersEntry.COLUMN_NAME_VALUE + " + 1 " +
                "WHERE " + CountersContract.CountersEntry.COLUMN_NAME_CONTEXT + " = " + "\"" + context + "\"";

        db.execSQL(query);
    }

    private static void deleteCounter(final SQLiteOpenHelper _db, final String context) {
        SQLiteDatabase db = _db.getReadableDatabase();

        String selection = CountersContract.CountersEntry.COLUMN_NAME_CONTEXT + " LIKE ?";
        String[] selectionArgs = { context };

        db.delete(CountersContract.CountersEntry.TABLE_NAME, selection, selectionArgs);
    }

    public static class InsertCounterOp extends AsyncTask<Counter, Void, Long> {

        private SQLiteOpenHelper _db;
        private boolean _done;
        private Long _result;

        public InsertCounterOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Long doInBackground(Counter... params) {
            Long id = insertCounter(_db, params[0]);
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

    public static class GetCounterOp extends AsyncTask<String, Void, Counter> {

        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private Counter _result = null;

        public GetCounterOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Counter doInBackground(String... params) {
            _result =  getCounter(_db, params[0]);
            _done = true;
            return _result;
        }

        @Override
        protected void onPostExecute(Counter result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() {
            return _done;
        }

        public Counter getResult() {
            return _result;
        }
    }

    public static class GetAllCountersOp extends AsyncTask<Void, Void, List<Counter>> {

        private SQLiteOpenHelper _db;
        private boolean _done = false;
        private List<Counter> _result = null;

        public GetAllCountersOp(SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected List<Counter> doInBackground(Void... params) {
            _result = getAllCounters(_db);
            _done = true;
            return _result;
        }

        @Override
        protected void onPostExecute(List<Counter> result) {
            _result = result;
            _done = true;
        }

        public boolean isDone() {
            return _done;
        }

        public List<Counter> getResult() {
            return _result;
        }
    }

    public static class IncrementCounterOp extends AsyncTask<String, Void, Void> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;

        public IncrementCounterOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Void doInBackground(String... params) {
            incrementCounter(_db, params[0]);
            _done = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            _done = true;
        }

        public boolean isDone() { return _done; }
    }

    public static class DeleteCounterOp extends AsyncTask<String, Void, Void> {
        private SQLiteOpenHelper _db;
        private boolean _done = false;

        public DeleteCounterOp(final SQLiteOpenHelper db) {
            _db = db;
        }

        @Override
        protected Void doInBackground(String... params) {
            deleteCounter(_db, params[0]);
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
