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
import com.example.newfido.authenticator.AuthenticatorInterface;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.msg.asm.obj.RequestData;
import com.example.newfido.tlv.Tags;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.TlvAssertionParser;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 13-Feb-17.
 */

 

public class Deregister {

    private Context mContext;

    KeyHandleDbHelper mKeyHandleDbHelper;
    AuthenticatorIndexDbHelper mAuthenticatorIndexDbHelper;

    private KeyHandleController mKeyHandleController;
    private AuthenticatorIndexController mAuthenticatorIndexController;

    private String mCallerID;
    private String mPersonaID;
    private String mASMToken;

    public Deregister(Context context, String CallerID, String PersonaID, String ASMToken) {
        mContext = context;

        mKeyHandleDbHelper = new KeyHandleDbHelper(mContext);
        mKeyHandleController = new KeyHandleController(mKeyHandleDbHelper);

        mAuthenticatorIndexDbHelper = new AuthenticatorIndexDbHelper(mContext);
        mAuthenticatorIndexController = new AuthenticatorIndexController(mAuthenticatorIndexDbHelper);

        mCallerID = CallerID;
        mPersonaID = PersonaID;
        mASMToken = ASMToken;
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-process}
     * %%% END SOURCE CODE %%%
     * <p>This function initiates the communitation with authenticator.
     * 
     * <p>DEREG 2.5.1.1.1.2.1
     * <p>DEREG 4
     * @see ASMResponse
     * @see Deregister#checkAuthenticatorIndex(int)
     * @see Deregister#buildCmd(ASMRequest)
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
        // BEGIN: Deregister-process

        ASMResponse asmResponse = new ASMResponse();

        if (!checkAuthenticatorIndex(request.authenticatorIndex)) {
            Log.d(this.getClass().getCanonicalName(), "No authenticator");
            closeDbs();

            asmResponse.statusCode = 0x01;
            return asmResponse;
        }

        try {
            byte[] response = AuthenticatorInterface.command(mContext, buildCmd(request), null, null);
            String b64Response = Base64.encodeToString(response, Base64.URL_SAFE);

            TlvAssertionParser parser = new TlvAssertionParser();
            Tags tlvTags = parser.parse(b64Response);

            if (extractStatusCode(tlvTags) != 0x00) {
                Log.d(this.getClass().getCanonicalName(), "Invalid authenticator status code");
                //asmResponse.responseData = null;
                asmResponse.statusCode = 0x01;
                return asmResponse;
            }

        } catch (Exception e) {
            e.printStackTrace();
            closeDbs();

            asmResponse.statusCode = 0x01;
            return asmResponse;
        }

        mKeyHandleController.deleteKeyHandle(request.args.appID, request.args.keyID);

        closeDbs();

        asmResponse.statusCode = 0x00;
        return asmResponse;
        // END: Deregister-process
    }

    private void closeDbs() {
        mAuthenticatorIndexDbHelper.close();
        mKeyHandleDbHelper.close();
    }

    private short extractStatusCode(Tags tags) {
        return ByteBuffer.allocate(2).wrap(tags.getTags().get(TagsEnum.TAG_STATUS_CODE.id).get(0).value).getShort();
    }

    /**
     * buildCmd
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-buildCmd}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the requests before forwarding the command to the authenticator.
     * 
     * <p>DEREG 2.5.1.1.1.2.1.1
     * @see RequestData
     * 
     * @param request
     * @return
     * @throws Exception
     */
    private byte[] buildCmd(ASMRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "buildCmd");
        // BEGIN: Deregister-buildCmd

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        RequestData deregisterIn = request.args;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_INDEX.id));
        //value = UnsignedUtil.encodeInt(request.authenticatorIndex);
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(request.authenticatorIndex));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_APPID.id));
        value = deregisterIn.appID.getBytes();
        //UnsignedUtil.reverse(value);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYID.id));
        value = deregisterIn.keyID.getBytes();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE_ACCESS_TOKEN.id));
        value = getKHAccessToken(deregisterIn.appID);
        //UnsignedUtil.reverse(value);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = bout.toByteArray();
        bout.reset();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_DEREGISTER_CMD.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: Deregister-buildCmd
    }

    /**
     * getKHAccessToken
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Deregister-getKHAccessToken}
     * %%% END SOURCE CODE %%%
     * <p>This function creates a token.
     * 
     * <p>DEREG 2.5.1.1.1.2.1.1.1
     * 
     */
    private byte[] getKHAccessToken(String AppID) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getKHAccessToken");
        // BEGIN: Deregister-getKHAccessToken

        String KHAccessToken = AppID + mASMToken + mPersonaID + mCallerID;
        byte[] token = Keystore.SHA256(KHAccessToken.getBytes());

        Log.d(this.getClass().getCanonicalName(), "ASMToken: " + mASMToken);
        Log.d(this.getClass().getCanonicalName(), "PersonaID: " + mPersonaID);
        Log.d(this.getClass().getCanonicalName(), "CallerID: " + mCallerID);

        Log.d(this.getClass().getCanonicalName(), "Access token generated by ASM: " + Base64.encodeToString(token, Base64.DEFAULT));

        return token;
        // END: Deregister-getKHAccessToken
    }

    private boolean checkAuthenticatorIndex(int index) {
        Log.d(this.getClass().getCanonicalName(), "checkAuthenticatorIndex");

        List<AuthenticatorIndex> authenticators = mAuthenticatorIndexController.getAllIndexes();
        for (AuthenticatorIndex authenticator : authenticators)
            if (authenticator.index == index)
                return true;

        return false;
    }
}
