package com.example.newfido.authenticator.db.models;

/**
 * Created by sorin.teican on 04-Jan-17.
 */
 

public class RawKeyHandle {
    public String KHAccessToken; // size depends on hashing alg.
    public String Username; // max 128 bytes
    public String KeyID;
}
