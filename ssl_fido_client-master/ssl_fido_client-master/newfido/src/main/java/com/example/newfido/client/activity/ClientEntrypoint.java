package com.example.newfido.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.example.newfido.asm.activity.ASMEntrypoint;
import com.example.newfido.client.cmds.Authenticate;
import com.example.newfido.client.cmds.Deregister;
import com.example.newfido.client.cmds.Register;
import com.example.newfido.client.db.AuthenticatorInfoDbHelper;
import com.example.newfido.client.db.controller.AuthenticatorInfoController;
import com.example.newfido.msg.AuthenticationRequest;
import com.example.newfido.msg.DeregistrationRequest;
import com.example.newfido.msg.RegistrationRequest;
import com.example.newfido.msg.Version;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.msg.asm.Request;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;
import com.example.newfido.msg.client.Authenticator;
import com.example.newfido.msg.client.DiscoveryData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

import com.example.newfido.R;

 

public class ClientEntrypoint extends Activity {

    public interface SelectAuthenticator {
        public void proceed(int pos);
    }

    public static final int GET_INFO_REQ_CODE = 0;
    public static final int REGISTER_REQ_CODE = 1;
    public static final int AUTHENTICATE_REQ_CODE = 2;
    public static final int DEREGISTER_REQ_CODE = 3;
    public static final int GET_REGISTRATIONS_REQ_CODE = 4;

    private Intent mStartIntent;
    private Gson mGson;

    AuthenticatorInfoDbHelper mAuthenticatorInfoDbHelper;
    private AuthenticatorInfoController mAuthenticatorInfoController;

    Register.OnActivityResult mRegisterASMResult;
    Authenticate.OnActivityResult mAuthenticateASMResult;

    private ViewFlipper mViewFlipper;

    private ListView mListView;
    ArrayList<String> mAuthenticators;
    private ArrayAdapter<String> mListAdapter;

    private SelectAuthenticator mSelectAuthenticator;

    public void setRegisterASMResult(Register.OnActivityResult result) {
        mRegisterASMResult = result;
    }

    public void setAuthenticateASMResult(Authenticate.OnActivityResult result) {
        mAuthenticateASMResult = result;
    }

    /**
     * onCreate
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-onCreate}
     * %%% END SOURCE CODE %%%
     * <p>Called when the activity is starting.
     * 
     * <p>DISC 1
     * <p>REG 1
     * <p>AUTH 1
     * <p>DEREG 1
     * 
     * @see ClientEntrypoint#initMembers()
     * @see ClientEntrypoint#proceed()
     * 
     * @param savedInstanceState A mapping from String keys to various Parcelable values.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this.getClass().getCanonicalName(), "onCreate");
        // BEGIN: ClientEntrypoint-onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_entrypoint);

        initMembers();

        proceed();
        // END: ClientEntrypoint-onCreate
    }

    /**
     * initMembers
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-initMembers}
     * %%% END SOURCE CODE %%%
     * <p>This function initializes the authenticators.
     * 
     * <p>DISC 1.1
     * <p>REG 1.1
     * <p>AUTH 1.1
     * <p>DEREG 1.1
     * 
     * @see AuthenticatorInfoDbHelper
     * @see AuthenticatorInfoController
     */
    private void initMembers() {
        Log.d(this.getClass().getCanonicalName(), "initMembers");
        // BEGIN: ClientEntrypoint-initMembers

        mStartIntent = getIntent();
        mGson = new Gson();

        mAuthenticatorInfoDbHelper = new AuthenticatorInfoDbHelper(this);
        mAuthenticatorInfoController = new AuthenticatorInfoController(mAuthenticatorInfoDbHelper);

        mViewFlipper = (ViewFlipper) findViewById(R.id.vf_viewHolder);
        mViewFlipper.setAutoStart(false);
        mViewFlipper.setFlipInterval(0);
        mViewFlipper.setInAnimation(null);
        mViewFlipper.setOutAnimation(null);

        mAuthenticators = new ArrayList<>();
        mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mAuthenticators);

