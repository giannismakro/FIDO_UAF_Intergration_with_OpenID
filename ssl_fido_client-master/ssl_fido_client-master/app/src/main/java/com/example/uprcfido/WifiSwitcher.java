package com.example.uprcfido;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static android.net.wifi.WifiEnterpriseConfig.Eap.PEAP;

/**
 * Created by sorin.teican on 20-Feb-18.
 */



public class WifiSwitcher {
    private static String cert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFRjCCAy6gAwIBAgIMIBQHER4eXVkTcNtgMA0GCSqGSIb3DQEBCwUAMEcxCzAJ\n" +
            "BgNVBAYTAlJPMRcwFQYDVQQKEw5UcnVzdCA0IE1vYmlsZTEfMB0GA1UECxMWVHJ1\n" +
            "c3QgNCBNb2JpbGUgUm9vdCBDQTAeFw0xNDA3MTAwODA2MTJaFw00NDA3MTAwODA2\n" +
            "MTJaMEcxCzAJBgNVBAYTAlJPMRcwFQYDVQQKEw5UcnVzdCA0IE1vYmlsZTEfMB0G\n" +
            "A1UECxMWVHJ1c3QgNCBNb2JpbGUgUm9vdCBDQTCCAiIwDQYJKoZIhvcNAQEBBQAD\n" +
            "ggIPADCCAgoCggIBANAcWVwkuEOrlzAQPQfOiQUel0diTJupxRX1jn3z649RwW8d\n" +
            "bo+FCmYkhZ7W0+RyqNEp0ncnOhMuYAXEQuBfFJ+iAgxwm6UD5R2KwNYeodrMPFX9\n" +
            "vwCOiJOA15yD/DBS47P0qA+fwbp8QtfDBso3C9IU3CDW/sf8dbM1x7clXKlZt6Ju\n" +
            "P+o1RjC6Lc04ZcC0b9g3XHKQj7NXvA38ylxzjZMQmASZQ61DQYBf3dQe06woOEYC\n" +
            "EUoJlp1rZr6Zs6p+MPKvrg+lXOTNXgfc4SJbzDib95Wp2ty774FNUgu9tYAq7L0f\n" +
            "6HYD1yzhKWb7VJ6qq2Xz4AHCnIiOjj8rcUzdEsSPPEPSnnHdgvrkgpSJa9ozYaSn\n" +
            "GqD8Dq3Eva/s8V2qZIknk8iIvneuvCTsAcvqbA9nkGUtrqejqd5ZKjh9F/igFTFR\n" +
            "+ZjCgB03slA96rntnAiCS1xkGfELZS+0JZewB3nCsxrrneEKi5Rd/L9c0vCIzKxK\n" +
            "IqbkNNypsldmgkZUL1hOn8r0LRHHn5lm3Wt/9pmSxMtUhYt0x6Dgjm6eCb/Ty1sP\n" +
            "NPwesKztyvAy0wuCymWqWKozKkeGS0rQMsdw8P00U7i1TqotIMgigytyJd7yfiLD\n" +
            "MiwR3/FYpVtSjyDd30NItfHcDiKXPWAm9lttj8hI//mlfPZvkrHgt3mslsC9AgMB\n" +
            "AAGjMjAwMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFF/Oh3rCZyccHGURcgxt\n" +
            "5KcFqShaMA0GCSqGSIb3DQEBCwUAA4ICAQCrdHlrG6SacOyPRsQAPtcJfeZAizA6\n" +
            "Kz/wS8uyH5C97Sm1yG1JbXUC0s3s/KU5A0jBJtRa7wUuCwe739LunT1Vtmci5ZoB\n" +
            "6NQyxabgJEiHhbd0TNQ5xIkyr2mmn0k2nGfRGWuVIU09Wzvtk+ZZtLzhNzEuKsIc\n" +
            "QRlV+FfRw6yUwSou0Kx++22FgjAMEFSyZVtfvXLNG5NqZ3+QDJK88cRrLV6rSABQ\n" +
            "xnJ2kqYV386xSTywtUSS4dh0+p14Tg0w46hhLhmXQDpiHzj8t/yurTXMxBsngycY\n" +
            "HwIbt8w+jJbHPW4BLPQWC7fBr51Ne9MyfSHVIyJemke5DlST8KmdWmznHuaWXIVv\n" +
            "X4w63+8BqhrAMI2UOLeQPHmzqJdPaY1axN5lYLg7WR/dt3VALOw3GwLzeqkf+G2b\n" +
            "h0+BdNb0Rg3YgQaJiz3H2hXrXhvHVtcWPoXdvRW8+fVVy4IiT5XjVeye1XTshoEL\n" +
            "9OcVC4x/TN5YP2NNkBrbkBs+c4faXgEh1KDX6+0WwopuAiopNFFmKGO3TnfFnkOU\n" +
            "gvjF1ctvhq0/RoQyy5T8jyuRX2mqZPzEFwNr9/ZEfjoJKBULHcUxgs92CPmknEm6\n" +
            "ZP1k6ayyI/vaAFi8jLsL8gx6yHzegvNMjWETkNvNbLQH0N/LiSsK9FrFGlvWao71\n" +
            "dNTQMNbAdfjcMQ==\n" +
            "-----END CERTIFICATE-----\n";


    public static void switchWifi(Activity context, String ssid, String user, String passwd) {
        WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = "\"" + ssid + "\"";

        wifiConf.allowedKeyManagement.clear();
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

        wifiConf.enterpriseConfig.setIdentity(user);
        wifiConf.enterpriseConfig.setPassword(passwd);

        wifiConf.enterpriseConfig.setEapMethod(PEAP);

        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(cert.getBytes());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(bi);
            Log.e(context.getClass().getName(), "Certificate created");
            wifiConf.enterpriseConfig.setCaCertificate(cert);

        } catch (CertificateException e) {
            Log.e(context.getClass().getName(), "Exception:" + e.getMessage());
        }

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConf);
        Log.e(context.getClass().getName().toString(), "Net ID:" + netId);
        wifiManager.enableNetwork(netId, true);

    }
}
