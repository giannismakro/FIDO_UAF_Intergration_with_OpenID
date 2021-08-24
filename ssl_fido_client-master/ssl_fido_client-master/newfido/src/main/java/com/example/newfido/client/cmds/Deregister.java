package com.example.newfido.client.cmds;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.newfido.client.activity.ClientEntrypoint;
import com.example.newfido.client.cmds.util.FieldValidator;
import com.example.newfido.client.db.AuthenticatorInfoDbHelper;
import com.example.newfido.client.db.controller.AuthenticatorInfoController;
import com.example.newfido.msg.DeregisterAuthenticator;
import com.example.newfido.msg.DeregistrationRequest;
import com.example.newfido.msg.Version;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.Request;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;
import com.example.newfido.msg.asm.obj.RequestData;
import com.google.gson.Gson;



import java.util.List;


/**
 * Created by sorin.teican on 22-Feb-17.
 */
 
public class Deregister {

    private Activity mContext;
    AuthenticatorInfoDbHelper mDbHelper;
    private AuthenticatorInfoController mAuthenticatorInfoController;
    private Gson mGson;

    public Deregister(Activity context) {
        mContext = context;

        mDbHelper = new AuthenticatorInfoDbHelper(mContext);
        mAuthenticatorInfoController = new AuthenticatorInfoController(mDbHelper);

        mGson = new Gson();
    }

    /**
     * processRequests
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-processRequests}
     * %%% END SOURCE CODE %%%
     * <p>This function handles the deregistration requests.
     * 
     * <p>DEREG 1.2.1.1
     * @see FieldValidator
     * @see DeregistrationRequest
     */
    public void processRequests(DeregistrationRequest[] requests) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "processRequests");
        // BEGIN: Deregister-processRequests

        if (!FieldValidator.checkDuplicateProtocolDictionaries(requests)) {
            protocolError();
        }
        for (DeregistrationRequest request : requests)
            processRequest(request);
        // END: Deregister-processRequests
    }

    private void protocolError() {
        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x6);

        mContext.setResult(Activity.RESULT_OK, intent);
        mDbHelper.close();

        mContext.finish();
    }

    /**
     * processRequest
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-processRequest}
     * %%% END SOURCE CODE %%%
     * <p>This function validates the deregistration request.
     * 
     * <p>DEREG 1.2.1.1.1
     * @see AuthenticatorInfo
     * @see DeregisterAuthenticator
     * @see AuthenticatorInfo
     * @see ASMRequest
     * @see RequestData
     * 
     * @param request
     * @throws Exception
     */
    private void processRequest(DeregistrationRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "processRequest");
        // BEGIN: Deregister-processRequest

        if (request.header.upv.major != 1 && request.header.upv.minor != 0) FieldValidator.unsupportedVersion(mContext);
        if (!request.header.op.equals("Dereg")) protocolError();
        if (request.header.appID == null || request.header.appID.isEmpty()) protocolError();

        if (!FieldValidator.checkAuthenticators(request.authenticators)) {
            protocolError();
        }

        List<AuthenticatorInfo> authenticatorsInfo = mAuthenticatorInfoController.getAllAuthenticatorsInfo();
        mDbHelper.close();
        for (DeregisterAuthenticator authenticator : request.authenticators) {
            for (AuthenticatorInfo authenticatorInfo : authenticatorsInfo) {
                if (authenticator.aaid.equals(authenticatorInfo.aaid)) {
                    ASMRequest asmRequest = new ASMRequest();
                    RequestData deregisterIn = new RequestData();
                    deregisterIn.appID = request.header.appID;
                    deregisterIn.keyID = authenticator.keyID;
                    asmRequest.args = deregisterIn;
                    asmRequest.requestType = Request.Deregister;
                    asmRequest.authenticatorIndex = authenticatorInfo.authenticatorIndex;
                    asmRequest.asmVersion = new Version(1, 0);

                    Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                    i.addCategory("android.intent.category.DEFAULT");
                    i.setType("application/fido.uaf_asm+json");
                    i.putExtra("message", mGson.toJson(asmRequest));

                    Log.d(this.getClass().getCanonicalName(), "Starting ASM");

                    mContext.startActivityForResult(i, ClientEntrypoint.DEREGISTER_REQ_CODE);
                }
            }
        }
        // END: Deregister-processRequest
    }
}
