package com.example.newfido.client.cmds.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;

import com.example.newfido.msg.*;


/**
 * Created by sorin.teican on 03-Mar-17.
 */
 

public class FieldValidator {

    public static boolean checkDuplicateProtocolDictionaries(UAFRequest[] requests) {
        int len = requests.length;
        for (int i = 0; i < len; i++) {
            if (i == len - 1)
                return true;
            Version vi = requests[i].header.upv;
            for (int j = i + 1; j < len; j++) {
                Version vj = requests[j].header.upv;
                if (vi.major == vj.major && vi.minor == vj.minor)
                    return false;
            }
        }

        return true;
    }

    /**
     * checkHeader
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FieldValidator-checkHeader}
     * %%% END SOURCE CODE %%%
     * <p>This function compares the operation type of the request with a string.
     * 
     * <p>REG 1.2.1.4.1.1
     * <p>AUTH 1.2.1.2.1.1
     * 
     * @param context
     * @param header
     * @param op
     * @return
     */
    public static boolean checkHeader(Activity context, OperationHeader header, String op) {
        // BEGIN: FieldValidator-checkHeader
        if (header == null) return false;

        if (header.op == null || !header.op.equals(op)) return false;

        if (header.upv == null || header.upv.major == null ||
                header.upv.minor == null || !(header.upv.major == 1 && header.upv.minor == 0)) unsupportedVersion(context);
        // appID is verified bellow.

        if (header.serverData == null || header.serverData.isEmpty() || header.serverData.length() > 1536) return false;

        return true;
        // END: FieldValidator-checkHeader
    }

    public static void unsupportedVersion(Activity context) {
        Intent intent = new Intent();
        intent.putExtra("UAFIntentType", "UAF_OPERATION_RESULT");
        intent.putExtra("componentName", context.getComponentName().flattenToString());
        intent.putExtra("errorCode", (short)0x4);

        context.setResult(Activity.RESULT_OK, intent);
        context.finish();
    }

    /**
     * checkChallenge
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FieldValidator-checkChallenge}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the Challenge of the request is not null.
     * 
     * <p>REG 1.2.1.4.1.2
     * <p>AUTH 1.2.1.2.1.2
     * 
     * @param challenge
     * @return
     */
    public static boolean checkChallenge(String challenge) {
        // BEGIN: FieldValidator-checkChallenge
        if (challenge == null || challenge.isEmpty()) return false;
        try {
            Base64.decode(challenge, Base64.URL_SAFE);
        } catch (IllegalArgumentException e) {
            // challenge not base64url encoded.
            return false;
        }
        if (challenge.length() < 8 || challenge.length() > 64) return false;

        return true;
        // END: FieldValidator-checkChallenge
    }

    /**
     * checkUsername
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FieldValidator-checkUsername}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the Username of the request is not null.
     * 
     * <p>REG 1.2.1.4.1.3
     * @param challenge
     * @return
     */
    public static boolean checkUsername(String username) {
        // BEGIN: FieldValidator-checkUsername
        if (username == null || username.isEmpty() || username.length() > 128) return false;

        return true;
        // END: FieldValidator-checkUsername
    }

    public static boolean checkPolicy(Policy policy) {
        if (policy == null) return false;

        return true;
    }

    private static boolean checkAAID(String aaid) {
        if (aaid == null || aaid.isEmpty() || aaid.length() > 9) return false;

        String[] vm = aaid.split("#");
        if (vm.length > 2) return false;
        for (String s : vm)
            if (!isHex(s))
                return false;

        return true;
    }

    private static boolean isHex(String str) {
        if (str == null || str.isEmpty())
            return false;

        for (char c : str.toCharArray()) {
            if (Character.digit(c, 16) == -1)
                return false;
        }

        return true;
    }

    private static boolean checkKeyID(String keyID) {
        if (keyID == null || keyID.isEmpty()) return false;
        if (keyID.length() < 32 || keyID.length() > 2048) return false;
        try {
            Base64.decode(keyID, Base64.URL_SAFE);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    /**
     * checkAuthenticators
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FieldValidator-checkAuthenticators}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the authenticator is not null.
     * 
     * <p>DEREG
     * 
     * @param authenticators
     * @return
     */
    public static boolean checkAuthenticators(DeregisterAuthenticator[] authenticators) {
        // BEGIN: FieldValidator-checkAuthenticators
        if (authenticators == null || authenticators.length == 0)
            return false;

        for (DeregisterAuthenticator authenticator : authenticators) {
            if (!checkAAID(authenticator.aaid)) return false;
            if (!checkKeyID(authenticator.keyID)) return false;
        }

        return true;
        // END: FieldValidator-checkAuthenticators
    }


}
