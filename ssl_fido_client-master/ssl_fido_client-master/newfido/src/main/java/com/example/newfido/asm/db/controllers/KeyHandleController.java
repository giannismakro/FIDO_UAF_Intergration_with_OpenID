package com.example.newfido.asm.db.controllers;

import android.util.Log;
import java.util.List;

import com.example.newfido.asm.db.KeyHandleDbHelper;
import com.example.newfido.asm.db.models.KeyHandle;
import com.example.newfido.asm.db.ops.KeyHandleOps;


/**
 * Created by sorin.teican on 12-Jan-17.
 */
 

public class KeyHandleController {

    private KeyHandleDbHelper _db;

    public KeyHandleController(KeyHandleDbHelper db) {
        _db = db;
    }

    /**
     * insertKeyHandle
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet KeyHandleController-insertKeyHandle}
     * %%% END SOURCE CODE %%%
     * <p>This function stores key handle.
     * 
     * <p>REG 4.1.1
     * @see KeyHandleOps
     * 
     * @param keyHandle
     */
    public void insertKeyHandle(final KeyHandle keyHandle) {
        Log.d(this.getClass().getCanonicalName(), "insertKeyHandle");
        // BEGIN: KeyHandleController-insertKeyHandle

        KeyHandleOps.InsertKeyHandleOp insertOp = new KeyHandleOps.InsertKeyHandleOp(_db);
        insertOp.execute(keyHandle);

        while(!insertOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // END: KeyHandleController-insertKeyHandle
    }

    public KeyHandle getKeyHandle(final String AppID, final String KeyID) {
        KeyHandleOps.GetKeyHandleOp getOp = new KeyHandleOps.GetKeyHandleOp(_db);
        getOp.execute(AppID, KeyID);

        while (!getOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getOp.getResult();
    }

    /**
     * getAllKeyHandles
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet KeyHandleController-getAllKeyHandles}
     * %%% END SOURCE CODE %%%
     * <p>This function returns all the key handles.
     * 
     * <p>AUTH
     * 
     * @return
     */
    public List<KeyHandle> getAllKeyHandles() {
        Log.d(this.getClass().getCanonicalName(), "getAllKeyHandles");
        // BEGIN: KeyHandleController-getAllKeyHandles

        KeyHandleOps.GetAllKeyHandlesOp getAllOp = new KeyHandleOps.GetAllKeyHandlesOp(_db);
        getAllOp.execute();

        while(!getAllOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getAllOp.getResult();
        // END: KeyHandleController-getAllKeyHandles
    }

    /**
     * deleteKeyHandle
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet KeyHandleController-deleteKeyHandle}
     * %%% END SOURCE CODE %%%
     * <p>This function deletes key handle.
     * 
     * <p>DEREG 4.1
     * @param AppID
     * @param KeyID
     */
    public void deleteKeyHandle(final String AppID, final String KeyID) {
        Log.d(this.getClass().getCanonicalName(), "deleteKeyHandle");
        // BEGIN: KeyHandleController-deleteKeyHandle

        KeyHandleOps.DeleteKeyHandleOp deleteOp = new KeyHandleOps.DeleteKeyHandleOp(_db);
        deleteOp.execute(AppID, KeyID);

        while (!deleteOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // END: KeyHandleController-deleteKeyHandle
    }

}
