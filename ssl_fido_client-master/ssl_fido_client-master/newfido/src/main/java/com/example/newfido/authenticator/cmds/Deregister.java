package com.example.newfido.authenticator.cmds;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.example.newfido.asm.db.KeyHandleDbHelper;
import com.example.newfido.asm.db.controllers.KeyHandleController;
import com.example.newfido.asm.db.models.KeyHandle;
import com.example.newfido.authenticator.db.CountersDbHelper;
import com.example.newfido.authenticator.db.controllers.CountersController;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.tlv.ByteInputStream;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 10-Jan-17.
 */
 

public class Deregister {

    private Context mContext;
    private CountersDbHelper mDb;
    private CountersController mCountersControllers;
    private KeyHandleController mKeyHandleController;


    private short mAuthenticatorIndex = -1;
    private byte[] mAppID = null;
    private byte[] mKeyID = null;
    private byte[] mKHAccessToken = null;

    public Deregister(Context context) {
        mContext = context;

        mDb = new CountersDbHelper(mContext);
        KeyHandleDbHelper mDbKeyhandle = new KeyHandleDbHelper(mContext);

        mCountersControllers = new CountersController(mDb);
        mKeyHandleController = new KeyHandleController(mDbKeyhandle);
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-process}
     * %%% END SOURCE CODE %%%
     * <p>This function prepares the response of the authenticator.
     * 
     * <p>DEREG 3.1
     * @see Deregister#createResponse(ByteInputStream)
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    public byte[] process(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: Deregister-process

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_DEREGISTER_CMD_RESPONSE.id));
        value = createResponse(cmd);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: Deregister-process
    }

    /**
     * createResponse
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-createResponse}
     * %%% END SOURCE CODE %%%
     * <p>This function creates the response of the authenticator.
     * 
     * <p>DEREG 3.1.1
     * @see Deregister#getCmdParams(ByteInputStream)
     * @see Keystore
     */
    private byte[] createResponse(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "createResponse");
        // BEGIN: Deregister-createResponse

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        try {

            getCmdParams(cmd);

            // mix KHAccessToken with AppID.
            byte[] concat = new byte[mKHAccessToken.length + mAppID.length];
            System.arraycopy(mKHAccessToken, 0, concat, 0, mKHAccessToken.length);
            System.arraycopy(mAppID, 0, concat, mKHAccessToken.length, mAppID.length);
            mKHAccessToken = Keystore.SHA256(concat);

//            List<KeyHandle> keyHandles = lookUpKeyHandles(new String(mAppID), new String[] { new String(mKeyID) });
//            List<RawKeyHandle> rawKeyHandles = new ArrayList<>();
//
//            if (keyHandles.size() > 1 || keyHandles.isEmpty())
//                throw new Exception();
//
//            for (KeyHandle keyHandle : keyHandles) {
//                rawKeyHandles.add(new Gson().fromJson(new String(Keystore.decryptAES("wrap_sym",
//                        Base64.decode(keyHandle.TAG_KEYHANDLE, Base64.DEFAULT))), RawKeyHandle.class));
//            }
//
//            // Filter provided handles based on KHAccessToken.
//            String b64KHAccessToken = Base64.encodeToString(mKHAccessToken, Base64.DEFAULT);
//            for (Iterator<RawKeyHandle> it = rawKeyHandles.iterator(); it.hasNext(); ) {
//                RawKeyHandle handle = it.next();
//                if (!handle.KHAccessToken.equals(b64KHAccessToken))
//                    it.remove();
//            }
//
//            if (rawKeyHandles.isEmpty() || rawKeyHandles.size() > 1)
//                throw new Exception();

            String KeyID = new String(mKeyID);
            Keystore.deleteKeyWithID(KeyID);
            mCountersControllers.deleteCounter(KeyID);

            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
            value = UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_OK.id);
            bout.write(UnsignedUtil.encodeInt(value.length));
            bout.write(value);
        } catch (Exception e) {
            e.printStackTrace();
            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
            value = UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_ERR_UNKNOWN.id);
            bout.write(UnsignedUtil.encodeInt(value.length));
            bout.write(value);
        }

        mDb.close();

        return bout.toByteArray();
        // END: Deregister-createResponse
    }

    /**
     * getCmdParams
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-getCmdParams}
     * %%% END SOURCE CODE %%%
     * <p>This function decodes the command sent by the ASM.
     * 
     * <p>DEREG 3.1.1.1
     */
    private void getCmdParams(ByteInputStream cmd) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getCmdParams");
        // BEGIN: Deregister-getCmdParams

        int tag, len;

        while (cmd.available() > 0) {
            tag = UnsignedUtil.read_UAFV1_UINT16(cmd);

            if (tag == TagsEnum.TAG_AUTHENTICATOR_INDEX.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mAuthenticatorIndex = (short) UnsignedUtil.read_UAFV1_UINT16(cmd);
            } else if (tag == TagsEnum.TAG_APPID.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mAppID = cmd.read(len);
                //mAppID = UnsignedUtil.reverse(mAppID);
            }  else if (tag == TagsEnum.TAG_KEYHANDLE_ACCESS_TOKEN.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mKHAccessToken = cmd.read(len);
                //mKHAccessToken = UnsignedUtil.reverse(mKHAccessToken);
            } else if (tag == TagsEnum.TAG_KEYID.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mKeyID = cmd.read(len);
                //mKeyID = UnsignedUtil.reverse(mKeyID);
            }
        }
        // END: Deregister-getCmdParams
    }

    private List<KeyHandle> lookUpKeyHandles(String AppID, String[] KeyIDs) {
        List<KeyHandle> foundHandles = new ArrayList<>();

        List<KeyHandle> handles = mKeyHandleController.getAllKeyHandles();
        for (KeyHandle handle : handles) {
            if (handle.AppID.equals(AppID)) {
                for (String KeyID : KeyIDs) {
                    if (handle.TAG_KEYID.equals(KeyID))
                        foundHandles.add(handle);
                }
            }
        }

        return foundHandles;
    }
}
