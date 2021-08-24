package com.example.newfido.authenticator.assertions;

import java.io.ByteArrayOutputStream;

import com.example.newfido.authenticator.Authenticator;
import com.example.newfido.authenticator.db.controllers.CountersController;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 04-Jan-17.
 */
 

public class AuthAssertion {

    /**
     * getAssertion
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet AuthAssertion-getAssertion}
     * %%% END SOURCE CODE %%%
     * <p>This function creates the response of the authenticator.
     * 
     * <p>AUTH 3.1.1.3
     * @see AuthAssertion#generateSignedData()
     */
    public static byte[] getAssertion(final String KeyID, final byte[] finalChallenge, final CountersController countersController) throws Exception {
        // BEGIN: AuthAssertion-getAssertion
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream bout_auth = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_SIGNED_DATA.id));
        value = generateSignedData(KeyID, finalChallenge, countersController);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = bout.toByteArray();
        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_SIGNATURE.id));
        value = Keystore.sign(value, KeyID);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        countersController.incrementCounter("global_sign");
        countersController.incrementCounter(KeyID);

        bout_auth.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_AUTH_ASSERTION.id));
        value = bout.toByteArray();
        bout_auth.write(UnsignedUtil.encodeInt(value.length));
        bout_auth.write(value);

        return bout_auth.toByteArray();
        // END: AuthAssertion-getAssertion
    }

    /**
     * generateSignedData
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet AuthAssertion-generateSignedData}
     * %%% END SOURCE CODE %%%
     * <p>This function signs the data of the response.
     * 
     * <p>AUTH 3.1.1.3.1
     */
    private static byte[] generateSignedData(final String KeyID, final byte[] finalChallenge, final CountersController countersController) throws Exception {
        // BEGIN: AuthAssertion-generateSignedData
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AAID.id));
        bout.write(UnsignedUtil.encodeInt(Authenticator.AAID.getBytes().length));
        bout.write(Authenticator.AAID.getBytes());

        // Authenticator version: 0 (2 bytes little endian)
        // Authentication mode: 0x01 (1 byte)
        // Signature algorithm and encoding: 0x02 UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER 0x02 (2 bytes little endian)
        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ASSERTION_INFO.id));
        value = new byte[] { 0x00, 0x00, 0x01, 0x02, 0x00};
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_NONCE.id));
        // MUST be at least 8 bytes.
        value = Keystore.generateNonce(8);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        bout.write(UnsignedUtil.encodeInt(finalChallenge.length));
        bout.write(finalChallenge);

        // No support for transactions.
        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_TRANSACTION_CONTENT_HASH.id));
        bout.write(UnsignedUtil.encodeInt(0));

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYID.id));
        value = KeyID.getBytes();
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_COUNTERS.id));
        bout.write(UnsignedUtil.encodeInt(4));
        bout.write(UnsignedUtil.encodeIntValue(Integer.reverseBytes(countersController.getCounter(KeyID).value)));

        return bout.toByteArray();
        // END: AuthAssertion-generateSignedData
    }
}
