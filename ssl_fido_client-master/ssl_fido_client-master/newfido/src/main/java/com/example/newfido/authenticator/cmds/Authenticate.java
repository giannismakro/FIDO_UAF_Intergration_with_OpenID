package com.example.newfido.authenticator.cmds;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.newfido.authenticator.assertions.AuthAssertion;
import com.example.newfido.authenticator.db.CountersDbHelper;
import com.example.newfido.authenticator.db.controllers.CountersController;
import com.example.newfido.authenticator.db.models.RawKeyHandle;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.tlv.ByteInputStream;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by sorin.teican on 06-Jan-17.
 */
 

public class Authenticate {

    private Context mContext;
    private CountersDbHelper mDb;
    private CountersController mCountersController;

    private short mAuthenticatorIndex = -1;
    private byte[] mUsername = null;
    private short mAttestationType = -1;
    private byte[] mAppID = null;
    private byte[] mFinalChallenge = null;
    private byte[] mKHAccessToken = null;
    private byte[] mTransactionContent = null;
    private byte[] mUserVerifyToken = null;
    private List<byte[]> mKeyHandles = null;

    public Authenticate(Context context) {
        mContext = context;
        mDb = new CountersDbHelper(mContext);
        mCountersController = new CountersController(mDb);
        mKeyHandles = new ArrayList<>();
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-process}
     * %%% END SOURCE CODE %%%
     * <p>This function prepares the response of the authenticator.
     * 
     * <p>AUTH 3.1
     * @see Authenticate#createResponse()
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    public byte[] process(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: Authenticate-process

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_SIGN_CMD_RESPONSE.id));
        value = createResponse(cmd);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

     
        return bout.toByteArray();
        // END: Authenticate-process
    }

    /**
     * createResponse
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-createResponse}
     * %%% END SOURCE CODE %%%
     * <p>This function creates the response of the authenticator.
     * 
     * <p>AUTH 3.1.1
     * @see Authenticate#getCmdParams()
     * @see Authenticate#createUsernameAndKeyHandleTag()
     * @see AuthAssertion#getAssertion()
     * @see Keystore
     * @see RawKeyHandle
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    private byte[] createResponse(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "createResponse");
        // BEGIN: Authenticate-createResponse

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        try {

            getCmdParams(cmd);
            Log.d(this.getClass().getCanonicalName(), "Parsed command parameter.");


            // Mix KHAccessToken with AppID.
            byte[] concat = new byte[mKHAccessToken.length + mAppID.length];
            System.arraycopy(mKHAccessToken, 0, concat, 0, mKHAccessToken.length);
            System.arraycopy(mAppID, 0, concat, mKHAccessToken.length, mAppID.length);
            mKHAccessToken = Keystore.SHA256(concat);

            // Unwrap all provided key handles.
            List<RawKeyHandle> providedKeyHandles = new ArrayList<>();
            for (byte[] wrapedHandle : mKeyHandles) {
                // Decrypt handle then use gson to convert to object from json.
                providedKeyHandles.add(new Gson().fromJson(new String(Keystore.decryptAES("wrap_sym",
                        Base64.decode(wrapedHandle, Base64.URL_SAFE | Base64.NO_WRAP))), RawKeyHandle.class));
            }

            Log.d(this.getClass().getCanonicalName(), "Privoded key handles size: " + providedKeyHandles.size());

            // Filter provided handles based on KHAccessToken.
            String b64KHAccessToken = Base64.encodeToString(mKHAccessToken, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            for (Iterator<RawKeyHandle> it = providedKeyHandles.iterator(); it.hasNext(); ) {
                RawKeyHandle handle = it.next();
                Log.d(this.getClass().getCanonicalName(), "KeyHandle access token: " + handle.KHAccessToken);
                Log.d(this.getClass().getCanonicalName(), "Computed access token: " + b64KHAccessToken);
                if (!handle.KHAccessToken.equals(b64KHAccessToken))
                    it.remove();
            }

            // UAF_CMD_STATUS_ACCESS_DENIED.
            if (providedKeyHandles.isEmpty()) {
                mDb.close();
                Log.d(this.getClass().getCanonicalName(), "Access denied");
                throw new Exception();
            }

            if (providedKeyHandles.size() > 1) {
                // Multiple handles available for the AppID.
                for (RawKeyHandle handle : providedKeyHandles)
                    bout.write(createUsernameAndKeyHandleTag(handle));
            } else {
                // Sign with key retrieved from keyhandle.
                value = AuthAssertion.getAssertion(providedKeyHandles.get(0).KeyID, mFinalChallenge, mCountersController);
                bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_ASSERTION.id));
                bout.write(UnsignedUtil.encodeInt(value.length));
                bout.write(value);
            }

            value = bout.toByteArray();
            bout.reset();

            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
            bout.write(UnsignedUtil.encodeInt(2));
            bout.write(UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_OK.id));

            bout.write(value);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(this.getClass().getCanonicalName(), "Error creating authentication assertion");

            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
            bout.write(UnsignedUtil.encodeInt(2));
            if (e.getMessage() != null && e.getMessage().equals("UAF_CMD_STATUS_CANNOT_RENDER_TRANSACTION_CONTENT")) {
                bout.write(UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_CANNOT_RENDER_TRANSACTION_CONTENT.id));
            } else {
                bout.write(UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_ERR_UNKNOWN.id));
            }
        }

        mDb.close();


        return bout.toByteArray();
        // END: Authenticate-createResponse
    }

    /**
     * createUsernameAndKeyHandleTag
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-createUsernameAndKeyHandleTag}
     * %%% END SOURCE CODE %%%
     * <p>This function creates pairs of username - keys.
     * 
     * <p>AUTH 3.1.1.2
     */
    private byte[] createUsernameAndKeyHandleTag(RawKeyHandle handle) throws Exception {
        // BEGIN: Authenticate-createUsernameAndKeyHandleTag
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_USERNAME.id));
        value = handle.Username.getBytes();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE.id));
        value = Keystore.encryptAES("wrap_sym", new Gson().toJson(handle).getBytes());
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = bout.toByteArray();
        bout.reset();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_USERNAME_AND_KEYHANDLE.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: Authenticate-createUsernameAndKeyHandleTag
    }

    /**
     * getCmdParams
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-getCmdParams}
     * %%% END SOURCE CODE %%%
     * <p>This function decodes the command sent by the ASM.
     * 
     * <p>AUTH 3.1.1.1
     * @see TagsEnum
     * 
     * @param cmd
     * @throws Exception
     */
    private void getCmdParams(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getCmdParams");
        // BEGIN: Authenticate-getCmdParams

        int tag, len;
        len = UnsignedUtil.read_UAFV1_UINT16(cmd);
        if (len <= 0) throw new Exception();

        while (cmd.available() > 0) {
            tag = UnsignedUtil.read_UAFV1_UINT16(cmd);

            if (tag == TagsEnum.TAG_AUTHENTICATOR_INDEX.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mAuthenticatorIndex = (short) UnsignedUtil.read_UAFV1_UINT16(cmd);
            } else if (tag == TagsEnum.TAG_APPID.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mAppID = cmd.read(len);
                //mAppID = UnsignedUtil.reverse(mAppID);
            } else if (tag == TagsEnum.TAG_FINAL_CHALLENGE.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mFinalChallenge = cmd.read(len);
                //mFinalChallenge = UnsignedUtil.reverse(mFinalChallenge);
            } else if (tag == TagsEnum.TAG_USERNAME.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mUsername = cmd.read(len);
                //mUsername = UnsignedUtil.reverse(mUsername);
            } else if (tag == TagsEnum.TAG_ATTESTATION_TYPE.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mAttestationType = (short) UnsignedUtil.read_UAFV1_UINT16(cmd);
                if (mAttestationType != TagsEnum.TAG_ATTESTATION_BASIC_FULL.id && mAttestationType != TagsEnum.TAG_ATTESTATION_BASIC_SURROGATE.id)
                    throw new Exception("UAF_CMD_STATUS_ATTESTATION_NOT_SUPPORTED");
            } else if (tag == TagsEnum.TAG_KEYHANDLE_ACCESS_TOKEN.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mKHAccessToken = cmd.read(len);
                //mKHAccessToken = UnsignedUtil.reverse(mKHAccessToken);
            } else if (tag == TagsEnum.TAG_TRANSACTION_CONTENT.id) {
                throw new Exception("UAF_CMD_STATUS_CANNOT_RENDER_TRANSACTION_CONTENT");
                //len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                //mTransactionContent = cmd.read(len);
                //mTransactionContent = UnsignedUtil.reverse(mTransactionContent);
            } else if (tag == TagsEnum.TAG_USERVERIFY_TOKEN.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mUserVerifyToken = cmd.read(len);
                //mUserVerifyToken = UnsignedUtil.reverse(mUserVerifyToken);
            } else if (tag == TagsEnum.TAG_KEYHANDLE.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                byte[] temp = cmd.read(len);
                //temp = UnsignedUtil.reverse(temp);
                mKeyHandles.add(temp.clone());
            }
        }
        // END: Authenticate-getCmdParams
    }


}