        mListView = (ListView) findViewById(R.id.lv_authenticators);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectAuthenticator.proceed(position);
                mListView.setVisibility(View.INVISIBLE);
            }
        });
        // END: ClientEntrypoint-initMembers
    }

    public void showAuthenticators(List<String> authenticators, SelectAuthenticator selectAuthenticator) {
        mSelectAuthenticator = selectAuthenticator;

        mAuthenticators.clear();
        for (String title : authenticators) {
            if (title != null)
                mAuthenticators.add(title);
        }

        mListAdapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
    }

    /**
     * proceed
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-proceed}
     * %%% END SOURCE CODE %%%
     * <p>This function checks the type of the request.
     * 
     * <p>DISC 1.2
     * <p>REG 1.2
     * <p>AUTH 1.2
     * <p>DEREG 1.2
     * @see ClientEntrypoint#passDiscoverIntent()
     * 
     * @see ClientEntrypoint#passUAFOperationIntent()
     */
    private void proceed() {
        Log.d(this.getClass().getCanonicalName(), "proceed");
        // BEGIN: ClientEntrypoint-proceed

        Log.d(this.getClass().getCanonicalName(), "Started proceeding");
        switch (mStartIntent.getStringExtra("UAFIntentType")) {
            case "DISCOVER":
                passDiscoverIntent();
                break;
            case "CHECK_POLICY":
                break;
            case "UAF_OPERATION":
                Log.d(this.getClass().getCanonicalName(), "Proceeding to: " + mStartIntent.getStringExtra("UAFIntentType"));
                try {
                    passUAFOperationIntent();
                } catch (Exception e) {

                }
                break;
            case "UAF_OPERATION_COMPLETION_STATUS":
                break;
            default:

        }
        // END: ClientEntrypoint-proceed
    }

    /**
     * passDiscoverIntent
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-passDiscoverIntent}
     * %%% END SOURCE CODE %%%
     * <p>This function creates a GetInfo request to ASM.
     * 
     * <p>DISC 1.2.1
     * @see ASMRequest
     * @see ASMEntrypoint #onCreate(Bundle)
     * 
     */
    private void passDiscoverIntent() {
        Log.d(this.getClass().getCanonicalName(), "passDiscoverIntent");
        // BEGIN: ClientEntrypoint-passDiscoverIntent

        ASMRequest asmRequest = new ASMRequest();
        asmRequest.requestType = Request.GetInfo;

        Intent request = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        request.addCategory("android.intent.category.DEFAULT");
        request.setType("application/fido.uaf_asm+json");
        request.putExtra("message", mGson.toJson(asmRequest));

        Log.d(this.getClass().getCanonicalName(), "DISCOVER: " + mGson.toJson(asmRequest));

        startActivityForResult(request, GET_INFO_REQ_CODE);
        // END: ClientEntrypoint-passDiscoverIntent
    }

    /**
     * passDiscoverResponseIntent
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-passDiscoverResponseIntent}
     * %%% END SOURCE CODE %%%
     * <p>This function saves the information of the response to objects.
     * 
     * <p>DISC 5.1
     * 
     * @see Authenticator
     * @see DiscoveryData
     * @see Version
     * 
     * @param info
     */
    private void passDiscoverResponseIntent(AuthenticatorInfo[] info) {
        Log.d(this.getClass().getCanonicalName(), "passDiscoverResponseIntent");
        // BEGIN: ClientEntrypoint-passDiscoverResponseIntent

        Authenticator[] authenticators = new Authenticator[info.length];
        for (int i = 0; i < info.length; i++) {
            authenticators[0] = new Authenticator();
            authenticators[0].title = info[0].title;
            authenticators[0].aaid = info[0].aaid;
            authenticators[0].description = info[0].description;
            authenticators[0].supportedUAFVersions = info[0].asmVersions;
            authenticators[0].assertionScheme = info[0].assertionScheme;
            authenticators[0].authenticationAlgorithm = info[0].authenticationAlgorithm;
            authenticators[0].attestationTypes = info[0].attestationTypes;
            authenticators[0].userVerification = info[0].userVerification;
            authenticators[0].keyProtection = info[0].keyProtection;
            authenticators[0].matcherProtection = info[0].matcherProtection;
            authenticators[0].attachmentHint = info[0].attachmentHint;
            authenticators[0].isSecondFactorOnly = info[0].isSecondFactorOnly;
            authenticators[0].tcDisplay = info[0].tcDisplay;
            authenticators[0].tcDisplayContentType = info[0].tcDisplayContentType;
            authenticators[0].icon = info[0].icon;
            authenticators[0].supportedExtensionIDs = info[0].supportedExtensionIDs;
        }

        DiscoveryData discoveryData = new DiscoveryData();
        discoveryData.supportedUAFVersions = new Version[1];
        discoveryData.supportedUAFVersions[0] = new Version(1, 0);
        discoveryData.clientVendor = "UPRC";
        discoveryData.clientVersion = new Version(0, 1);
        discoveryData.availableAuthenticators = authenticators;

        Intent result = new Intent();
        result.putExtra("UAFIntentType", "DISCOVER_RESULT");
        result.putExtra("discoveryData", mGson.toJson(discoveryData));
        result.putExtra("componentName", getComponentName().flattenToString());
        result.putExtra("errorCode", (short)0x0); // NO_ERROR
        setResult(RESULT_OK, result);

        Log.d(this.getClass().getCanonicalName(), "Parsed getinfo, Finishing activity");

        finish();
        // END: ClientEntrypoint-passDiscoverResponseIntent
    }

    /**
     * passUAFOperationIntent
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-passUAFOperationIntent}
     * %%% END SOURCE CODE %%%
     * <p>This function handles requests by operation type.
     * 
     * <p>REG 1.2.1
     * <p>AUTH 1.2.1
     * <p>DEREG 1.2.1
     * @see ClientEntrypoint#obtainCallerFacetID()
     * @see ClientEntrypoint#getDeviceID()
     * @see ClientEntrypoint#getDeviceType()
     * @see Register#processRequests(RegistrationRequest[], Register.OnActivityResult)
     * @see RegistrationRequest
     * @see Register
     * 
     * @see ClientEntrypoint#obtainCallerFacetID()
     * @see Register#processRequests(RegistrationRequest[], Register.OnActivityResult)
     * @see AuthenticationRequest
     * @see Authenticate
     * 
     * @see Deregister#processRequests(DeregistrationRequest[])
     * @see DeregistrationRequest
     * @see Deregister
     */
    private void passUAFOperationIntent() throws Exception {
        Log.d(this.getClass().getCanonicalName(), "passUAFOperationIntent");
        // BEGIN: ClientEntrypoint-passUAFOperationIntent

        String uafMessage = mStartIntent.getStringExtra("message");
        //Log.d(this.getClass().getCanonicalName(), "uafMessage: " + uafMessage);
        if (uafMessage.contains("\"op\":\"Reg\"")) {
            RegistrationRequest[] registrationRequests = mGson.fromJson(uafMessage, RegistrationRequest[].class);
            Register registerCmd = new Register(this, obtainCallerFacetID());
            registerCmd.setmDeviceID(getDeviceID());
            registerCmd.setmDeviceType(getDeviceType());
            Log.d(this.getClass().getCanonicalName(), "UAF_OPERATION: REG: " + mGson.toJson(registrationRequests));
            registerCmd.processRequests(registrationRequests, mRegisterASMResult);
        } else if (uafMessage.contains("\"op\":\"Auth\"")) {
            AuthenticationRequest[] authenticationRequests = mGson.fromJson(uafMessage, AuthenticationRequest[].class);
            Authenticate authenticateCmd = new Authenticate(this, obtainCallerFacetID());
            Log.d(this.getClass().getCanonicalName(), "UAF_OPERATION: AUTH: " + mGson.toJson(authenticationRequests));
            authenticateCmd.processRequests(authenticationRequests, mAuthenticateASMResult);
        } else if (uafMessage.contains("\"op\":\"Dereg\"")) {
            DeregistrationRequest[] deregistrationRequests = mGson.fromJson(uafMessage, DeregistrationRequest[].class);
            Deregister deregister = new Deregister(this);
            Log.d(this.getClass().getCanonicalName(), "UAF_OPERATION: DEREG: " + mGson.toJson(deregistrationRequests));
            deregister.processRequests(deregistrationRequests);
        } else {
            Log.d(this.getClass().getCanonicalName(), "Operation type not detected!");
        }
        // END: ClientEntrypoint-passUAFOperationIntent
    }

    private void passUAFOperationResponseIntent(String asmResponse) {

    }

    /**
     * onActivityResult
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-onActivityResult}
     * %%% END SOURCE CODE %%%
     * <p>Called when an activity you launched exits, giving you the requestCode you started it with, 
     * the resultCode it returned, and any additional data from it. The resultCode will be RESULT_CANCELED 
     * if the activity explicitly returned that, didn't return any result, or crashed during its operation.
     * 
     * <p>DISC 5
     * <p>REG 5
     * <p>AUTH 5
     * <p>DEREG 5
     * 
     * @see ASMResponse
     * @see AuthenticatorInfo
     * 
     * @param requestCode
     * @param resultCode
     * @param response
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent response) {
        Log.d(this.getClass().getCanonicalName(), "onActivityResult");
        // BEGIN: ClientEntrypoint-onActivityResult

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_INFO_REQ_CODE:
                    try {
                        Log.d(this.getClass().getCanonicalName(), "Received getinfo assertion from ASM");
                        ASMResponse asmResponse = mGson.fromJson(response.getStringExtra("message"), ASMResponse.class);
                        for (AuthenticatorInfo info : asmResponse.responseData.Authenticators)
                            mAuthenticatorInfoController.insertInfo(info);
                        mAuthenticatorInfoDbHelper.close();
                        passDiscoverResponseIntent(asmResponse.responseData.Authenticators);
                    } catch (JsonSyntaxException e) {
                        mAuthenticatorInfoDbHelper.close();
                    }
                    break;
                case REGISTER_REQ_CODE:
                    if (mRegisterASMResult == null) { /* error. */ }
                    Log.d(this.getClass().getCanonicalName(), "Received register assertion from ASM");
                    mRegisterASMResult.onResult(requestCode, resultCode, response);
                    break;
                case AUTHENTICATE_REQ_CODE:
                    if (mAuthenticateASMResult == null) { /* error. */ }
                    Log.d(this.getClass().getCanonicalName(), "Received authenticate assertion from ASM");
                    mAuthenticateASMResult.onResult(requestCode, resultCode, response);
                    break;
                case DEREGISTER_REQ_CODE:
