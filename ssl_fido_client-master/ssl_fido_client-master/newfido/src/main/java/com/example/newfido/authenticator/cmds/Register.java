package com.example.newfido.authenticator.cmds;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.newfido.authenticator.assertions.RegAssertion;
import com.example.newfido.authenticator.db.CountersDbHelper;
import com.example.newfido.authenticator.db.controllers.CountersController;
import com.example.newfido.authenticator.db.models.RawKeyHandle;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.tlv.ByteInputStream;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

/**
 * Created by sorin.teican on 05-Jan-17.
 */
 
public class Register {

    private Context mContext;
    private CountersDbHelper mDb;
    private CountersController mCountersControllers;

    private short mAuthenticatorIndex = -1;
    private byte[] mAppID = null;
    private byte[] mFinalChallenge = null;
    private byte[] mUsername = null;
    private short mAttestationType = -1;
    private byte[] mKHAccessToken = null;
    private byte[] mUserVerifyToken = null;

    private String mDeviceID;
    private String mDeviceType;

    //private RawKeyHandle mRawKeyHandle;

    public Register(Context context, String deviceID, String deviceType) {
        mContext = context;
        mDb = new CountersDbHelper(mContext);
        mCountersControllers = new CountersController(mDb);
        //mRawKeyHandle = new RawKeyHandle();

        mDeviceID = deviceID;
        mDeviceType = deviceType;
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-process}
     * %%% END SOURCE CODE %%%
     * <p>This function prepares the response of the authenticator.
     * 
     * <p>REG 3.1
     * @see Register#createResponse(ByteInputStream)
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    public byte[] process(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: GetInfo-process

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_REGISTER_CMD_RESPONSE.id));
        value = createResponse(cmd);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: GetInfo-process
    }

    /**
     * createResponse
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-createResponse}
     * %%% END SOURCE CODE %%%
     * <p>This function creates the response of the authenticator.
     * 
     * <p>REG 3.1.1
     * @see RegAssertion#getAssertion(byte[], CountersController, short, String, String)
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    private byte[] createResponse(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "createResponse");
        // BEGIN: GetInfo-createResponse

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        try {
            getCmdParams(cmd);
            Log.d(this.getClass().getCanonicalName(), "Parse command parameters");

            // mix KHAccessToken with AppID.
            byte[] concat = new byte[mKHAccessToken.length + mAppID.length];
            System.arraycopy(mKHAccessToken, 0, concat, 0, mKHAccessToken.length);
            System.arraycopy(mAppID, 0, concat, mKHAccessToken.length, mAppID.length);
            mKHAccessToken = Keystore.SHA256(concat);

            // Generate registration assertion..
            byte[] regAssertion = RegAssertion.getAssertion(mFinalChallenge, mCountersControllers, mAttestationType, mDeviceID, mDeviceType);
            Log.d(this.getClass().getCanonicalName(), "Created assertion");

            // Create and wrap key handle - lacks uAuth.priv because we can`t extract keys from TEE.
            RawKeyHandle rawKeyHandle = new RawKeyHandle();
            rawKeyHandle.KHAccessToken = Base64.encodeToString(mKHAccessToken, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            rawKeyHandle.Username = new String(mUsername);
            rawKeyHandle.KeyID = RegAssertion.KeyID;
            byte[] wrapedKeyHandle = Keystore.encryptAES("wrap_sym", new Gson().toJson(rawKeyHandle).getBytes());
            //wrapedKeyHandle = UnsignedUtil.reverse(wrapedKeyHandle);

            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
            value = UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_OK.id);
            bout.write(UnsignedUtil.encodeInt(value.length));
            bout.write(value);

            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_ASSERTION.id));
            bout.write(UnsignedUtil.encodeInt(regAssertion.length));
            bout.write(regAssertion);

            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE.id));
            bout.write(UnsignedUtil.encodeInt(wrapedKeyHandle.length));
            bout.write(wrapedKeyHandle);

            Log.d(this.getClass().getCanonicalName(), "wraped key handle");

        } catch (Exception e) {
            e.printStackTrace();
            bout.reset();
            if (e.getMessage().equals("UAF_CMD_STATUS_ATTESTATION_NOT_SUPPORTED")) {
                Log.d(this.getClass().getCanonicalName(), "Attestation not supported");

                bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
                value = UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_ATTESTATION_NOT_SUPPORTED.id);
                bout.write(UnsignedUtil.encodeInt(value.length));
                bout.write(value);

                return bout.toByteArray();
            } else {
                //e.printStackTrace();
                Log.d(this.getClass().getCanonicalName(), "Exception unkown");
                bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
                value = UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_ERR_UNKNOWN.id);
                bout.write(UnsignedUtil.encodeInt(value.length));
                bout.write(value);

                return bout.toByteArray();
            }
        }

        mDb.close();

        return bout.toByteArray();
        // END: GetInfo-createResponse
    }

    /**
     * getCmdParams
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-getCmdParams}
     * %%% END SOURCE CODE %%%
     * <p>This function decodes the command sent by the ASM.
     * 
     * <p>REG 3.1.1.1
     * 
     * @param cmd
     * @throws Exception
     */
    private void getCmdParams(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getCmdParams");
        // BEGIN: GetInfo-getCmdParams

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
            } else if (tag == TagsEnum.TAG_USERVERIFY_TOKEN.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mUserVerifyToken = cmd.read(len);
                //mUserVerifyToken = UnsignedUtil.reverse(mUserVerifyToken);
            }
        }
        // END: GetInfo-getCmdParams
    }

}
