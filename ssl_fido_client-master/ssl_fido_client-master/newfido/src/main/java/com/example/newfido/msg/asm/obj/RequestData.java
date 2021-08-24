package com.example.newfido.msg.asm.obj;

import com.example.newfido.msg.Transaction;

/**
 * Created by sorin.teican on 09-Mar-17.
 */


public class RequestData {
    public String appID;
    public String username;
    public String finalChallenge;
    public short attestationType;
    public String[] keyIDs;
    public Transaction[] transaction;
    public String keyID;
}