//                    ASMResponse<Void> asmResponse = mGson.fromJson(response.getStringExtra("message"), ASMResponse.class);
//                    //if (asmResponse.statusCode == )
                    finish();
                default:
            }
        }
        // END: ClientEntrypoint-onActivityResult
    }

    /**
     * obtainCallerFacetID
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-obtainCallerFacetID}
     * %%% END SOURCE CODE %%%
     * <p>This function generates a facetID.
     * 
     * <p>REG 1.2.1.1
     * <p>AUTH 1.2.1.1
     */
    private String obtainCallerFacetID() throws Exception {
        Log.d(this.getClass().getCanonicalName(), "obtainCallerFacetID");
        // BEGIN: ClientEntrypoint-obtainCallerFacetID

        PackageManager pm = getPackageManager();
        PackageInfo callerPackageInfo = pm.getPackageInfo(getCallingPackage(), pm.GET_SIGNATURES);

        // TODO nikosev revert
        // return "android:apk-key-hash:" +
        //         Base64.encodeToString(Keystore.SHA256(callerPackageInfo.signatures[0].toByteArray()), Base64.URL_SAFE | Base64.NO_WRAP);
        return "android:apk-key-hash:yu23F6N010hHZyCnZMZeEQDfsi7h_rajhISuFazzbdc=";
        // END: ClientEntrypoint-obtainCallerFacetID
    }

    /**
     * getDeviceID
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-getDeviceID}
     * %%% END SOURCE CODE %%%
     * <p>This function retrieves the DeviceID.
     * 
     * <p>REG 1.2.1.2
     */
    private String getDeviceID() {
        Log.d(this.getClass().getCanonicalName(), "getDeviceID");
        // BEGIN: ClientEntrypoint-getDeviceID

        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        return deviceID;
        // END: ClientEntrypoint-getDeviceID
    }

    /**
     * getDeviceID
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ClientEntrypoint-getDeviceType}
     * %%% END SOURCE CODE %%%
     * <p>This function retrieves the Device Type.
     * 
     * <p>REG 1.2.1.3
     */
    private String getDeviceType() {
        Log.d(this.getClass().getCanonicalName(), "getDeviceType");
        // BEGIN: ClientEntrypoint-getDeviceType

        return Build.MODEL;
        // END: ClientEntrypoint-getDeviceType
    }

}
