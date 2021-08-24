package com.example.uprcfido;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static com.example.uprcfido.FidoUtil.AUTH_ACTIVITY_RES_5;
import static com.example.uprcfido.FidoUtil.REG_ACTIVITY_RES_3;



public class MainActivity extends AppCompatActivity {

    private EditText et_username, et_server;
    private Button btn_reg, btn_auth, btn_dereg, btn_ct, btn_cd;
    private TextView tv_serverRes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        initMembers();

        FidoUtil.initUAFClient(this);
    }

    private void initMembers() {
        et_username = findViewById(R.id.et_username);
        et_server = findViewById(R.id.et_server);

        btn_reg = findViewById(R.id.btn_reg);
        btn_auth = findViewById(R.id.btn_auth);
        btn_dereg = findViewById(R.id.btn_dereg);

        tv_serverRes = findViewById(R.id.tv_serverResponse);

        btn_ct = findViewById(R.id.btn_ct);
        btn_ct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_username.getText().clear();
                et_server.getText().clear();
            }
        });

        btn_cd = findViewById(R.id.btn_cd);
        btn_cd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    try {
                        // clearing app data
                        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                            ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
                        } else {
                            String packageName = getApplicationContext().getPackageName();
                            Runtime runtime = Runtime.getRuntime();
                            runtime.exec("pm clear "+packageName);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    deleteSharedPreferences("com.example.newfido.asm.ASM_TOKEN_PREF_KEY");
                    deleteSharedPreferences("com.example.newfido.asm.INIT_AUTHENTICATOR_KEY");
                    deleteSharedPreferences("com.example.newfido.IS_UAF_CLIENT_INIT");
            }
        });

    }

    public void saveCache(String content) {
        String filename = "cache.uprc";
        String fileContents = "profil:" + content;
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean cacheExist() {
        FileInputStream file;
        try {
            file = openFileInput("cache.uprc");
            if (file.available() < 3)
                return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void register(View v) {
        String username = et_username.getText().toString();
        String server_url = et_server.getText().toString();
        if (cacheExist()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            // Setting Dialog Title
            alertDialog.setTitle("FIDO Info");

            // Setting Dialog Message
            alertDialog.setMessage("This version is single profile. Please Clear Data of the application and then restart");
            // Showing Alert Message
            alertDialog.show();
        } else {
            FidoUtil.register(this, server_url, username);
        }
    }

    public void authenticate(View v) {
        String server_url = et_server.getText().toString();

        FidoUtil.authenticate(this, server_url);
    }

    public void dererister(View v) {
        String username = et_username.getText().toString();
        String server_url = et_server.getText().toString();

        FidoUtil.deregister(this, username, server_url);
    }

    public void setIDCFido(View v) {
        et_server.setText("https://fidouaf.ds.unipi.gr/fido");
    }

    public void setBAAFido(View v) {
        et_server.setText("https://fidouaf.ds.unipi.gr/fido");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Jean-Didier ", "error is : " + resultCode);
        if (requestCode == REG_ACTIVITY_RES_3) {
            if (resultCode == RESULT_OK) {
                try {
                    String uafMessage = data.getStringExtra("message");
                    String res = FidoUtil.sendRegResponse(this, et_server.getText().toString(), uafMessage);
                    tv_serverRes.setText(res);
                    saveCache(res);
                } catch (Exception e) {
                    tv_serverRes.setText("Registration operation failed.\n" + e);
                }
            }
        } else if (requestCode == AUTH_ACTIVITY_RES_5) {
            if (resultCode == RESULT_OK) {
                try {
                    String uafMessage = data.getStringExtra("message");
                    String res = FidoUtil.sendAuthResponse(this, et_server.getText().toString(), uafMessage);
                    tv_serverRes.setText(res);
                    try {
                        JSONArray response = new JSONArray(res);
                        String radiusPass = response.getJSONObject(0).getString("radiusPassword");
                        if (radiusPass != null && !radiusPass.isEmpty())
                            WifiSwitcher.switchWifi(MainActivity.this, "uprc-test", et_username.getText().toString(), radiusPass);
                    } catch (JSONException e) {

                    }
                } catch (Exception e) {
                    tv_serverRes.setText("Registration operation failed.\n" + e);
                }
            }
        }
    }
}
