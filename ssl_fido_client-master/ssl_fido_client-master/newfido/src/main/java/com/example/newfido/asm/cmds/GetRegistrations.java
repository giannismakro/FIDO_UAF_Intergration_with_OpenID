package com.example.newfido.asm.cmds;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.newfido.asm.db.AuthenticatorIndexDbHelper;
import com.example.newfido.asm.db.KeyHandleDbHelper;
import com.example.newfido.asm.db.controllers.AuthenticatorIndexController;
import com.example.newfido.asm.db.controllers.KeyHandleController;
import com.example.newfido.asm.db.models.AuthenticatorIndex;
import com.example.newfido.asm.db.models.KeyHandle;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.msg.asm.obj.AppRegistration;
import com.example.newfido.msg.asm.obj.ResponseData;


/**
 * Created by sorin.teican on 13-Feb-17.
 */
 

public class GetRegistrations {

    private Context mContext;
    private String mCallerID;

    KeyHandleDbHelper mKeyHandleDbHelper;
    AuthenticatorIndexDbHelper mAuthenticatorIndexDbHelper;

    private KeyHandleController mKeyHandleController;
    private AuthenticatorIndexController mAutenticatorIndexController;

    public GetRegistrations(Context context, String CallerID) {
        mContext = context;

        mKeyHandleDbHelper = new KeyHandleDbHelper(mContext);
        mAuthenticatorIndexDbHelper = new AuthenticatorIndexDbHelper(mContext);

        mKeyHandleController = new KeyHandleController(mKeyHandleDbHelper);
        mAutenticatorIndexController = new AuthenticatorIndexController(mAuthenticatorIndexDbHelper);

        mCallerID = CallerID;
    }

    public ASMResponse process(ASMRequest request) {
        ASMResponse asmResponse = new ASMResponse();

        if (!checkAuthenticatorIndex(request.authenticatorIndex)) {
            Log.d(this.getClass().getCanonicalName(), "No available authenticator");
            closeDbs();

            asmResponse.responseData = null;
            asmResponse.statusCode = 0x01;

            return asmResponse;
        }

        asmResponse.responseData = buildResponse(request);
        asmResponse.statusCode = 0x00;

        closeDbs();

        return asmResponse;
    }

    private void closeDbs() {
        mAuthenticatorIndexDbHelper.close();
        mKeyHandleDbHelper.close();
    }

    private ResponseData buildResponse(ASMRequest request) {
        ResponseData registrationsOut = new ResponseData();

        Map<String, List<String>> appIdKeyIds = new HashMap<>();
        List<KeyHandle> keyHandles = mKeyHandleController.getAllKeyHandles();
        for (KeyHandle keyHandle : keyHandles) {
            if (keyHandle.CallerID.equals(mCallerID)) {
                if (appIdKeyIds.containsKey(keyHandle.AppID)) {
                    appIdKeyIds.get(keyHandle.AppID).add(keyHandle.TAG_KEYID);
                } else {
                    appIdKeyIds.put(keyHandle.AppID, new ArrayList<String>());
                    appIdKeyIds.get(keyHandle.AppID).add(keyHandle.TAG_KEYID);
                }
            }
        }

        int it = 0;
        registrationsOut.appRegs = new AppRegistration[appIdKeyIds.entrySet().size()];
        for (Map.Entry<String, List<String>> entry : appIdKeyIds.entrySet()) {
            AppRegistration appRegistration = new AppRegistration();
            appRegistration.appID = entry.getKey();

            int len = entry.getValue().size();
            appRegistration.keyIDs = (String[]) entry.getValue().toArray();
            registrationsOut.appRegs[it++] = appRegistration;
        }

        return registrationsOut;
    }

    private boolean checkAuthenticatorIndex(int index) {
        List<AuthenticatorIndex> authenticators = mAutenticatorIndexController.getAllIndexes();
        for (AuthenticatorIndex authenticator : authenticators)
            if (authenticator.index == index)
                return true;

        return false;
    }
}
