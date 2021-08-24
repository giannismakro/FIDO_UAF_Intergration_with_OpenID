package com.example.newfido.asm.cmds;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.example.newfido.asm.activity.ASMEntrypoint;
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
import com.example.newfido.msg.asm.obj.AuthenticateOut;
import com.example.newfido.msg.asm.obj.RequestData;
import com.example.newfido.msg.asm.obj.ResponseData;
import com.example.newfido.tlv.*;


/**
 * Created by sorin.teican on 05-Feb-17.
 */



public class Authenticate {

    public interface IUsernameSelected {
        void selected(int index);
    }

    private ASMEntrypoint mContext;

    AuthenticatorIndexDbHelper mDbIndex;
    KeyHandleDbHelper mDbKeyhandle;
    private AuthenticatorIndexController mAutenticatorIndexController;

    private KeyHandleController mKeyHandleController;

    private AuthenticatorIndex mAuthenticatorIndex;

    private String mCallerID;
    private String mPersonaID;
    private String mASMToken;

    public Authenticate(ASMEntrypoint context, String CallerID, String PersonaID, String ASMToken) {
        mContext = context;

        mDbIndex = new AuthenticatorIndexDbHelper(mContext);
        mDbKeyhandle = new KeyHandleDbHelper(mContext);

        mAutenticatorIndexController = new AuthenticatorIndexController(mDbIndex);
        mKeyHandleController = new KeyHandleController(mDbKeyhandle);

        mAuthenticatorIndex = null;

        mCallerID = CallerID;
        mPersonaID = PersonaID;
        mASMToken = ASMToken;
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-process}
     * %%% END SOURCE CODE %%%
     * <p>This function initiates the communitation with authenticator.
     * 
     * <p>AUTH 2.5.1.1.1.2.1
     * <p>AUTH 4
     * @see ASMResponse
     * @see Authenticate#checkAuthenticatorIndex(int)
     * @see Authenticate#buildCmd(ASMRequest, byte[])
     * @see AuthenticatorInterface #command(com.example.newfido.authenticator.Context, byte[], String, String)
     * 
     * @see Authenticate#processResponse(byte[], ASMRequest)
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public ASMResponse process(ASMRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: Authenticate-process

        ASMResponse asmResponse = new ASMResponse();

        mAuthenticatorIndex = checkAuthenticatorIndex(request.authenticatorIndex);
        if (mAuthenticatorIndex == null) {
            closeDbs();

            asmResponse.responseData = null;
            asmResponse.statusCode = 0x01;
            return asmResponse;
        }

        try {
            Log.d(this.getClass().getCanonicalName(), "Sending authenticate request to authenticator");
            byte[] response = AuthenticatorInterface.command(mContext, buildCmd(request, null), null, null);
            Log.d(this.getClass().getCanonicalName(), "Receiving authenticate response from authenticator");
            return processResponse(response, request);
        } catch (Exception e) {
            e.printStackTrace();
            asmResponse.responseData = null;
            asmResponse.statusCode = 0x01;
            return asmResponse;
        }
        // END: Authenticate-process
    }

    private short extractStatusCode(Tags tags) {
        return ByteBuffer.allocate(2).wrap(tags.getTags().get(TagsEnum.TAG_STATUS_CODE.id).get(0).value).getShort();
    }

    /**
     * processResponse
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-processResponse}
     * %%% END SOURCE CODE %%%
     * <p>This function prepares the response of ASM to client.
     * 
     * <p>AUTH 4.1
     * @see ASMResponse
     * @see TlvAssertionParser
     * @see Tags
     * @see Tag
     * @see AuthenticateOut
     * @see IUsernameSelected
     * @see ResponseData
     * @see Authenticate#buildCmd(ASMRequest, byte[])
     * @see AuthenticatorInterface #command(com.example.newfido.authenticator.Context, byte[], String, String)
     * 
     * @param authenticatorRes
     * @param request
     * @return
     * @throws Exception
     */
    private ASMResponse processResponse(byte[] authenticatorRes, final ASMRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "processResponse");
        // BEGIN: Authenticate-processResponse

