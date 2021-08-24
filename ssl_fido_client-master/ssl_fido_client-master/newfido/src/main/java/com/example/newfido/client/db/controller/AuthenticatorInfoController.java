package com.example.newfido.client.db.controller;

import java.util.List;

import com.example.newfido.client.db.AuthenticatorInfoDbHelper;
import com.example.newfido.client.db.ops.AuthenticatorInfoOps;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;


/**
 * Created by sorin.teican on 20-Feb-17.
 */
 

public class AuthenticatorInfoController {

    private AuthenticatorInfoDbHelper _db;

    public AuthenticatorInfoController(AuthenticatorInfoDbHelper db) {
        _db = db;
    }

    public void insertInfo(final AuthenticatorInfo info) {
        AuthenticatorInfoOps.InsertAuthenticatorInfoOp insertOp = new AuthenticatorInfoOps.InsertAuthenticatorInfoOp(_db);
        insertOp.execute(info);

        while (!insertOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * getAllAuthenticatorsInfo
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet AuthenticatorInfoController-getAllAuthenticatorsInfo}
     * %%% END SOURCE CODE %%%
     * <p>This function retrives AuthenticatorInfo from database.
     * 
     * <p>REG 1.2.1.4.1.5.1
     * <p>AUTH 1.2.1.2.1.4.1
     * @see AuthenticatorInfoOps #GetAllAuthenticatorsInfoOp(SQLiteOpenHelper)
     * 
     * @return
     */
    public List<AuthenticatorInfo> getAllAuthenticatorsInfo() {
        // BEGIN: AuthenticatorInfoController-getAllAuthenticatorsInfo
        AuthenticatorInfoOps.GetAllAuthenticatorsInfoOp getAllOp = new AuthenticatorInfoOps.GetAllAuthenticatorsInfoOp(_db);
        getAllOp.execute();

        while (!getAllOp.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return getAllOp.getResult();
        // END: AuthenticatorInfoController-getAllAuthenticatorsInfo
    }
}
