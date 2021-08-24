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
 * Created by sorin.teican on 14-Feb-17.
 */
 
public class Register {

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

    private String mAssertionScheme;
    private String mAssertion;

    private String mDeviceID;
    private String mDeviceType;

    //private OnActivityResult mOnActivityResult;

    public Register(ClientEntrypoint context, String FacetID) {
        mContext = context;

        mDbHelper = new AuthenticatorInfoDbHelper(mContext);
        mAuthenticatorInfoController = new AuthenticatorInfoController(mDbHelper);

        mGson = new Gson();

        mChannelBinding = new ChannelBinding();

        mFacetID = FacetID;

        mFacetValidator = new FacetValidator(mFacetID, mContext);
        mPolicyProcessor = new PolicyProcessor(mContext, mAuthenticatorInfoController);

        //mOnActivityResult = result;

        mDeviceID = null;
        mDeviceType = null;
    }

    /**
     * processRequests
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-processRequests}
     * %%% END SOURCE CODE %%%
     * <p>This function handles the registration requests.
     * 
     * <p>REG 1.2.1.4
     * @see Register#processRequest(RegistrationRequest, OnActivityResult)
     * @see RegistrationResponse
     * 
     * @param requests
     * @param onResult
     * @return
     */
    public RegistrationResponse[] processRequests(RegistrationRequest[] requests, OnActivityResult onResult) {
        Log.d(this.getClass().getCanonicalName(), "processRequests");
        // BEGIN: Register-processRequests

        RegistrationResponse[] responses = new RegistrationResponse[requests.length];

        if (!FieldValidator.checkDuplicateProtocolDictionaries(requests)) {
            protocolError();
        }
        for (int i = 0; i < requests.length; i++)
            responses[i] = processRequest(requests[i], onResult);

        return responses;
        // END: Register-processRequests
    }

    public void setmDeviceID(String deviceID) {
        mDeviceID = deviceID;
    }

    public void setmDeviceType(String deviceType) { mDeviceType = deviceType; }

    private void protocolError() {
        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x6); // PROTOCOL_ERROR

        mContext.setResult(Activity.RESULT_OK, intent);
        mDbHelper.close();

        mContext.finish();
    }

    /**
     * noSuitableAuthenticator
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-noSuitableAuthenticator}
     * %%% END SOURCE CODE %%%
     * <p>This function display error message when there is no authenticator.
     * 
     * <p>REG
     */
    private void noSuitableAuthenticator() {
        // BEGIN: Register-noSuitableAuthenticator
        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x5); // NO_SUITABLE_AUTHENTICATOR

        mContext.setResult(Activity.RESULT_OK, intent);
        mDbHelper.close();

        mContext.finish();
        // END: Register-noSuitableAuthenticator
    }

    private void untrustedFacetId() {
        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", mContext.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x7); // UNTRUSTED_FACET_ID

        mContext.setResult(Activity.RESULT_OK, intent);
        mDbHelper.close();

        mContext.finish();
    }

    /**
     * processRequest
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-processRequest}
     * %%% END SOURCE CODE %%%
     * <p>This function validates the registration request.
     * 
     * <p>REG 1.2.1.4.1
     * @see FieldValidator#checkHeader(Activity, OperationHeader, String)
     * @see FieldValidator#checkChallenge(String)
     * @see FieldValidator#checkUsername(String)
     * @see AuthenticatorInfo #processFacetID(String, Version, ChannelBinding)
     * @see PolicyProcessor#processPolicy(Policy)
     * @see Register#noSuitableAuthenticator()
     * @see PolicyProcessor#hasAuthenticatorVersion()
     * @see RegistrationResponse
     * @see FinalChallengeParams
     * @see OnActivityResult
     * @see ASMResponse
     * @see AuthenticatorRegistrationAssertion
     * @see RegistrationResponse
     * @see RequestData
     * @see ASMRequest
     * 
     * @param request
     * @param onResult
     * @return
     */
    private RegistrationResponse processRequest(final RegistrationRequest request, OnActivityResult onResult) {
        Log.d(this.getClass().getCanonicalName(), "processRequest");
        // BEGIN: Register-processRequest

        final RegistrationResponse response = new RegistrationResponse();

        if (!FieldValidator.checkHeader(mContext, request.header, "Reg")) protocolError();
        if (request.header.appID == null || request.header.appID.isEmpty())
            request.header.appID = mFacetID;
        if (!FieldValidator.checkChallenge(request.challenge)) protocolError();
        if (!FieldValidator.checkUsername(request.username)) protocolError();

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
                    if (requestCode == ClientEntrypoint.REGISTER_REQ_CODE) {
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
                        response.assertions = new AuthenticatorRegistrationAssertion[1];
                        response.assertions[0] = new AuthenticatorRegistrationAssertion();
                        response.assertions[0].assertionScheme = asmResponse.responseData.assertionScheme;
                        response.assertions[0].assertion = asmResponse.responseData.assertion;

                        i.putExtra("errorCode", (short) 0x0); // NO_ERROR
                        i.putExtra("message", mGson.toJson(new RegistrationResponse[] { response }));

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
                                Log.d(this.getClass().getCanonicalName(), "onActivityResponse no suitable authenticator");
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

        mContext.setRegisterASMResult(onResult);

        final RequestData registerIn = new RequestData();
        registerIn.username = request.username;
        registerIn.appID = request.header.appID;
        registerIn.finalChallenge = finalChallenge;

        final ASMRequest asmRequest = new ASMRequest();
        asmRequest.asmVersion = new Version(1, 0);
        asmRequest.requestType = Request.Register;
        asmRequest.args = registerIn;

        if (matchingAuthenticators.size() == 1) {
            asmRequest.authenticatorIndex = matchingAuthenticators.get(0).authenticatorIndex;
            registerIn.attestationType = matchingAuthenticators.get(0).attestationTypes[0];

            Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
            i.addCategory("android.intent.category.DEFAULT");
            i.setType("application/fido.uaf_asm+json");
            i.putExtra("message", mGson.toJson(asmRequest));
            i.putExtra("deviceID", mDeviceID);
            i.putExtra("deviceType", mDeviceType);

            Log.d(this.getClass().getCanonicalName(), "Starting ASM");

            mContext.startActivityForResult(i, ClientEntrypoint.REGISTER_REQ_CODE);
        } else if (matchingAuthenticators.size() > 1){
            List<String> authenticatorTitles = new ArrayList<>();
            for (AuthenticatorInfo info : matchingAuthenticators)
                authenticatorTitles.add(info.title);

            // send request to ASM after the user clicks list item.
            ClientEntrypoint.SelectAuthenticator selectAuthenticator = new ClientEntrypoint.SelectAuthenticator() {
                @Override
                public void proceed(int pos) {
                    asmRequest.authenticatorIndex = matchingAuthenticators.get(pos).authenticatorIndex;
                    registerIn.attestationType = matchingAuthenticators.get(pos).attestationTypes[0];

                    Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                    i.addCategory("android.intent.category.DEFAULT");
                    i.setType("application/fido.uaf_asm+json");
                    i.putExtra("message", mGson.toJson(asmRequest));
                    i.putExtra("deviceID", mDeviceID);
                    i.putExtra("deviceType", mDeviceType);

                    Log.d(this.getClass().getCanonicalName(), "Starting ASM");

                    mContext.startActivityForResult(i, ClientEntrypoint.REGISTER_REQ_CODE);
                }
            };

            mContext.showAuthenticators(authenticatorTitles, selectAuthenticator);
        }

        return response;
        // END: Register-processRequest
    }


}
