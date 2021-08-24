package com.example.newfido.client.cmds.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.example.newfido.msg.ChannelBinding;
import com.example.newfido.msg.TrustedFacets;
import com.example.newfido.msg.TrustedFacetsList;
import com.example.newfido.msg.Version;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;

import com.example.newssl.Curl;


/**
 * Created by sorin.teican on 23-Feb-17.
 */

public class FacetValidator {

    private String mFacetID;
    private Gson mGson;
    private Context mContext;

    public FacetValidator(String FacetID, Context context) {
        mContext = context;
        mFacetID = FacetID;
        mGson = new Gson();
    }

    private String getInSeparateThread(String url, ChannelBinding channelBinding) {
        GetAsyncTask async = new GetAsyncTask();
        async.execute(url, channelBinding);
        while (!async.isDone()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return async.getResult();
    }

    /**
     * processFacetID
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FacetValidator-processFacetID}
     * %%% END SOURCE CODE %%%
     * <p>This function formats the FacetID.
     * <p>REG 1.2.1.4.1.4
     * <p>AUTH 1.2.1.2.1.3
     * 
     * @see FacetValidator#processTrustedFacetsList(TrustedFacetsList, Version, String)
     * 
     * @param AppID
     * @param version
     * @param channelBinding
     * @return
     */
    public boolean processFacetID(String AppID, Version version, ChannelBinding channelBinding) {
        Log.d(this.getClass().getCanonicalName(), "processFacetID");
        // BEGIN: FacetValidator-processFacetID

        Log.d(this.getClass().getCanonicalName(), "AppID: " + AppID);
        Log.d(this.getClass().getCanonicalName(), "version: " + mGson.toJson(version));
        Log.d(this.getClass().getCanonicalName(), "channelBinding: " + mGson.toJson(channelBinding));


        if (AppID == mFacetID)
            return true;
        if (AppID.length() > 512) {
            Log.d(this.getClass().getCanonicalName(), "AppID length to big");
            return false;
        }
        URL appID_url = null;
        try {
            appID_url = new URL(AppID);
            if (!AppID.contains("https://")) {
                Log.d(this.getClass().getCanonicalName(), "AppID not https!");
            }
        } catch (MalformedURLException e) {
            Log.d(this.getClass().getCanonicalName(), "AppID is not a valid url");
            return false;
        }
        try {
            //URL appID_url = new URL(AppID);
            Log.d(this.getClass().getCanonicalName(), "FacetID: " + mFacetID);
            if (mFacetID.contains("https://")) {
                Log.d(this.getClass().getCanonicalName(), "FacetID registered as url");
                URL facetID_url = new URL(mFacetID);
                if (facetID_url.getHost().equals(appID_url.getHost()))
                    return true;
            } else {
                Log.d(this.getClass().getCanonicalName(), "FacetID not an url");
                String trustedFacetsJson = Curl.getInSeparateThread(AppID, mContext);
                TrustedFacetsList trustedFacets = mGson.fromJson(trustedFacetsJson, TrustedFacetsList.class);
                if (trustedFacets.getTrustedFacets() == null || trustedFacets.getTrustedFacets().length == 0) {
                    Log.d(this.getClass().getCanonicalName(), "No trusted facets!");
                    return false;
                }

                return processTrustedFacetsList(trustedFacets, version, mFacetID);
            }
        } catch (MalformedURLException e) {
            Log.d(this.getClass().getCanonicalName(), "MalformedURLException: FacetID not an url");
            return false;
        }

        return false;
        // END: FacetValidator-processFacetID
    }

    /**
     * processTrustedFacetsList
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FacetValidator-processTrustedFacetsList}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the facet ID is trusted.
     * 
     * <p>REG 1.2.1.4.1.4.1
     * <p>AUTH 1.2.1.2.1.3.1
     * 
     * @param trustedFacetsList
     * @param version
     * @param facetId
     * @return
     */
    private boolean processTrustedFacetsList(TrustedFacetsList trustedFacetsList, Version version, String facetId){
        Log.d(this.getClass().getCanonicalName(), "processTrustedFacetsList");
        // BEGIN: FacetValidator-processTrustedFacetsList

        for (TrustedFacets trustedFacets: trustedFacetsList.getTrustedFacets()){
            // select the one with the version matching that of the protocol message version
            if ((trustedFacets.getVersion().minor >= version.minor)
                    && (trustedFacets.getVersion().major <= version.major)) {
                //The scheme of URLs in ids MUST identify either an application identity
                // (e.g. using the apk:, ios: or similar scheme) or an https: Web Origin [RFC6454].
                for (String id : trustedFacets.getIds()) {
                    if (id.equals(facetId)) {
                        return true;
                    }
                }
            }
        }
        Log.d(this.getClass().getCanonicalName(), "FacetID not found in trusted facets!");
        return false;
        // END: FacetValidator-processTrustedFacetsList
    }

    private class GetAsyncTask extends AsyncTask<Object, Integer, String> {

        private String result = null;
        private boolean done = false;
        public boolean isDone() {
            return done;
        }
        public String getResult() {
            return result;
        }
        @Override
        protected String doInBackground(Object... args) {
            result = get((String)args[0], (ChannelBinding)args[1]);
            done = true;
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            this.result = result;
            done = true;
        }

        private String toStr(InputStream responseIS) {
            String result = "";
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(responseIS));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line + "\n");
                }
                //in.close();
                result = str.toString();
                responseIS.close();
            } catch (Exception ex) {
                result = "Error";
            }
            return result;
        }

        private String get(String url, String[] header, ChannelBinding channelBinding) {
            String ret = "";
            try {
                //--------------------------------------------
                URL _url = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection)_url.openConnection();
                connection.setRequestMethod("GET");
                if (header != null) {
                    for (String h : header) {
                        String[] split = h.split(":");
                        connection.setRequestProperty(split[0], split[1]);
                    }
                }
                //connection.connect();


                int responseCode = connection.getResponseCode();
                // response code is either HTTP_MOVED_TEMP || HTTP_MOVED_PERM || HTTP_SEE_OTHER
                if (responseCode > 300 && responseCode < 304) {
                    if (!checkHeader(connection, "FIDO-AppID-Redirect-Authorized", "true"))
                        throw new Exception();
                } else if (!checkHeader(connection, "Content-Type", "application/fido.trusted-apps+json"))
                    throw new Exception();

                ret = toStr(connection.getInputStream());

                channelBinding.tlsServerCertificate = Base64.encodeToString(connection.getServerCertificates()[0].getEncoded(), Base64.URL_SAFE);
                for (Certificate cert : connection.getServerCertificates()) {
                    Log.d(this.getClass().getCanonicalName(), "-----CERTIFICATE-----");
                    Log.d(this.getClass().getCanonicalName(), cert.toString());
                }
                //Log.d(this.getClass().getCanonicalName(), "channelBinding: " + mGson.toJson(channelBinding));

                connection.disconnect();
                //--------------------------------------------
            } catch (Exception e) {
                e.printStackTrace();
                ret = "{'error_code':'connect_fail','e':'" + e + "'}";
            }

            return ret;
        }

        private boolean checkHeader(HttpURLConnection connection, String headerName, String headerValue) {
            return connection.getHeaderField(headerName).equals(headerValue);
        }

        private String get(String url, ChannelBinding channelBinding) {
            return get(url, null, channelBinding);
        }
    }
}
