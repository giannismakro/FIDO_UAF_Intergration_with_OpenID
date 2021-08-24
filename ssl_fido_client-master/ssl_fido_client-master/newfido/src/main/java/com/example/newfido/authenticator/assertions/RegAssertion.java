package com.example.newfido.authenticator.assertions;

import android.util.Base64;
import com.example.newfido.authenticator.Authenticator;
import com.example.newfido.authenticator.db.controllers.CountersController;
import com.example.newfido.authenticator.db.models.Counter;
import com.example.newfido.crypto.Keystore;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.cert.Certificate;


/**
 * Created by sorin.teican on 03-Jan-17.
 */
 

public class RegAssertion {
    
    public static String KeyID = null;
    
    /**
     * getAssertion
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet RegAssertion-getAssertion}
     * %%% END SOURCE CODE %%%
     * <p>This function creates the response of the authenticator.
     * 
     * <p>REG 3.1.1.2
     * @see RegAssertion#generateKRD(byte[], CountersController,String, String)
     */
    public static byte[] getAssertion(final byte[] finalChallange, final CountersController countersController,
                                      short attestationType, String deviceID, String deviceType) throws Exception {
        // BEGIN: RegAssertion-getAssertion
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream baos_reg = new ByteArrayOutputStream();

        byte[] value = null;

        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_KRD.id));
        value = generateKRD(finalChallange, countersController, deviceID, deviceType);
        baos.write(UnsignedUtil.encodeInt(value.length));
        baos.write(value);

        value = baos.toByteArray();

        if (attestationType == TagsEnum.TAG_ATTESTATION_BASIC_FULL.id) {
            baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_BASIC_FULL.id));
            value = generateAttestationBasicFull(value);
            baos.write(UnsignedUtil.encodeInt(value.length));
            baos.write(value);
        } else if (attestationType == TagsEnum.TAG_ATTESTATION_BASIC_SURROGATE.id) {
            baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_BASIC_FULL.id));
            value = generateAttestationBasicSurrogate(value);
            baos.write(UnsignedUtil.encodeInt(value.length));
            baos.write(value);

            countersController.incrementCounter(KeyID);
        } else {
            throw new Exception("UAF_CMD_STATUS_ATTESTATION_NOT_SUPPORTED");
        }

        countersController.incrementCounter("global_reg");

        baos_reg.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_REG_ASSERTION.id));
        value = baos.toByteArray();
        baos_reg.write(UnsignedUtil.encodeInt(value.length));
        baos_reg.write(value);

        return baos_reg.toByteArray();
        // END: RegAssertion-getAssertion
    }

    /**
     * generateKRD
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet RegAssertion-generateKRD}
     * %%% END SOURCE CODE %%%
     * <p>This function generates the KRD.
     * 
     * <p>REG 3.1.1.2.1
     * 
     */
    private static byte[] generateKRD(final byte[] finalChallenge, final CountersController countersController,
                                      String deviceID, String deviceType) throws Exception {
        // BEGIN: RegAssertion-generateKRD
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] value = null;

        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AAID.id));
        baos.write(UnsignedUtil.encodeInt(Authenticator.AAID.getBytes().length));
        baos.write(Authenticator.AAID.getBytes());

        // Authenticator version: 0 (2 bytes little endian)
        // Authentication mode: 0x01 (1 byte)
        // Signature algorithm and encoding: 0x02 UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER 0x02 (2 bytes little endian)
        // Public key encoding: 0x100 UAF_ALG_KEY_ECC_X962_DER 0x101 (2 bytes little endian)
        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ASSERTION_INFO.id));
        value = new byte[] { 0x00, 0x00, 0x01, 0x02, 0x00, 0x01, 0x01 };
        baos.write(UnsignedUtil.encodeInt(value.length));
        baos.write(value);

        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        baos.write(UnsignedUtil.encodeInt(finalChallenge.length));
        baos.write(finalChallenge);

        // Generate KeyID.
        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_KEYID.id));
        value = Keystore.generateKeyID();
        // Base64 encode KeyID to use as key alias.
        KeyID = Base64.encodeToString(value, Base64.URL_SAFE | Base64.NO_WRAP);
        baos.write(UnsignedUtil.encodeInt(KeyID.length()));
        baos.write(KeyID.getBytes());

        // Get global register and sign counters from the db countersController.
        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_COUNTERS.id));
        baos.write(UnsignedUtil.encodeInt(8));
        baos.write(UnsignedUtil.encodeIntValue(Integer.reverseBytes(countersController.getCounter("global_sign").value)));
        baos.write(UnsignedUtil.encodeIntValue(Integer.reverseBytes(countersController.getCounter("global_register").value)));

        // Create key counter.
        Counter signCounter = new Counter();
        signCounter.type = "sign";
        signCounter.value = 0;
        signCounter.context = KeyID;
        countersController.insertCounter(signCounter);

        // Generate Uauth.
        KeyPair keyPair = Keystore.generateKeyPair(KeyID);
        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_PUB_KEY.id));
        value = keyPair.getPublic().getEncoded();
        baos.write(UnsignedUtil.encodeInt(value.length));
        baos.write(value);

        if (deviceID != null) {
            baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_DEVICE_ID.id));
            baos.write(UnsignedUtil.encodeInt(deviceID.getBytes().length));
            baos.write(deviceID.getBytes());
        }

        if (deviceType != null) {
            baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_DEVICE_TYPE.id));
            baos.write(UnsignedUtil.encodeInt(deviceType.getBytes().length));
            baos.write(deviceType.getBytes());
        }

        return baos.toByteArray();
        // END: RegAssertion-generateKRD
    }

    private static byte[] generateAttestationBasicFull(byte[] krd) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] value = null;

        baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_SIGNATURE.id));
        value = Keystore.sign(krd, "attestation");
        baos.write(UnsignedUtil.encodeInt(value.length));
        baos.write(value);

        Certificate[] chain = Keystore.getCertificateChain("attestation");
        for (int i = 0; i < chain.length; i++) {
            baos.write(UnsignedUtil.encodeInt(TagsEnum.TAG_ATTESTATION_CERT.id));
            value = chain[i].getEncoded();
            baos.write(UnsignedUtil.encodeInt(value.length));
            baos.write(value);
        }

        return baos.toByteArray();
    }

    private static byte[] generateAttestationBasicSurrogate(byte[] krd) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value = null;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_SIGNATURE.id));
        value = Keystore.sign(krd, KeyID);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
    }
}
