package com.example.newfido.asm.cmds;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.example.newfido.asm.db.AuthenticatorIndexDbHelper;
import com.example.newfido.asm.db.controllers.AuthenticatorIndexController;
import com.example.newfido.asm.db.models.AuthenticatorIndex;
import com.example.newfido.authenticator.AuthenticatorInterface;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.ASMResponse;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.UnsignedUtil;


/**
 * Created by sorin.teican on 13-Feb-17.
 */
 

public class OpenSettings {

    private Context mContext;

    AuthenticatorIndexDbHelper mAuthenticatorIndexDbHelper;

    private AuthenticatorIndexController mAutenticatorIndexController;

    public OpenSettings(Context context) {
        mContext = context;

        mAuthenticatorIndexDbHelper = new AuthenticatorIndexDbHelper(mContext);
        mAutenticatorIndexController = new AuthenticatorIndexController(mAuthenticatorIndexDbHelper);
    }

    public ASMResponse process(ASMRequest request) throws Exception {
        ASMResponse asmResponse = new ASMResponse();

        if (!checkAuthenticatorIndex(request.authenticatorIndex)) {
            Log.d(this.getClass().getCanonicalName(), "No authenticator");
            mAuthenticatorIndexDbHelper.close();

            asmResponse.statusCode = 0x01;
            return asmResponse;
        }

        try {
            AuthenticatorInterface.command(mContext, buildCmd(request), null, null);
        } catch (Exception e) {
            e.printStackTrace();
            mAuthenticatorIndexDbHelper.close();

            asmResponse.statusCode = 0x01;
            return asmResponse;
        }

        mAuthenticatorIndexDbHelper.close();

        asmResponse.statusCode = 0x00;
        return asmResponse;
    }

    private byte[] buildCmd(ASMRequest request) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] value;

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_AUTHENTICATOR_INDEX.id));
        value = UnsignedUtil.encodeInt(request.authenticatorIndex);
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(UnsignedUtil.reverse(value));

        value = bout.toByteArray();
        bout.reset();

        bout.write(UnsignedUtil.encodeInt(TagsEnum.TAG_UAFV1_OPEN_SETTINGS_CMD.id));
        bout.write(UnsignedUtil.encodeInt(value.length));
        bout.write(value);

        return bout.toByteArray();
    }

    private boolean checkAuthenticatorIndex(int index) {
        List<AuthenticatorIndex> authenticators = mAutenticatorIndexController.getAllIndexes();
        for (AuthenticatorIndex authenticator : authenticators)
            if (authenticator.index == index)
                return true;

        return false;
    }
}
