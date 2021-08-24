package com.example.newfido.client.cmds;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.example.newfido.client.activity.ClientEntrypoint;
import com.example.newfido.client.cmds.util.FacetValidator;
import com.example.newfido.client.cmds.util.FieldValidator;
import com.example.newfido.client.cmds.util.PolicyProcessor;
import com.example.newfido.client.db.AuthenticatorInfoDbHelper;
import com.example.newfido.client.db.controller.AuthenticatorInfoController;
import com.example.newfido.msg.*;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.msg.asm.Request;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;
import com.example.newfido.msg.asm.obj.RequestData;
import com.google.gson.Gson;



import java.util.ArrayList;
import java.util.List;


/**
 * Created by sorin.teican on 22-Feb-17.
 */ 

public class Authenticate {

    public interface OnActivityResult {
        void onResult(int requestCode, int resultCode, Intent result);
    }

    private ClientEntrypoint mContext;
    AuthenticatorInfoDbHelper mDbHelper;
    private AuthenticatorInfoController mAuthenticatorInfoController;
    private Gson mGson;
    private String mFacetID;
    private ChannelBinding mChannelBinding;

    private FacetValidator mFacetValidator;
    private PolicyProcessor mPolicyProcessor;

    //private OnActivityResult mOnActivityResult;

    public Authenticate(ClientEntrypoint context, String FacetID) {
        mContext = context;

        mDbHelper = new AuthenticatorInfoDbHelper(mContext);
        mAuthenticatorInfoController = new AuthenticatorInfoController(mDbHelper);

        mFacetValidator = new FacetValidator(mFacetID, mContext);

        mGson = new Gson();

        mChannelBinding = new ChannelBinding();

        mFacetID = FacetID;

        mFacetValidator = new FacetValidator(mFacetID,mContext);
        mPolicyProcessor = new PolicyProcessor(mContext, mAuthenticatorInfoController);

        //mOnActivityResult = result;
    }

    /**
     * processRequests
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-processRequests}
     * %%% END SOURCE CODE %%%
     * <p>This function handles the authentication requests.
     *
     * <p>AUTH 1.2.1.2
     * @see Authenticate #processRequest()
     * @see AuthenticationResponse
     * @see FieldValidator
     *
     * @param requests
     * @param onResult
     * @return
     */
    public AuthenticationResponse[] processRequests(AuthenticationRequest[] requests, OnActivityResult onResult) {
        Log.d(this.getClass().getCanonicalName(), "processRequests");
        // BEGIN: Authenticate-processRequests

        AuthenticationResponse[] responses = new AuthenticationResponse[requests.length];

        if (!FieldValidator.checkDuplicateProtocolDictionaries(requests)) {
            protocolError();
        }
        for (int i = 0; i < requests.length; i++)
            responses[i] = processRequest(requests[i], onResult);

        return responses;
        // END: Authenticate-processRequests
    }

    private void protocolError() {
        Log.d(this.getClass().getCanonicalName(), "protocolError");

        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x6);

