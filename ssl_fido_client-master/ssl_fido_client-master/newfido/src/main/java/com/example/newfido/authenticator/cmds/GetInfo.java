package com.example.newfido.authenticator.cmds;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.util.Log;

import com.example.newfido.authenticator.Authenticator;
import com.example.newfido.tlv.AlgAndEncodingEnum;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 10-Jan-17.
 */
 

public class GetInfo {

    /**
     * process
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-process}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the GetInfo request.
     * 
     * <p>DISC 3.1
     * 
     * @see GetInfo#authenticatorInfo()
     * 
     * @return
     * @throws Exception
     */
    public byte[] process() throws Exception {
        Log.d(this.getClass().getCanonicalName(), "process");
        // BEGIN: GetInfo-process

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_OK.id));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_API_VERSION.id));
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(1));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_INFO.id));
        value = authenticatorInfo();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = bout.toByteArray();
        bout.reset();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_GETINFO_CMD_RESPONSE.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
        // END: GetInfo-process
    }

    /**
     * authenticatorInfo
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-authenticatorInfo}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the information of the authenticator.
     * 
     * <p>DISC 3.1.1
     * 
     * @see GetInfo#metadata()
     * 
     * @return
     * @throws Exception
     */
    private byte[] authenticatorInfo() throws Exception {
        Log.d(this.getClass().getCanonicalName(), "authenticatorInfo");
        // BEGIN: GetInfo-authenticatorInfo

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_INDEX.id));
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(0));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AAID.id));
        value = Authenticator.AAID.getBytes();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_METADATA.id));
        value = metadata();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ASSERTION_SCHEME.id));
        value = "UAFV1TLV".getBytes();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value); // (UAFV1TLV).

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_TYPE.id));
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_BASIC_FULL.id));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_TYPE.id));
        bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_BASIC_SURROGATE.id));



        return bout.toByteArray();
        // END: GetInfo-authenticatorInfo
    }

    /**
     * metadata
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet GetInfo-metadata}
     * %%% END SOURCE CODE %%%
     * <p>This function encodes the metadata.
     * 
     * <p>DISC 3.1.1.1
     * 
     * @return
     * @throws IOException
     */
    private byte[] metadata() throws IOException {
        Log.d(this.getClass().getCanonicalName(), "metadata");
        // BEGIN: GetInfo-metadata

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        // authenticator type.
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(0x0008 | 0x0010 | 0x0020 | 0x0040));

        // max key handles.
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(1));

        // user verification.
        // (USER_VERIFY_FINGERPRINT | USER_VERIFY_PASSCODE | USER_VERIFY_PATTERN).
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(0x02 | 0x04 | 0x80));

        // key protection. (KEY_PROTECTION_SOFTWARE).
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(0x01));

        // matcher protection. (MATCHER_PROTECTION_SOFTWARE).
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(0x01));

        // transaction confirmation display.
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(0));

        // authentication alg.
        //bout.write(UnsignedUtil.encodeInt(2));
        bout.write(UnsignedUtil.encodeInt(AlgAndEncodingEnum.UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER.id));

        return bout.toByteArray();
        // END: GetInfo-metadata
    }

}
