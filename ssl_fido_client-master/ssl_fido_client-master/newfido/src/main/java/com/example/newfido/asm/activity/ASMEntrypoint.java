package com.example.newfido.asm.activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.newfido.asm.ASMInterface;
import com.example.newfido.asm.cmds.Authenticate;
import com.example.newfido.authenticator.db.CountersDbHelper;
import com.example.newfido.authenticator.db.controllers.CountersController;
import com.example.newfido.authenticator.db.models.Counter;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.google.gson.Gson;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.example.newfido.R;



public class ASMEntrypoint extends Activity {

    private final int REQUEST_DEVICE_CREDENTIALS = 1234;
    private final String ASM_TOKEN_PREF_KEY = "com.example.newfido.asm.ASM_TOKEN_PREF_KEY";
    private final String INIT_AUTHENTICATOR_KEY = "com.example.newfido.asm.INIT_AUTHENTICATOR_KEY";
    private final String INIT_GLOBAL_COUNTERS = "com.example.newfido.asm.INIT_GLOBAL_COUNTERS";

    private Intent mCallingIntent;

    private ArrayAdapter<String> mListAdapter;
    private List<String> mUsernames;

    private String mASMToken;

    private ViewFlipper mViewFlipper;
    private ListView mListView;

    private TextView tv_operation, tv_to, tv_rp;

    private Authenticate.IUsernameSelected mUsernameSelected;

    /**
     * onCreate
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-onCreate}
     * %%% END SOURCE CODE %%%
     * <p>Called when the activity is starting.
     * 
     * <p>DISC 2
     * <p>REG 2
     * <p>AUTH 2
     * <p>DEREG 2
     * 
     * @see ASMEntrypoint#initAuthenticator()
     * @see ASMEntrypoint#initGlobalCounters()
     * @see ASMEntrypoint#initASMToken()
     * @see ASMEntrypoint#initMembers()
     * @see ASMEntrypoint#checkGetInfo()
     * 
     * @param savedInstanceState A mapping from String keys to various Parcelable values.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this.getClass().getCanonicalName(), "onCreate");
        // BEGIN: ASMEntrypoint-onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asmentrypoint);

        Log.d(this.getClass().getCanonicalName(), "Entered ASMEntrypoint");

        initAuthenticator();
        initGlobalCounters();
        initASMToken();
        initMembers();

        checkGetInfo();
        //initView();
        // END: ASMEntrypoint-onCreate
    }

    private void initView() {
        String request = mCallingIntent.getStringExtra("message");
        String host;
        try {
            ASMRequest _req = new Gson().fromJson(request, ASMRequest.class);
            if (_req.args.appID == null || _req.args.appID.isEmpty())
                return;
            URL url = new URL(_req.args.appID);
            host = url.getHost();
        } catch (Exception e) {
            return;
        }

        tv_to.setText("to");
        //tv_rp.setText(host);

        if (request.indexOf("Register") > 0) {
            tv_operation.setText("Register");

        } else if (request.indexOf("Authenticate") > 0) {
            tv_operation.setText("Authenticate");
        } else if (request.indexOf("Deregister") > 0) {
            tv_operation.setText("Deregister");
            tv_to.setText("from");
        }
    }

    /**
     * checkGetInfo
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-checkGetInfo}
     * %%% END SOURCE CODE %%%
     * <p>This function initializes the proper process based on request type.
     * 
     * <p>DISC 2.5
     * <p>REG 2.5
     * <p>AUTH 2.5
     * <p>DEREG 2.5
     * @see ASMEntrypoint#processIntent()
     * @see ASMEntrypoint#proceed()
     */
    private void checkGetInfo() {
        Log.d(this.getClass().getCanonicalName(), "checkGetInfo");
        // BEGIN: ASMEntrypoint-checkGetInfo
        String request = mCallingIntent.getStringExtra("message");
        if (request.indexOf("GetInfo") > 0) {
            try {
                processIntent();
            } catch (Exception e) {
                Log.d(this.getClass().getCanonicalName(), "Error checking message type.");
                e.printStackTrace();
                returnError();
            }
        } else {
            proceed();
        }
        // END: ASMEntrypoint-checkGetInfo
    }

