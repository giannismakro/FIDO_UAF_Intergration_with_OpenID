package com.example.newfido.asm.cmds;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import com.example.newfido.asm.db.AuthenticatorIndexDbHelper;
import com.example.newfido.asm.db.KeyHandleDbHelper;
import com.example.newfido.asm.db.controllers.AuthenticatorIndexController;
import com.example.newfido.asm.db.controllers.KeyHandleController;
import com.example.newfido.asm.db.models.AuthenticatorIndex;
import com.example.newfido.asm.db.models.KeyHandle;
import com.example.newfido.authenticator.AuthenticatorInterface;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.msg.asm.obj.RequestData;
import com.example.newfido.msg.asm.obj.ResponseData;
import com.example.newfido.tlv.Tags;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.TlvAssertionParser;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 13-Jan-17.
 */
 

public class Register {

    private Context mContext;

    private KeyHandleDbHelper mKeyhandleDb;
    private AuthenticatorIndexDbHelper mAuthenticatorIndexDb;
    private KeyHandleController mKeyHandleController;
    private AuthenticatorIndexController mAutenticatorIndexController;

    private String mCallerID;
    private String mPersonaID;
    private String mASMToken;

    private String mDeviceID;
    private String mDeviceType;

    public Register(Context context, String CallerID, String PersonaID, String ASMToken, String deviceID, String deviceType) {
        mContext = context;

        mKeyhandleDb = new KeyHandleDbHelper(mContext);
        mAuthenticatorIndexDb = new AuthenticatorIndexDbHelper(mContext);
        mKeyHandleController = new KeyHandleController(mKeyhandleDb);
        mAutenticatorIndexController = new AuthenticatorIndexController(mAuthenticatorIndexDb);

        mCallerID = CallerID;
        mPersonaID = PersonaID;
        mASMToken = ASMToken;

        mDeviceID = deviceID;
        mDeviceType = deviceType;
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-process}
     * %%% END SOURCE CODE %%%
     * <p>This function initiate the communitation with authenticator.
     * 
     * <p>REG 2.5.1.1.1.2.1
     * <p>REG 4
     * @see ASMResponse
     * @see Register#checkAuthenticatorIndex(int)
     * @see Register#buildCmd(ASMRequest)
     * @see AuthenticatorInterface #command(com.example.newfido.authenticator.Context, byte[], String, String)
     * 
     * @see TlvAssertionParser
     * @see Tags
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public ASMResponse process(ASMRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: Register-process

        ASMResponse asmResponse = new ASMResponse();

        if (!checkAuthenticatorIndex(request.authenticatorIndex)) {
            closeDbs();

            asmResponse.responseData = null;
            asmResponse.statusCode = 0x01;

            return asmResponse;
        }

        try {
            Log.d(this.getClass().getCanonicalName(), "Sending register request to authenticator");
            byte[] response = AuthenticatorInterface.command(mContext, buildCmd(request), mDeviceID, mDeviceType);
            Log.d(this.getClass().getCanonicalName(), "Received register response from authenticator");
            String b64Response = Base64.encodeToString(response, Base64.URL_SAFE);

            TlvAssertionParser parser = new TlvAssertionParser();
            Tags tlvTags = parser.parse(b64Response);
            if (extractStatusCode(tlvTags) != 0x00) {
                Log.d(this.getClass().getCanonicalName(), "Invalid status code from authenticator");

                closeDbs();

                asmResponse.responseData = null;
                asmResponse.statusCode = 0x01;
                return asmResponse;
            }
            storeKeyHandle(tlvTags, request.args.appID);

            ResponseData registerOut = new ResponseData();
            registerOut.assertionScheme = "UAFV1TLV";
            registerOut.assertion = Base64.encodeToString(tlvTags.getTags()
                    .get(TagsEnum.TAG_AUTHENTICATOR_ASSERTION.id).get(0).value, Base64.URL_SAFE);

            asmResponse.responseData = registerOut;
            asmResponse.statusCode = 0x00;
        } catch (Exception e) {
            e.printStackTrace();
            asmResponse.responseData = null;
            asmResponse.statusCode = 0x01;
        }

        closeDbs();

        return asmResponse;
        // END: Register-process
    }

    private short extractStatusCode(Tags tags) {
        return ByteBuffer.allocate(2).wrap(tags.getTags().get(TagsEnum.TAG_STATUS_CODE.id).get(0).value).getShort();
    }

