package com.example.newfido.authenticator.cmds;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.io.ByteArrayOutputStream;

import com.example.newfido.tlv.ByteInputStream;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 10-Jan-17.
 */
 

public class OpenSettings {

    private Context mContext;

    private byte mAuthenticatorIndex = -1;

    public OpenSettings(Context context) {
        mContext = context;
    }

    public byte[] process(ByteInputStream cmd) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        getCmdParams(cmd);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_STATUS_CODE.id));
        value = UnsignedUtil.encodeInt(TagsEnum.UAF_CMD_STATUS_OK.id);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        value = bout.toByteArray();
        bout.reset();

        Intent i = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        mContext.startActivity(i);

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_OPEN_SETTINGS_CMD_RESPONSE.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
    }

    private void getCmdParams(ByteInputStream cmd) throws Exception {
        int tag, len;

        while (cmd.available() > 0) {
            tag = UnsignedUtil.read_UAFV1_UINT16(cmd);

            if (tag == TagsEnum.TAG_AUTHENTICATOR_INDEX.id) {
                len = UnsignedUtil.read_UAFV1_UINT16(cmd);
                mAuthenticatorIndex = (byte) cmd.readUnsignedByte();
            }
        }
    }
}
