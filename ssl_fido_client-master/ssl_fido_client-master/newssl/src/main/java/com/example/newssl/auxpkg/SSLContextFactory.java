package com.example.newssl.auxpkg;

import android.content.Context;
import com.example.newssl.R.raw;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.KeyStore.LoadStoreParameter;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;



public class SSLContextFactory {
    private static SSLContextFactory theInstance = null;
    private Context context;

    private SSLContextFactory(Context context) {
        this.context = context;
    }

    public static SSLContextFactory getInstance(Context context) {
        if (theInstance == null) {
            theInstance = new SSLContextFactory(context);
        }

        return theInstance;
    }

    public SSLContext makeContext() throws Exception {
        KeyStore trustStore = this.loadPEMTrustStore();
        TrustManager[] trustManagers = new TrustManager[]{new NewTrustManager(trustStore)};
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init((KeyManager[])null, trustManagers, (SecureRandom)null);
        return sslContext;
    }

    private KeyStore loadPEMTrustStore() throws Exception {
        InputStream certInputStream = this.context.getResources().openRawResource(raw.class1);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(certInputStream);
        String alias = cert.getSubjectX500Principal().getName();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load((LoadStoreParameter)null);
        trustStore.setCertificateEntry(alias, cert);
        return trustStore;
    }
}