    /**
     * initMembers
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-initMembers}
     * %%% END SOURCE CODE %%%
     * <p>This function initializes the authenticators.
     * 
     * <p>DISC 2.4
     * <p>REG 2.4
     * <p>AUTH 2.4
     * <p>DEREG 2.4
     */
    private void initMembers() {
        Log.d(this.getClass().getCanonicalName(), "initMembers");
        // BEGIN: ASMEntrypoint-initMembers
        mCallingIntent = getIntent();

        mViewFlipper = (ViewFlipper) findViewById(R.id.vf_holder);
        mViewFlipper.setAutoStart(false);
        mViewFlipper.setFlipInterval(0);
        mViewFlipper.setInAnimation(null);
        mViewFlipper.setOutAnimation(null);

        mListView = (ListView) findViewById(R.id.lv_users);
        mUsernames = new ArrayList<>();
        mListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mUsernames);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsernameSelected.selected(position);
                mViewFlipper.showPrevious();
            }
        });

        //tv_operation = (TextView) findViewById(R.id.fc_tv_operation);
        //tv_to = (TextView) findViewById(R.id.fc_tv_to);
        //tv_rp = (TextView) findViewById(R.id.fc_tv_relyingParty);
        // END: ASMEntrypoint-initMembers
    }

    /**
     * processIntent
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-processIntent}
     * %%% END SOURCE CODE %%%
     * <p>This function forwards the request to Authenticator.
     * 
     * <p>DISC 2.5.1
     * <p>REG 2.5.1.1.1
     * <p>AUTH 2.5.1.1.1
     * <p>DEREG 2.5.1.1.1
     * @see ASMInterface
     * @see ASMEntrypoint#getCallerID(PackageInfo)
     * @see ASMEntrypoint#getPersonaID()
     * @see ASMInterface#command(String)
     * 
     * @throws Exception
     */
    private void processIntent() throws Exception {
        Log.d(this.getClass().getCanonicalName(), "processIntent");
        // BEGIN: ASMEntrypoint-processIntent
        String message = mCallingIntent.getStringExtra("message");
        String deviceID = mCallingIntent.getStringExtra("deviceID");
        String deviceType = mCallingIntent.getStringExtra("deviceType");

        PackageManager pm = getPackageManager();
        String CallerID = getCallerID(pm.getPackageInfo(getCallingPackage(), pm.GET_SIGNATURES));
        String PersonaID = getPersonaID();


        ASMInterface asmInterface = new ASMInterface(this, CallerID, PersonaID, mASMToken, deviceID, deviceType);
        String response = asmInterface.command(message);

        Log.d(this.getClass().getCanonicalName(), "Created assertion");

        Intent result = new Intent();
        result.putExtra("message", response);
        setResult(RESULT_OK, result);
        finish();
        // END: ASMEntrypoint-processIntent
    }

    /**
     * showUsernames
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-showUsernames}
     * %%% END SOURCE CODE %%%
     * <p>This function displays the username of the authentiator.
     * 
     * <p>AUTH
     * 
     * @param usernames
     * @param usernameSelected
     */
    public void showUsernames(List<String> usernames, Authenticate.IUsernameSelected usernameSelected) {
        Log.d(this.getClass().getCanonicalName(), "showUsernames");
        // BEGIN: ASMEntrypoint-showUsernames
        mUsernameSelected = usernameSelected;

        mUsernames.clear();
        mUsernames.addAll(usernames);

        mListAdapter.notifyDataSetChanged();

        mViewFlipper.showNext();
        // END: ASMEntrypoint-showUsernames
    }

    /**
     * initASMToken
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-initASMToken}
     * %%% END SOURCE CODE %%%
     * <p>This function initializes the ASM Token.
     * 
     * <p>DISC 2.3
     * <p>REG 2.3
     * <p>AUTH 2.3
     * <p>DEREG 2.3
     * 
     * @see ASMEntrypoint#generateASMToken()
     */
    private void initASMToken() {
        Log.d(this.getClass().getCanonicalName(), "initASMToken");
        // BEGIN: ASMEntrypoint-initASMToken

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        String ASMToken = prefs.getString(ASM_TOKEN_PREF_KEY, null);
        if (ASMToken == null) {
            mASMToken = generateASMToken();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ASM_TOKEN_PREF_KEY, mASMToken);
            editor.commit();

            Log.d(this.getClass().getCanonicalName(), "ASMToken initialized");
        }
        mASMToken = ASMToken;
        // END: ASMEntrypoint-initASMToken
    }

    /**
     * initAuthenticator
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-initAuthenticator}
     * %%% END SOURCE CODE %%%
     * <p>This function initializes the authenticator.
     * 
     * <p>DISC 2.1
     * <p>REG 2.1
     * <p>AUTH 2.1
     * <p>DEREG 2.1
     */
    private void initAuthenticator() {
        Log.d(this.getClass().getCanonicalName(), "initAuthenticator");
        // BEGIN: ASMEntrypoint-initAuthenticator

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        boolean initAuthenticator = prefs.getBoolean(INIT_AUTHENTICATOR_KEY, false);
        if (initAuthenticator == false) {
            try {
                Keystore.initAuthenticator();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(INIT_AUTHENTICATOR_KEY, true);
                editor.commit();

                Log.d(this.getClass().getCanonicalName(), "Authenticator initialized");
            } catch (Exception e) {
                Log.d(this.getClass().getCanonicalName(), "Error initing authenticator");
                e.printStackTrace();
                returnError();
            }
        }
        // END: ASMEntrypoint-initAuthenticator
    }

    /**
     * initGlobalCounters
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-initGlobalCounters}
     * %%% END SOURCE CODE %%%
     * <p>This function initializes the counters of the FIDO Client.
     * 
     * <p>DISC 2.2
     * <p>REG 2.2
     * <p>AUTH 2.2
     * <p>DEREG 2.2
     * 
     * @see CountersDbHelper
     * @see CountersController
     * @see Counter
     */
    private void initGlobalCounters() {
        Log.d(this.getClass().getCanonicalName(), "initGlobalCounters");
        // BEGIN: ASMEntrypoint-initGlobalCounters

        CountersDbHelper db = new CountersDbHelper(this);
        CountersController countersControllers = new CountersController(db);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean initGlobalCounters = prefs.getBoolean(INIT_GLOBAL_COUNTERS, false);
        if (initGlobalCounters == false) {
            try {
                Counter regCounter = new Counter();
                regCounter.context = "global_register";
                regCounter.type = "register";
                regCounter.value = 0;

                Counter signCounter = new Counter();
                signCounter.context = "global_sign";
                regCounter.type = "sign";
                regCounter.value = 0;

                countersControllers.insertCounter(regCounter);
                countersControllers.insertCounter(signCounter);

                db.close();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(INIT_AUTHENTICATOR_KEY, true);
                editor.commit();

                Log.d(this.getClass().getCanonicalName(), "Authenticator initialized");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(this.getClass().getCanonicalName(), "Init global counters error");
                returnError();
            }
        }
        // END: ASMEntrypoint-initGlobalCounters
    }

    /**
     * getCallerID
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-getCallerID}
     * %%% END SOURCE CODE %%%
     * <p>This function decode callerID from Base64 to text.
     * 
     * <p>DISC
     * <p>REG
     * <p>AUTH
     * <p>DEREG
     * 
     * @param packageInfo
     * @return
     * @throws Exception
     */
    private String getCallerID(PackageInfo packageInfo) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getCallerID");
        // BEGIN: ASMEntrypoint-getCallerID

        // TODO nikosev revert
        // return Base64.encodeToString(Keystore.SHA256(packageInfo.signatures[0].toByteArray()), Base64.DEFAULT);
        return "yu23F6N010hHZyCnZMZeEQDfsi7h/rajhISuFazzbdc=";
        // END: ASMEntrypoint-getCallerID
    }

    /**
     * getPersonaID
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-getPersonaID}
     * %%% END SOURCE CODE %%%
     * <p>This function returns personaID.
     * 
     * <p>DISC 2.5.1.1
     * <p>REG 2.5.1.1.1.1
     * <p>AUTH 2.5.1.1.1.1
     * <p>DEREG 2.5.1.1.1.1
     */
    private String getPersonaID() {
        Log.d(this.getClass().getCanonicalName(), "getPersonaID");
        // BEGIN: ASMEntrypoint-getPersonaID
//        UserManager um = (UserManager)getSystemService(Context.USER_SERVICE);
//        return um.getUserName();
        return "UPRC";
        // END: ASMEntrypoint-getPersonaID
    }

    /**
     * generateASMToken
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-generateASMToken}
     * %%% END SOURCE CODE %%%
     * <p>Creates a random value.
     * 
     * <p>DISC
     * <p>REG
     * <p>AUTH
     * <p>DEREG
     * 
     * @return
     */
    private String generateASMToken() {
        Log.d(this.getClass().getCanonicalName(), "generateASMToken");
        // BEGIN: ASMEntrypoint-generateASMToken

        SecureRandom random = new SecureRandom();
        byte[] asmToken = new byte[32];
        random.nextBytes(asmToken);

        return Base64.encodeToString(asmToken, Base64.URL_SAFE | Base64.NO_WRAP);
        // END: ASMEntrypoint-generateASMToken
    }

    /**
     * proceed
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-proceed}
     * %%% END SOURCE CODE %%%
     * <p>This function proceeds the operation if the user confirms his/her identity successfuly.
     * 
     * <p>REG 2.5.1
     * <p>AUTH 2.5.1
     * <p>DEREG 2.5.1
     */
    public void proceed() {
        Log.d(this.getClass().getCanonicalName(), "proceed");
        // BEGIN: ASMEntrypoint-proceed

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("UPRC", "Confirm your identity: Insert pin, pattern or swipe your finger");
        if (intent != null) {
            Log.d(this.getClass().getCanonicalName(), "Starting keyguard service");
            startActivityForResult(intent, REQUEST_DEVICE_CREDENTIALS);
        } else {
            returnError();
        }
        // END: ASMEntrypoint-proceed
    }

