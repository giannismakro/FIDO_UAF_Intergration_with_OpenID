package com.example.newfido.asm;

import android.util.Log;

import com.example.newfido.asm.activity.ASMEntrypoint;
import com.example.newfido.asm.cmds.*;
import com.example.newfido.msg.asm.ASMRequest;
import com.google.gson.Gson;


/**
 * Created by sorin.teican on 11-Jan-17.
 */



public class ASMInterface {

    private Gson mGson;
    private ASMEntrypoint mContext;

    private String mCallerID;
    private String mPersonaID;
    private String mASMToken;

    private String mDeviceID;
    private String mDeviceType;

    public ASMInterface(ASMEntrypoint context, String CallerID, String PersonaID, String ASMToken, String deviceID, String deviceType) {
        mContext = context;
        mGson = new Gson();

        mCallerID = CallerID;
        mPersonaID = PersonaID;
        mASMToken = ASMToken;

        mDeviceID = deviceID;
        mDeviceType = deviceType;
    }

    /**
     * command
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ASMInterface-command}
     * %%% END SOURCE CODE %%%
     * <p>This function creates a request to Authenticator based on request type.
     * 
     * <p>DISC 2.5.1.2
     * <p>REG 2.5.1.1.1.2
     * <p>AUTH 2.5.1.1.1.2
     * <p>DEREG 2.5.1.1.1.2
     * @see ASMRequest
     * 
     * @see GetInfo
     * @see GetInfo#process(ASMRequest)
     * 
     * @see Register
     * @see Register#process(ASMRequest)
     * 
     * @see Authenticate
     * @see Authenticate#process(ASMRequest)
     * 
     * @see Deregister
     * @see Deregister#process(ASMRequest)
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public String command(String request) throws Exception {
        Log.d(this.getClass().getCanonicalName(), "command");
        // BEGIN: ASMInterface-command

        String response = null;

        if (request.indexOf("GetInfo") > 0) {
            Log.d(this.getClass().getCanonicalName(), "Received get info");
            ASMRequest getInfoReq = mGson.fromJson(request, ASMRequest.class);
            GetInfo getInfoCmd = new GetInfo(mContext);
            response = mGson.toJson(getInfoCmd.process(getInfoReq));
            Log.d(this.getClass().getCanonicalName(), "Processed get info");
        } else if (request.indexOf("Register") > 0) {
            Log.d(this.getClass().getCanonicalName(), "Received register command");
            ASMRequest registerReq = mGson.fromJson(request, ASMRequest.class);
            Register registerCmd = new Register(mContext, mCallerID, mPersonaID, mASMToken, mDeviceID, mDeviceType);
            response = mGson.toJson(registerCmd.process(registerReq));
            Log.d(this.getClass().getCanonicalName(), "Processed register commmand");
        } else if (request.indexOf("Authenticate") > 0) {
            Log.d(this.getClass().getCanonicalName(), "Received authenticate command");
            ASMRequest authenticateReq = mGson.fromJson(request, ASMRequest.class);
            Authenticate authenticateCmd = new Authenticate(mContext, mCallerID, mPersonaID, mASMToken);
            response = mGson.toJson(authenticateCmd.process(authenticateReq));
        } else if (request.indexOf("Deregister") > 0) {
            Log.d(this.getClass().getCanonicalName(), "Received deregister command");
            ASMRequest deregisterReq = mGson.fromJson(request, ASMRequest.class);
            Deregister deregisterCmd = new Deregister(mContext, mCallerID, mPersonaID, mASMToken);
            response = mGson.toJson(deregisterCmd.process(deregisterReq));
            Log.d(this.getClass().getCanonicalName(), "Processed deregister command");
        } else if (request.indexOf("GetRegistrations") > 0) {
            Log.d(this.getClass().getCanonicalName(), "Received get registrations command");
            ASMRequest getRegistrationsReq = mGson.fromJson(request, ASMRequest.class);
            GetRegistrations getRegistrationsCmd = new GetRegistrations(mContext, mCallerID);
            response = mGson.toJson(getRegistrationsCmd.process(getRegistrationsReq));
            Log.d(this.getClass().getCanonicalName(), "Processed get registrations command");
        } else if (request.indexOf("OpenSettings") > 0) {
            Log.d(this.getClass().getCanonicalName(), "Received opensettings command");
            ASMRequest openSettingsReq = mGson.fromJson(request, ASMRequest.class);
            OpenSettings openSettingsCmd = new OpenSettings(mContext);
            response = mGson.toJson(openSettingsCmd.process(openSettingsReq));
            Log.d(this.getClass().getCanonicalName(), "Processed opensettings command");
        }

        return response;
        // END: ASMInterface-command
    }

}
