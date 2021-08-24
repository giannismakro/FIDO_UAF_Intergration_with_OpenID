package com.example.newfido.authenticator.db.controllers;

import java.util.List;

import com.example.newfido.authenticator.db.CountersDbHelper;
import com.example.newfido.authenticator.db.models.Counter;
import com.example.newfido.authenticator.db.ops.CounterOps;


/**
 * Created by sorin.teican on 02-Nov-16.
 */


public class CountersController {

    private CountersDbHelper _db;

    public CountersController(CountersDbHelper db) {
        _db = db;
    }

    public void insertCounter(final Counter counter) {
        CounterOps.InsertCounterOp insertOp = new CounterOps.InsertCounterOp(_db);
        insertOp.execute(counter);

        while (!insertOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Counter getCounter(final String context) {
        CounterOps.GetCounterOp getOp = new CounterOps.GetCounterOp(_db);
        getOp.execute(context);

        while (!getOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getOp.getResult();
    }

    public List<Counter> getAllCounters() {
        CounterOps.GetAllCountersOp getAllCountersOp = new CounterOps.GetAllCountersOp(_db);
        getAllCountersOp.execute();

        while (!getAllCountersOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getAllCountersOp.getResult();
    }

    public void incrementCounter(final String context) {
        CounterOps.IncrementCounterOp incrementOp = new CounterOps.IncrementCounterOp(_db);
        incrementOp.execute(context);

        while (!incrementOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteCounter(final String context) {
        CounterOps.DeleteCounterOp deleteOp = new CounterOps.DeleteCounterOp(_db);
        deleteOp.execute(context);

        while (!deleteOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
