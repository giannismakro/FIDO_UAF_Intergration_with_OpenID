package com.example.newfido.asm.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newfido.R;

/**
 * Created by sorin.teican on 08-Feb-17.
 */


public class ConfirmTransactionDialog extends DialogFragment {

    protected String mTextContent = null;
    protected Bitmap mImageContent = null;
    //protected int mImageWidth = 0, mImageHeight;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.transaction_dialog_layout, null);

        if (mTextContent == null && mImageContent == null) {
            Log.d(this.getClass().getName(), "no content type to set");
        } else if (mTextContent != null && mImageContent == null) {
            TextView tv_content = (TextView) layout.findViewById(R.id.tv_content);
            tv_content.setText(mTextContent);
        } else {
            ImageView iv_content = (ImageView) layout.findViewById(R.id.iv_content);
            iv_content.setImageBitmap(mImageContent);
        }
        builder.setView(layout);

        return builder.create();
    }
}
