package com.example.newssl;

import android.content.Context;
import com.example.newssl.R.raw;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;



class UPRCSSLContext {
    public UPRCSSLContext() {
    }

    public static SSLSocketFactory UPRCSSLSocketFactory(Context context) throws Exception {
        InputStream certInputStream = context.getResources().openRawResource(raw.class1);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate ca = (X509Certificate)cf.generateCertificate(certInputStream);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load((InputStream)null, (char[])null);
        keyStore.setCertificateEntry("ca", ca);
        UPRCTrustManager trustManager = new UPRCTrustManager(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init((KeyManager[])null, new TrustManager[]{trustManager}, (SecureRandom)null);
        return sslContext.getSocketFactory();
    }
}
