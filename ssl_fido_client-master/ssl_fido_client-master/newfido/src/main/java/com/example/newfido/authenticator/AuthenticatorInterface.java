package com.example.newfido.authenticator;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.example.newfido.authenticator.cmds.*;
import com.example.newfido.tlv.ByteInputStream;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 05-Jan-17.
 */
 

public class AuthenticatorInterface {

    private static final String TAG = AuthenticatorInterface.class.getCanonicalName();

    //private static Context mContext;

    /**
     * command
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet AuthenticatorInterface-command}
     * %%% END SOURCE CODE %%%
     * <p>This function processes the request based on type.
     * 
     * <p>DISC 3
     * <p>REG 3
     * <p>AUTH 3
     * <p>DEREG 3
     * @see GetInfo#process()
     * 
     * @see Register#process(ByteInputStream)
     * 
     * @see Authenticate#process(ByteInputStream)
     * 
     * @see AuthenticatorInterface#command(Context, byte[], String, String)
     * 
     * @param context
     * @param in
     * @param deviceID
     * @param deviceType
     * @return
     * @throws Exception
     */
    public static byte[] command(Context context, final byte[] in, String deviceID, String deviceType) throws Exception {
        // BEGIN: AuthenticatorInterface-command
        if (context == null)
            throw new IllegalArgumentException("null context");
        if (in == null || in.length == 0)
            throw new IllegalArgumentException("no command data");

        ByteInputStream bin = new ByteInputStream(in);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        try {
            if (bin.available() > 0) {
                int cmd_tag = UnsignedUtil.read_UAFV1_UINT16(bin);

                if (cmd_tag == TagsEnum.TAG_UAFV1_GETINFO_CMD.id) {
                    bout.write(new GetInfo().process());
                } else if (cmd_tag == TagsEnum.TAG_UAFV1_REGISTER_CMD.id) {
                    bout.write(new Register(context, deviceID, deviceType).process(bin));
                } else if (cmd_tag == TagsEnum.TAG_UAFV1_SIGN_CMD.id) {
                    bout.write(new Authenticate(context).process(bin));
                } else if (cmd_tag == TagsEnum.TAG_UAFV1_DEREGISTER_CMD.id) {
                    bout.write(new Deregister(context).process(bin));
                } else if (cmd_tag == TagsEnum.TAG_UAFV1_OPEN_SETTINGS_CMD.id) {
                    bout.write(new OpenSettings(context).process(bin));
                } else {
                    bout.write(UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_CMD_NOT_SUPPORTED.id));
                }
            }
        } catch (IOException e) {
            bout.write(UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_ERR_UNKNOWN.id));
            e.printStackTrace();
        }

        return bout.toByteArray();
        // END: AuthenticatorInterface-command
    }

}
