package com.example.newssl;

import android.content.Context;
import com.example.newssl.auxpkg.SSLContextFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;



public class Curl {
    public Curl() {
    }

    public static String toStr(InputStream responseIS) {
        String result = "";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseIS));
            StringBuilder str = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null) {
                str.append(line + "\n");
            }

            result = str.toString();
            responseIS.close();
        } catch (Exception var5) {
            result = "Error";
        }

        return result;
    }

    public static String getInSeparateThread(String url, Context context) {
        GetAsyncTask async = new GetAsyncTask();
        async.execute(new Object[]{url, context});

        while(!async.isDone()) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }
        }

        return async.getResult();
    }

    public static String postInSeparateThread(String url, String header, String data, Context context) {
        PostAsyncTask async = new PostAsyncTask();
        async.execute(new Object[]{url, header, data, context});

        while(!async.isDone()) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException var6) {
                var6.printStackTrace();
            }
        }

        return async.getResult();
    }

    public static String get(String url, Context context) {
        return get(url, (String[])null, context);
    }

    public static String get(String url, String[] header, Context context) {
        String ret = "";

        try {
            URL _url = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection)_url.openConnection();
            connection.setSSLSocketFactory(SSLContextFactory.getInstance(context).makeContext().getSocketFactory());
            connection.setRequestMethod("GET");
            if (header != null) {
                String[] var6 = header;
                int var7 = header.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    String h = var6[var8];
                    String[] split = h.split(":");
                    connection.setRequestProperty(split[0], split[1]);
                }
            }

            ret = toStr(connection.getInputStream());
            connection.disconnect();
        } catch (Exception var11) {
            var11.printStackTrace();
            ret = "{'error_code':'connect_fail','e':'" + var11 + "'}";
        }

        return ret;
    }

    public static String post(String url, String header, String data, Context context) {
        return post(url, header.split(" "), data, context);
    }

    public static String post(String url, String[] header, String data, Context context) {
        String ret = "";

        try {
            URL _url = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection)_url.openConnection();
            connection.setSSLSocketFactory(SSLContextFactory.getInstance(context).makeContext().getSocketFactory());
            connection.setRequestMethod("POST");
            if (header != null) {
                String[] var7 = header;
                int var8 = header.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    String h = var7[var9];
                    String[] split = h.split(":");
                    connection.setRequestProperty(split[0], split[1]);
                }
            }

            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(data);
            dStream.flush();
            dStream.close();
            ret = toStr(connection.getInputStream());
            connection.disconnect();
        } catch (Exception var12) {
            var12.printStackTrace();
            ret = "{'error_code':'connect_fail','e':'" + var12 + "'}";
        }

        return ret;
    }
}