        mContext.setResult(Activity.RESULT_OK, intent);
        mContext.finish();
    }

    private void noSuitableAuthenticator() {
        Log.d(this.getClass().getCanonicalName(), "noSuitableAuthenticator");

        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x5);

        mContext.setResult(Activity.RESULT_OK, intent);
        mDbHelper.close();

        mContext.finish();
    }

    private void untrustedFacetId() {
        Log.d(this.getClass().getCanonicalName(), "untrustedFacetId");

        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x7);

        mContext.setResult(Activity.RESULT_OK, intent);
        mDbHelper.close();

        mContext.finish();
    }

    /**
     * processRequest
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-processRequest}
     * %%% END SOURCE CODE %%%
     * <p>This function validates the authentication request.
     *
     * <p>AUTH 1.2.1.2.1
     * @see FieldValidator#checkHeader(Activity, OperationHeader, String)
     * @see FieldValidator#checkChallenge(String)
     * @see AuthenticatorInfo #processFacetID(String, Version, ChannelBinding)
     * @see PolicyProcessor#processPolicy(Policy)
     * @see Authenticate#noSuitableAuthenticator()
     * @see AuthenticationResponse
     * @see FinalChallengeParams
     * @see OnActivityResult
     * @see ASMResponse
     * @see AuthenticatorSignAssertion
     * @see RequestData
     * @see ASMRequest
     *
     * @param request
     * @param onResult
     * @return
     */
    private AuthenticationResponse processRequest(final AuthenticationRequest request, OnActivityResult onResult) {
        Log.d(this.getClass().getCanonicalName(), "processRequest");
        // BEGIN: Authenticate-processRequest

        final AuthenticationResponse response = new AuthenticationResponse();

        if (!FieldValidator.checkHeader(mContext, request.header, "Auth")) protocolError();
        if (request.header.appID == null || request.header.appID.isEmpty())
            request.header.appID = mFacetID;
        if (!FieldValidator.checkChallenge(request.challenge)) protocolError();

        if (!mFacetValidator.processFacetID(request.header.appID, request.header.upv, mChannelBinding)) {
           untrustedFacetId();
        }

        final List<AuthenticatorInfo> matchingAuthenticators = mPolicyProcessor.processPolicy(request.policy);
        if (matchingAuthenticators.isEmpty()) {
            noSuitableAuthenticator();
        }

        mDbHelper.close();

        FinalChallengeParams fcp = new FinalChallengeParams();
        fcp.appID = request.header.appID;
        fcp.challenge = request.challenge;
        fcp.facetID = mFacetID;
        fcp.channelBinding = mChannelBinding;
        final String finalChallenge = Base64.encodeToString(mGson.toJson(fcp).getBytes(), Base64.URL_SAFE);

        onResult = new OnActivityResult() {
            @Override
            public void onResult(int requestCode, int resultCode, Intent result) {
                if (resultCode == -1) { // RESULT_OK
                    if (requestCode == ClientEntrypoint.AUTHENTICATE_REQ_CODE) {
                        Intent i = new Intent();
                        i.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
                        i.putExtra("componentName", mContext.getComponentName().flattenToString());

                        ASMResponse asmResponse = mGson.fromJson(result.getStringExtra("message"), ASMResponse.class);
                        Log.d("Jean-Didier Debug","asmResponse status code" + asmResponse.statusCode);
                        Log.d("Jean-Didier Debug","asmResponse data" + asmResponse.responseData.assertionScheme);

                        if (asmResponse.statusCode == 0x01 || asmResponse.statusCode == 0x02 || asmResponse.responseData == null) { //changed
                            i.putExtra("errorCode", 0xff); // UNKNOWN
                            mContext.setResult(Activity.RESULT_OK, i);
                            mContext.finish();
                        } else if (asmResponse.statusCode == 0x03) {
                            i.putExtra("errorCode", 0x05); // NO_SUITABLE_AUTHENTICATOR.
                            mContext.setResult(Activity.RESULT_OK, i);
                            mContext.finish();
                        }

                        response.header = request.header;
                        response.fcParams = finalChallenge;
                        response.assertions = new AuthenticatorSignAssertion[1];
                        response.assertions[0] = new AuthenticatorSignAssertion();
                        response.assertions[0].assertionScheme = asmResponse.responseData.assertionScheme; // this line creates the bug ,because responseData is null
                        response.assertions[0].assertion = asmResponse.responseData.assertion;

                        i.putExtra("errorCode", (short) 0x0); // NO_ERROR
                        i.putExtra("message", mGson.toJson(new AuthenticationResponse[] { response }));

                        mContext.setResult(Activity.RESULT_OK, i);

                        Log.d(this.getClass().getCanonicalName(), "Finishing activity");

                        mContext.finish();

                        if (mPolicyProcessor.hasAuthenticatorVersion()) {
                            try {
                                if (!mPolicyProcessor.checkVersion(request.policy, response.assertions[0].assertion)) {
                                    noSuitableAuthenticator();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(this.getClass().getCanonicalName(), "onActivityResponse no authenticator");
                                noSuitableAuthenticator();
                            }
                        }
                    } else {
                        Log.d(this.getClass().getCanonicalName(), "onActivityResponse protocolError");
                        protocolError();
                    }
                } else {
                    Log.d(this.getClass().getCanonicalName(), "onActivityResponse protocolError");
                    protocolError();
                }
            }
        };

        mContext.setAuthenticateASMResult(onResult);

        RequestData authenticateIn = new RequestData();
        authenticateIn.appID = request.header.appID;
        authenticateIn.transaction = request.transaction;
        authenticateIn.finalChallenge = finalChallenge;

        final ASMRequest asmRequest = new ASMRequest();
        asmRequest.asmVersion = new Version(1, 0);
        asmRequest.requestType = Request.Authenticate;
        asmRequest.args = authenticateIn;

        if (matchingAuthenticators.size() == 1) {
            asmRequest.authenticatorIndex = matchingAuthenticators.get(0).authenticatorIndex;

            Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
            i.addCategory("android.intent.category.DEFAULT");
            i.setType("application/fido.uaf_asm+json");
            i.putExtra("message", mGson.toJson(asmRequest));

            Log.d(this.getClass().getCanonicalName(), "Starting ASM");

            mContext.startActivityForResult(i, ClientEntrypoint.AUTHENTICATE_REQ_CODE);
        } else if (matchingAuthenticators.size() > 1) {
            List<String> authenticatorTitles = new ArrayList<>();
            for (AuthenticatorInfo info : matchingAuthenticators)
                authenticatorTitles.add(info.title);

            // send request to ASM after the user clicks list item.
            ClientEntrypoint.SelectAuthenticator selectAuthenticator = new ClientEntrypoint.SelectAuthenticator() {
                @Override
                public void proceed(int pos) {
                    asmRequest.authenticatorIndex = matchingAuthenticators.get(pos).authenticatorIndex;

                    Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                    i.addCategory("android.intent.category.DEFAULT");
                    i.setType("application/fido.uaf_asm+json");
                    i.putExtra("message", mGson.toJson(asmRequest));

                    Log.d(this.getClass().getCanonicalName(), "Starting ASM");

                    mContext.startActivityForResult(i, ClientEntrypoint.AUTHENTICATE_REQ_CODE);
                }
            };

            mContext.showAuthenticators(authenticatorTitles, selectAuthenticator);
        }

        return response;
        // END: Authenticate-processRequest
    }
}
