package com.example.uprcfido;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.newssl.Curl;





public class FidoUtil {
    public final static int REG_ACTIVITY_RES_3 = 3;
    public final static int AUTH_ACTIVITY_RES_5 = 5;
    private final static String IS_UAF_CLIENT_INIT = "com.example.uprcfido.IS_UAF_CLIENT_INIT";

    public static void register(Activity activity, String url, String username) {

        Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        i.addCategory("android.intent.category.DEFAULT");
        i.setType("application/fido.uaf_client+json");

        String regRequest = getRegRequest(username, activity, url);

        Bundle data = new Bundle();
        data.putString("message", regRequest);
        data.putString("UAFIntentType", "UAF_OPERATION");
        i.putExtras(data);

        try {
            activity.startActivityForResult(i, REG_ACTIVITY_RES_3);
        } catch (Exception e) {
            System.out.println("**************************************");
            System.out.println(e);
        }

    }

    private static String getRegRequest(String username, Activity context, String url) {
        String url_complete = url + "/v1/registration/request/" + username;
        return createSeparatedProcess(url_complete, context);
    }

    public static String createSeparatedProcess(String url, Activity context) { // for get request
        try {
            return Curl.getInSeparateThread(url, context);
        } catch (Exception e) {
            return "";
        }
    }

    public static String createSeparatedProcessPost(String url_complete, String headerStr, String response, Activity context) { // for post request
        try {
            return Curl.postInSeparateThread(url_complete, headerStr, response, context);
        } catch (Exception e) {
            return "null";
        }
    }

    public static void authenticate(Activity activity, String url) {

        Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        i.addCategory("android.intent.category.DEFAULT");
        i.setType("application/fido.uaf_client+json");

        String authRequest = getAuthRequest(activity, url);

        Bundle data = new Bundle();
        data.putString("message", authRequest);
        data.putString("UAFIntentType", "UAF_OPERATION");
        i.putExtras(data);

        try {
            activity.startActivityForResult(i, AUTH_ACTIVITY_RES_5);
        } catch (Exception e) {
            System.out.println("***************Auth***********************");
            System.out.println(e);
        }
    }

    private static String getAuthRequest(Activity context, String url) {
        String url_complete = url + "/v1/authentication/request";
        return createSeparatedProcess(url_complete, context);
    }

    public static void deregister(Activity activity, String username, String url) {
        getDeregRequest(username, url, activity);
    }

    private static String getDeregRequest(String username, String url, Activity context) {
        String url_complete = url + "/v1/registration/dereg/" + username;
        return createSeparatedProcess(url_complete, context);
    }

    public static String sendRegResponse(Activity context, String url, String response) {
        String headerStr = "Content-Type:Application/json Accept:Application/json";
        String url_complete = url + "/v1/registration/response";
        return createSeparatedProcessPost(url_complete, headerStr, response, context);
    }

    public static String sendAuthResponse(Activity context, String url, String response) {
        String headerStr = "Content-Type:Application/json Accept:Application/json";
        String url_complete = url + "/v1/authentication/response";
        return createSeparatedProcessPost(url_complete, headerStr, response, context);
    }

    private static void initUAFClientIntent(Activity context) {
        Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        i.addCategory("android.intent.category.DEFAULT");
        i.setType("application/fido.uaf_client+json");
        i.putExtra("UAFIntentType", "DISCOVER");
        context.startActivityForResult(i, 1);
    }

    public static void initUAFClient(Activity activity) {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        boolean isUAFClientInit = prefs.getBoolean(IS_UAF_CLIENT_INIT, false);

        if (!isUAFClientInit) {
            initUAFClientIntent(activity);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(IS_UAF_CLIENT_INIT, true);
            editor.commit();
        }
    }

}
