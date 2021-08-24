package com.example.newfido.client;

/**
 * Created by sorin.teican on 03-Mar-17.
 */

 

public class ErrorCode {
    public static final short NO_ERROR = 0x0;
    public static final short WAIT_USER_ACTION = 0x1;
    public static final short INSECURE_TRANSPORT = 0x2;
    public static final short USER_CANCELLED = 0x3;
    public static final short UNSUPPORTED_VERSION = 0x4;
    public static final short NO_SUITABLE_AUTHENTICATOR = 0x5;
    public static final short PROTOCOL_ERROR = 0x6;
    public static final short UNNTRUSTED_FACET_ID = 0x7;
    public static final short UNKNOWN = 0xFF;
}