//    public void decline(View v) {
//        Gson gson = new Gson();
//
//        ASMResponse asmResponse = new ASMResponse();
//        asmResponse.statusCode = 0x03;
//
//        Intent intent = new Intent();
//        intent.putExtra("message", gson.toJson(asmResponse));
//
//        setResult(RESULT_CANCELED, intent);
//        finish();
//    }

    private void returnError() {
        Gson gson = new Gson();

        ASMResponse asmResponse = new ASMResponse();
        asmResponse.statusCode = 0x02;

        Intent intent = new Intent();
        intent.putExtra("message", gson.toJson(asmResponse));

        setResult(RESULT_CANCELED, intent);
        finish();;
    }

    /**
     * onActivityResult
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMEntrypoint-onActivityResult}
     * %%% END SOURCE CODE %%%
     * <p>This function proceeds the operation to ASM.
     * 
     * <p>REG 2.5.1.1
     * <p>AUTH 2.5.1.1
     * <p>DEREG 2.5.1.1
     * 
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this.getClass().getCanonicalName(), "onActivityResult");
        // BEGIN: ASMEntrypoint-onActivityResult

        if (requestCode == REQUEST_DEVICE_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                try {
                    processIntent();
                } catch (Exception e) {
                    Log.d(this.getClass().getCanonicalName(), "Error processing intent.");
                    e.printStackTrace();
                    returnError();
                }
            } else {
                Log.d(this.getClass().getCanonicalName(), "User canceled consent");
                returnError();
            }
        } else {
            Log.d(this.getClass().getCanonicalName(), "User canceled consent.");
            returnError();
        }
        // END: ASMEntrypoint-onActivityResult
    }

}
