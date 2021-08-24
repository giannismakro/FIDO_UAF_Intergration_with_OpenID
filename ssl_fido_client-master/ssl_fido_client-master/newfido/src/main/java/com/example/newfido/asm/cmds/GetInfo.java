package com.example.newfido.asm.cmds;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.example.newfido.asm.db.AuthenticatorIndexDbHelper;
import com.example.newfido.asm.db.controllers.AuthenticatorIndexController;
import com.example.newfido.asm.db.models.AuthenticatorIndex;
import com.example.newfido.authenticator.AuthenticatorInterface;
import com.example.newfido.msg.Version;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;
import com.example.newfido.msg.asm.obj.ResponseData;
import com.example.newfido.tlv.Tags;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.TlvAssertionParser;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 11-Jan-17.
 */
 

public class GetInfo {

    //private AuthenticatorInterface mAuthenticator;
    private Context mContext;

    private AuthenticatorIndexDbHelper mDb;
    private AuthenticatorIndexController mAutenticatorIndexController;

    public GetInfo(Context context) {
        mContext = context;

        mDb = new AuthenticatorIndexDbHelper(mContext);
        mAutenticatorIndexController = new AuthenticatorIndexController(mDb);
    }

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-process}
     * %%% END SOURCE CODE %%%
     * <p>This function sends and receives the requests/responces to Authenticator.
     * 
     * <p>DISC 2.5.1.2.1
     * <p>DISC 4
     * @see ASMResponse
     * 
     * @see GetInfo#buildCmd()
     * @see AuthenticatorInterface #command(com.example.newfido.authenticator.Context, byte[], String, String)
     * 
     * @see TlvAssertionParser
     * @see AuthenticatorInfo
     * @see ResponseData
     * 
     * @see TlvAssertionParser #TlvAssertionParser()
     * @see TlvAssertionParser#parse(String)
     * @see Tags #Tags()
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public ASMResponse process(ASMRequest request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: GetInfo-process

        ASMResponse asmResponse = new ASMResponse();

        try {
            Log.d(this.getClass().getCanonicalName(), "Sending getinfo to authenticator");
            byte[] response = AuthenticatorInterface.command(mContext, buildCmd(), null, null);
            Log.d(this.getClass().getCanonicalName(), "Received get info from authenticator");

            TlvAssertionParser parser = new TlvAssertionParser();
            Tags tlvTags = parser.parse(Base64.encodeToString(response, Base64.URL_SAFE));
            AuthenticatorInfo info = extractInfoFromTags(tlvTags);

            ResponseData infoOut = new ResponseData();
            infoOut.Authenticators = new AuthenticatorInfo[1];
            infoOut.Authenticators[0] = info;

            mDb.close();

            asmResponse.responseData = infoOut;
            asmResponse.statusCode = 0x00;
        } catch (Exception e) {
            e.printStackTrace();
            mDb.close();

            asmResponse.responseData = null;
            asmResponse.statusCode = 0x01;
        }

        return asmResponse;
        // END: GetInfo-process
    }

    /**
     * buildCmd
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-buildCmd}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the command.
     * 
     * <p>DISC 2.5.1.2.1.1
     */
    private byte[] buildCmd() throws Exception {
        Log.d(this.getClass().getCanonicalName(), "buildCmd");
        // BEGIN: GetInfo-buildCmd

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_GETINFO_CMD.id));
        bout.write(UnsignedUtil.encodeInt(0));

        return bout.toByteArray();
        // END: GetInfo-buildCmd
    }

    /**
     * extractInfoFromTags
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-extractInfoFromTags}
     * %%% END SOURCE CODE %%%
     * <p>This function decodes the response from Authenticator.
     * 
     * <p>DISC 4.2
     * @see AuthenticatorInfo
     * @see Version
     * @see AuthenticatorIndex
     * 
     * @param tlvTags
     * @return
     * @throws Exception
     */
    private AuthenticatorInfo extractInfoFromTags(Tags tlvTags) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "extractInfoFromTags");
        // BEGIN: GetInfo-extractInfoFromTags

        AuthenticatorInfo info = new AuthenticatorInfo();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        short asmVersion = Short.reverseBytes(ByteBuffer.allocate(2).wrap(tlvTags.getTags().get(TagsEnum.TAG_API_VERSION.id).get(0).value).getShort());
        if (asmVersion == 1) {
            info.asmVersions = new Version[1];
            info.asmVersions[0] = new Version(1, 0);
        } else throw new Exception();

        info.authenticatorIndex = 0;

        value = tlvTags.getTags().get(TagsEnum.TAG_AAID.id).get(0).value;
        //value = UnsignedUtil.reverse(value); // from little endian to big endian.
        info.aaid = new String(value);

        value = tlvTags.getTags().get(TagsEnum.TAG_AUTHENTICATOR_METADATA.id).get(0).value;
        if (value.length < 14)
            throw new Exception();
        //value = UnsignedUtil.reverse(value);

        // Metadata - authenticator type.
        bout.write(value[0]);
        bout.write(value[1]);
        short authenticatorType = Short.reverseBytes(ByteBuffer.allocate(2).wrap(bout.toByteArray()).getShort()); bout.reset();
        if (isBitSet(authenticatorType, 0))
            info.isSecondFactorOnly = true;
        else info.isSecondFactorOnly = false;

        if (isBitSet(authenticatorType, 1))
            info.isRoamingAuthenticator = true;
        else info.isRoamingAuthenticator = false;

        if (isBitSet(authenticatorType, 6))
            info.isUserEnrolled = true;
        else info.isUserEnrolled = false;

        if (isBitSet(authenticatorType, 4))
            info.hasSettings = true;
        else info.hasSettings = false;

        // Metadata - user verification.
        //bout.write(value[3]);
        //bout.write(value[4]);
        bout.reset();
        bout.write(value[4]);
        bout.write(value[5]);
        info.userVerification = Short.reverseBytes(ByteBuffer.allocate(2).wrap(bout.toByteArray()).getShort()); bout.reset();

        // Metadata - key protection.
        bout.write(value[6]);
        bout.write(value[7]);
        info.keyProtection = Short.reverseBytes(ByteBuffer.allocate(2).wrap(bout.toByteArray()).getShort()); bout.reset();

        // Metadata - matcher protection.
        bout.write(value[8]);
        bout.write(value[9]);
        info.matcherProtection = Short.reverseBytes(ByteBuffer.allocate(2).wrap(bout.toByteArray()).getShort()); bout.reset();

        // Metadata - transaction content display.
        bout.write(value[10]);
        bout.write(value[11]);
        info.tcDisplay = Short.reverseBytes(ByteBuffer.allocate(2).wrap(bout.toByteArray()).getShort()); bout.reset();

        // Metadata - authentication algorithm.
        bout.write(value[12]);
        bout.write(value[13]);
        info.authenticationAlgorithm = Short.reverseBytes(ByteBuffer.allocate(2).wrap(bout.toByteArray()).getShort()); bout.reset();

        value = tlvTags.getTags().get(TagsEnum.TAG_ASSERTION_SCHEME.id).get(0).value;
        //value = UnsignedUtil.reverse(value);
        info.assertionScheme = new String(value);

        int len = tlvTags.getTags().get(TagsEnum.TAG_ATTESTATION_TYPE.id).size();
        if (len == 0) throw new Exception();
        info.attestationTypes = new short[len];
        for (int i = 0; i < len; i++) {
            value = tlvTags.getTags().get(TagsEnum.TAG_ATTESTATION_TYPE.id).get(i).value;
            info.attestationTypes[i] = Short.reverseBytes(ByteBuffer.allocate(2).wrap(value).getShort());
        }

        info.title = "certSIGN Bound UAF Authenticator";

        AuthenticatorIndex index = new AuthenticatorIndex();
        index.type = info.isRoamingAuthenticator ? 1 : 0;
        index.AAID = info.aaid;
        index.index = info.authenticatorIndex;
        mAutenticatorIndexController.insertIndex(index);

        return info;
        // END: GetInfo-extractInfoFromTags
    }

    private boolean isBitSet(int value, int bitNb) {
        return (((value >>> bitNb) & 1) != 0);
    }
}
