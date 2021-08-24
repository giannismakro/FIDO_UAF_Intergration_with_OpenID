package com.example.newfido.asm.db.controllers;


import java.util.List;

import com.example.newfido.asm.db.AuthenticatorIndexDbHelper;
import com.example.newfido.asm.db.models.AuthenticatorIndex;
import com.example.newfido.asm.db.ops.AuthenticatorIndexOps;


/**
 * Created by sorin.teican on 12-Jan-17.
 */
 
public class AuthenticatorIndexController {

    private AuthenticatorIndexDbHelper _db;

    public AuthenticatorIndexController(AuthenticatorIndexDbHelper db) {
        _db = db;
    }

    public void insertIndex(final AuthenticatorIndex index) {
        AuthenticatorIndexOps.InsertIndexOp insertOp = new AuthenticatorIndexOps.InsertIndexOp(_db);
        insertOp.execute(index);

        while (!insertOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public AuthenticatorIndex getIndex(final String AAID) {
        AuthenticatorIndexOps.GetIndexOp getOp = new AuthenticatorIndexOps.GetIndexOp(_db);
        getOp.execute(AAID);

        while (!getOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getOp.getResult();
    }

    public List<AuthenticatorIndex> getAllIndexes() {
        AuthenticatorIndexOps.GetAllIndexesOp getAllOp = new AuthenticatorIndexOps.GetAllIndexesOp(_db);
        getAllOp.execute();

        while (!getAllOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getAllOp.getResult();
    }

    public void deleteIndex(final String AAID) {
        AuthenticatorIndexOps.DeleteIndexOp deleteOp = new AuthenticatorIndexOps.DeleteIndexOp(_db);
        deleteOp.execute(AAID);

        while (!deleteOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