        final ASMResponse response = new ASMResponse();

        String b64Response = Base64.encodeToString(authenticatorRes, Base64.URL_SAFE);

        try {
            TlvAssertionParser parser = new TlvAssertionParser();
            final Tags tlvTags = parser.parse(b64Response);

            if (extractStatusCode(tlvTags) != 0x00) {
                Log.d(this.getClass().getCanonicalName(), "Invalid status code from authenticator");
                response.statusCode = 0x01;
                response.responseData = null;
                return response;
            }

            if (mAuthenticatorIndex.type == 0) {
                List<String> usernames = new ArrayList<>();
                if (tlvTags.getTags().containsKey(TagsEnum.TAG_USERNAME_AND_KEYHANDLE.id)) {
                    for (Tag tag : tlvTags.getTags().get(TagsEnum.TAG_USERNAME.id))
                        usernames.add(new String(tag.value));

                    final AuthenticateOut authenticateOut = new AuthenticateOut();

                    IUsernameSelected selectedUsername = new IUsernameSelected() {
                        @Override
                        public void selected(int index) {
                            try {
                                authenticateOut.assertion = Base64.encodeToString(AuthenticatorInterface.command(mContext, buildCmd(request,
                                        tlvTags.getTags().get(TagsEnum.TAG_KEYHANDLE.id).get(index).value), null, null), Base64.URL_SAFE);
                                authenticateOut.assertionScheme = "UAFV1TLV";
                            } catch (Exception e) {
                                //Log.d(this.getClass().getName(), "Bad exception");
                                e.printStackTrace();
                                Log.d(this.getClass().getCanonicalName(), "Authenticate error");
                                response.statusCode = 0x01;
                                response.responseData = null;
                            }
                        }
                    };
                    mContext.showUsernames(usernames, selectedUsername);
                }
            }

            if (response.statusCode == 0x01)
                return response;

            ResponseData authenticateOut = new ResponseData();
            authenticateOut.assertion = Base64.encodeToString(tlvTags.getTags()
                    .get(TagsEnum.TAG_AUTHENTICATOR_ASSERTION.id).get(0).value, Base64.URL_SAFE);
            authenticateOut.assertionScheme = "UAFV1TLV";
            response.responseData = authenticateOut;
            response.statusCode = 0x00;

            closeDbs();

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.responseData = null;
            response.statusCode = 0x01;

            closeDbs();

            return response;
        }
        // END: Authenticate-processResponse
    }

    private void closeDbs() {
        mDbIndex.close();
        mDbKeyhandle.close();
    }

    /**
     * buildCmd
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-buildCmd}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the requests before forwarding the command to the authenticator.
     * 
     * <p>AUTH 2.5.1.1.1.2.1.1
     * @see Authenticate#lookUpKeyHandles(String, String[])
     * @see KeyHandleController#getAllKeyHandles()
     * 
     * @param request
     * @param keyHandle
     * @return
     * @throws Exception
     */
    private byte[] buildCmd(ASMRequest request, byte[] keyHandle) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "buildCmd");
        // BEGIN: Authenticate-buildCmd

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        RequestData authenticateIn = request.args;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_INDEX.id));
        bout.write(UnsignedUtil.encodeInt(2));
        //value = UnsignedUtil.encodeInt(request.authenticatorIndex);
        bout.write(UnsignedUtil.encodeInt(request.authenticatorIndex));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_APPID.id));
        value = authenticateIn.appID.getBytes();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = Keystore.SHA256(authenticateIn.finalChallenge.getBytes());
        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        if (authenticateIn.transaction != null && authenticateIn.transaction.length > 0) {
            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_TRANSACTION_CONTENT.id));
            value = Base64.decode(authenticateIn.transaction[0].content, Base64.URL_SAFE);
            bout.write(UnsignedUtil.encodeInt(value.length));
            bout.write(value);
        }

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE_ACCESS_TOKEN.id));
        value = getKHAccessToken(authenticateIn.appID);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        if (keyHandle == null) {
            if (authenticateIn.keyIDs != null && authenticateIn.keyIDs.length > 0) {
                if (mAuthenticatorIndex.type == 0) {
                    List<KeyHandle> foundHandles = lookUpKeyHandles(authenticateIn.appID, authenticateIn.keyIDs);
                    if (foundHandles.isEmpty())
                        throw new Exception("No keyhandles found");
                    for (KeyHandle handle : foundHandles) {
                        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE.id));
                        value = handle.TAG_KEYHANDLE.getBytes();
                        bout.write(UnsignedUtil.encodeInt(value.length));
                        bout.write(value);
                    }
                } else {
                    for (String KeyID : authenticateIn.keyIDs) {
                        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE.id));
                        value = KeyID.getBytes();
                        bout.write(UnsignedUtil.encodeInt(value.length));
                        bout.write(value);
                    }
                }
            } else {
                if (mAuthenticatorIndex.type == 1)
                    throw new Exception();

                List<KeyHandle> keyHandles = mKeyHandleController.getAllKeyHandles();
                for (int i = 0; i < keyHandles.size(); i++) {
                    bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE.id));
                    bout.write(UnsignedUtil.encodeInt(keyHandles.get(i).TAG_KEYHANDLE.length()));
                    bout.write(keyHandles.get(i).TAG_KEYHANDLE.getBytes());
                }
            }
        } else {
            bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYHANDLE.id));
            bout.write(UnsignedUtil.encodeInt(keyHandle.length));
            bout.write(keyHandle);
        }

        value = bout.toByteArray();
        bout.reset();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_SIGN_CMD.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: Authenticate-buildCmd
    }

    /**
     * getKHAccessToken
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-getKHAccessToken}
     * %%% END SOURCE CODE %%%
     * <p>This function creates a token.
     * 
     * <p>AUTH 2.5.1.1.1.2.1.1.1
     * 
     * @param AppID
     * @return
     * @throws Exception
     */
    private byte[] getKHAccessToken(String AppID) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "getKHAccessToken");
        // BEGIN: Authenticate-getKHAccessToken

        String KHAccessToken = AppID + mASMToken + mPersonaID + mCallerID;
        byte[] token = Keystore.SHA256(KHAccessToken.getBytes());


        Log.d(this.getClass().getCanonicalName(), "AppID: " + AppID);
        Log.d(this.getClass().getCanonicalName(), "ASMToken: " + mASMToken);
        Log.d(this.getClass().getCanonicalName(), "PersonaID: " + mPersonaID);
        Log.d(this.getClass().getCanonicalName(), "CallerID: " + mCallerID);

        Log.d(this.getClass().getCanonicalName(), "Access token generated by ASM: " + Base64.encodeToString(token, Base64.DEFAULT | Base64.NO_WRAP | Base64.NO_PADDING));

        return token;
        // END: Authenticate-getKHAccessToken
    }

    /**
     * checkAuthenticatorIndex
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-checkAuthenticatorIndex}
     * %%% END SOURCE CODE %%%
     * <p> This function returns the index of the authenticator.
     * 
     * <p>AUTH
     * @see AuthenticatorIndex
     * 
     * @param index
     * @return
     */
    private AuthenticatorIndex checkAuthenticatorIndex(int index) {
        Log.d(this.getClass().getCanonicalName(), "checkAuthenticatorIndex");
        // BEGIN: Authenticate-checkAuthenticatorIndex

        List<AuthenticatorIndex> authenticators = mAutenticatorIndexController.getAllIndexes();
        for (AuthenticatorIndex authenticator : authenticators)
            if (authenticator.index == index)
                return authenticator;

        return null;
        // END: Authenticate-checkAuthenticatorIndex
    }

    /**
     * lookUpKeyHandles
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet Authenticate-lookUpKeyHandles}
     * %%% END SOURCE CODE %%%
     * <p>This function searches for specific key handles.
     * 
     * <p>AUTH
     */
    private List<KeyHandle> lookUpKeyHandles(String AppID, String[] KeyIDs) {
        Log.d(this.getClass().getCanonicalName(), "lookUpKeyHandles");
        // BEGIN: Authenticate-lookUpKeyHandles

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
        // END: Authenticate-lookUpKeyHandles
    }

}
