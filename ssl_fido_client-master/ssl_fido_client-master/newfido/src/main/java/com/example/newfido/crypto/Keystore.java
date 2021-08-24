package com.example.newfido.crypto;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Created by sorin.teican on 28-Oct-16.
 */


public class Keystore {

    private static KeyStore _androidKeyStore = null;

    private static final String B64URL_ATTESTATION_CERT_DER = "MIIB-TCCAZ-gAwIBAgIEVTFM0zAJBgcqhkjOPQQBMIGEMQswCQYDVQQGEwJVUzELMAkGA1UECAwCQ0ExETAPBgNVBAcMCFNhbiBKb3NlMRMwEQYDVQQKDAplQmF5LCBJbmMuMQwwCgYDVQQLDANUTlMxEjAQBgNVBAMMCWVCYXksIEluYzEeMBwGCSqGSIb3DQEJARYPbnBlc2ljQGViYXkuY29tMB4XDTE1MDQxNzE4MTEzMVoXDTE1MDQyNzE4MTEzMVowgYQxCzAJBgNVBAYTAlVTMQswCQYDVQQIDAJDQTERMA8GA1UEBwwIU2FuIEpvc2UxEzARBgNVBAoMCmVCYXksIEluYy4xDDAKBgNVBAsMA1ROUzESMBAGA1UEAwwJZUJheSwgSW5jMR4wHAYJKoZIhvcNAQkBFg9ucGVzaWNAZWJheS5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQ8hw5lHTUXvZ3SzY9argbOOBD2pn5zAM4mbShwQyCL5bRskTL3HVPWPQxqYVM-3pJtJILYqOWsIMd5Rb_h8D-EMAkGByqGSM49BAEDSQAwRgIhAIpkop_L3fOtm79Q2lKrKxea-KcvA1g6qkzaj42VD2hgAiEArtPpTEADIWz2yrl5XGfJVcfcFmvpMAuMKvuE1J73jp4";
    private static final String B64URL_PKCS8_ATTESTATION = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgezOOy1TykYoCiwOdJkKCfScV3-lN1v_E9keawMikuFygCgYIKoZIzj0DAQehRANCAAQ8hw5lHTUXvZ3SzY9argbOOBD2pn5zAM4mbShwQyCL5bRskTL3HVPWPQxqYVM-3pJtJILYqOWsIMd5Rb_h8D-E";

    private static void loadKeystore() throws Exception {
        _androidKeyStore = KeyStore.getInstance("AndroidKeyStore");
        _androidKeyStore.load(null);
    }

    public static KeyPair generateKeyPair(String keyAlias)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException
    {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");

        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(keyAlias,
                        KeyProperties.PURPOSE_SIGN)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .setDigests(KeyProperties.DIGEST_SHA256,
                                KeyProperties.DIGEST_SHA384,
                                KeyProperties.DIGEST_SHA512)
                        //.setUserAuthenticationRequired(true)
                        //.setUserAuthenticationValidityDurationSeconds(120)
                        .build());

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public static void deleteKeyWithID(String alias) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        _androidKeyStore.deleteEntry(alias);
    }

    public static byte[] sign(byte[] data, String keyAlias) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        KeyStore.Entry entry = _androidKeyStore.getEntry(keyAlias, null);

        if (!(entry instanceof KeyStore.PrivateKeyEntry))
            return null;

        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(((KeyStore.PrivateKeyEntry)entry).getPrivateKey());
        s.update(data);
        return s.sign();
    }

    public static boolean isInsideSecureHardware(String keyAlias) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        KeyStore.Entry entry = _androidKeyStore.getEntry(keyAlias, null);

        if (!(entry instanceof KeyStore.PrivateKeyEntry))
            return false;

        PrivateKey privateKey = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();

        KeyFactory factory = KeyFactory.getInstance(privateKey.getAlgorithm(), "AndroidKeyStore");
        KeyInfo keyInfo = factory.getKeySpec(privateKey, KeyInfo.class);
        return keyInfo.isInsideSecureHardware();
    }

    public static boolean verify(byte[] data, byte[] signature, String keyAlias) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        KeyStore.Entry entry = _androidKeyStore.getEntry(keyAlias, null);
        if (!(entry instanceof KeyStore.PrivateKeyEntry))
            return false;

        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initVerify(((KeyStore.PrivateKeyEntry)entry).getCertificate());
        s.update(data);
        return s.verify(signature);
    }

    public static byte[] SHA256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    public static void addPrivateKeyEntry(String alias, KeyStore.PrivateKeyEntry entry) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        _androidKeyStore.setEntry(alias, entry, null);
    }

    public static byte[] generateKeyID() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return bytes;
    }

    public static byte[] generateNonce(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);

        return bytes;
    }

    public static byte[] encodeRaw(PublicKey publicKey) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ECPublicKey ecKeystoreKey = (ECPublicKey) publicKey;
        byte[] X = ecKeystoreKey.getW().getAffineX().toByteArray();
        byte[] Y = ecKeystoreKey.getW().getAffineY().toByteArray();

        baos.write(0x04);
        baos.write(X);
        baos.write(Y);

        return baos.toByteArray();
    }

    public static PrivateKey fromPKCS8EC(byte[] pkcs8Key) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8Key);
        KeyFactory factory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = factory.generatePrivate(spec);

        return privateKey;
    }

    public static X509Certificate fromDER(byte[] derCertificate) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(derCertificate);

        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(bais);

        bais.close();

        return certificate;
    }

    public static Certificate[] getCertificateChain(String alias) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        return _androidKeyStore.getCertificateChain("attestation");
    }

    private static void generateWrapSym(String keyAlias) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        if (_androidKeyStore.containsAlias(keyAlias))
            return;

        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        kg.init(new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                //.setKeySize(256)
                                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                //.setUserAuthenticationRequired(true)
                                //.setUserAuthenticationValidityDurationSeconds(120)
                                .build());
        kg.generateKey();
    }

    public static byte[] encryptAES(String keyAlias, byte[] plain) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, _androidKeyStore.getKey(keyAlias, null));
        baos.write(cipher.getIV());
        baos.write(cipher.doFinal(plain));

        return baos.toByteArray();
    }

    public static byte[] decryptAES(String keyAlias, byte[] cipher) throws Exception {
        if (_androidKeyStore == null)
            loadKeystore();

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        // Authentication tag length and iv - which is prefixed to the cipher text.
        GCMParameterSpec spec = new GCMParameterSpec(128, Arrays.copyOfRange(cipher, 0, 12));
        c.init(Cipher.DECRYPT_MODE, _androidKeyStore.getKey(keyAlias, null), spec);

        return c.doFinal(Arrays.copyOfRange(cipher, 12, cipher.length));
    }

    public static void initAuthenticator() throws Exception {
        byte[] pkcs8 = Base64.decode(B64URL_PKCS8_ATTESTATION, Base64.URL_SAFE);
        byte[] der = Base64.decode(B64URL_ATTESTATION_CERT_DER, Base64.URL_SAFE);

        KeyStore.PrivateKeyEntry entry = new KeyStore.PrivateKeyEntry(fromPKCS8EC(pkcs8), new X509Certificate[] { fromDER(der) });

        addPrivateKeyEntry("attestation", entry);

        generateWrapSym("wrap_sym");
    }
}