    /**
     * buildCmd
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-buildCmd}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the requests before forwarding the command to the authenticator.
     * 
     * <p>REG 2.5.1.1.1.2.1.1
     * @see RequestData
     * 
     * @param request
     * @return
     * @throws Exception
     */
    private byte[] buildCmd(ASMRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "buildCmd");
        // BEGIN: Register-buildCmd

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        RequestData registerIn = request.args;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_INDEX.id));
        //value = UnsignedUtil.encodeInt(request.authenticatorIndex);
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(request.authenticatorIndex));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_APPID.id));
        value = registerIn.appID.getBytes();
        //UnsignedUtil.reverse(value);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        value = registerIn.finalChallenge.getBytes();
        value = Keystore.SHA256(value);
        //UnsignedUtil.reverse(value);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_USERNAME.id));
        value = registerIn.username.getBytes();
        //UnsignedUtil.reverse(value);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_TYPE.id));
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(registerIn.attestationType));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE_ACCESS_TOKEN.id));
        value = getKHAccessToken(registerIn.appID);
        //UnsignedUtil.reverse(value);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = bout.toByteArray();
        bout.reset();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_REGISTER_CMD.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: Register-buildCmd
    }

    /**
     * getKHAccessToken
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-getKHAccessToken}
     * %%% END SOURCE CODE %%%
     * <p>This function creates a token.
     * 
     * <p>REG 2.5.1.1.1.2.1.1.1
     * 
     * @param AppID
     * @return
     * @throws Exception
     */
    private byte[] getKHAccessToken(String AppID) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getKHAccessToken");
        // BEGIN: Register-getKHAccessToken

        String KHAccessToken = AppID + mASMToken + mPersonaID + mCallerID;
        byte[] token = Keystore.SHA256(KHAccessToken.getBytes());

        Log.d(this.getClass().getCanonicalName(), "AppID: " + AppID);
        Log.d(this.getClass().getCanonicalName(), "ASMToken: " + mASMToken);
        Log.d(this.getClass().getCanonicalName(), "PersonaID: " + mPersonaID);
        Log.d(this.getClass().getCanonicalName(), "CallerID: " + mCallerID);

        Log.d(this.getClass().getCanonicalName(), "Access token generated by ASM: " + Base64.encodeToString(token, Base64.DEFAULT | Base64.NO_WRAP | Base64.NO_PADDING));

        return token;
        // END: Register-getKHAccessToken
    }

    /**
     * storeKeyHandle
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-storeKeyHandle}
     * %%% END SOURCE CODE %%%
     * <p>This function prepares a key handle.
     * 
     * <p>REG 4.1
     * @see KeyHandle
     * 
     * @param tlvTags
     * @param AppID
     */
    private void storeKeyHandle(Tags tlvTags, String AppID) {
        Log.d(this.getClass().getCanonicalName(), "storeKeyHandle");
        // BEGIN: Register-storeKeyHandle

        byte[] KRD = tlvTags.getTags().get(TagsEnum.TAG_KEYHANDLE.id).get(0).value;
        //UnsignedUtil.reverse(KRD);

        byte[] KeyID = tlvTags.getTags().get(TagsEnum.TAG_KEYID.id).get(0).value;
        //UnsignedUtil.reverse(KeyID);

        KeyHandle keyHandle = new KeyHandle();
        keyHandle.AppID = AppID;
        keyHandle.CallerID = mCallerID;
        keyHandle.CurrentTimestamp = System.currentTimeMillis();
        keyHandle.TAG_KEYHANDLE = Base64.encodeToString(KRD, Base64.URL_SAFE | Base64.NO_WRAP);
        keyHandle.TAG_KEYID = Base64.encodeToString(KeyID, Base64.URL_SAFE | Base64.NO_WRAP);
        //keyHandle.Username = username;

        mKeyHandleController.insertKeyHandle(keyHandle);
        // END: Register-storeKeyHandle
    }

    /**
     * checkAuthenticatorIndex
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Register-checkAuthenticatorIndex}
     * %%% END SOURCE CODE %%%
     * <p>This finction checks if the index of the authenticator is defined.
     * 
     * <p>REG
     * 
     * @param index
     * @return
     */
    private boolean checkAuthenticatorIndex(int index) {
        Log.d(this.getClass().getCanonicalName(), "checkAuthenticatorIndex");
        // BEGIN: Register-checkAuthenticatorIndex

        List<AuthenticatorIndex> authenticators = mAutenticatorIndexController.getAllIndexes();
        for (AuthenticatorIndex authenticator : authenticators)
            if (authenticator.index == index)
                return true;

        return false;
        // END: Register-checkAuthenticatorIndex
    }

    private void closeDbs() {
        mAuthenticatorIndexDb.close();
        mKeyhandleDb.close();
    }
}
