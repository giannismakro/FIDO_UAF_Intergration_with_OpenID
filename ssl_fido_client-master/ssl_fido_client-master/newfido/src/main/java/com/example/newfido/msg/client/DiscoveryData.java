package com.example.newfido.msg.client;

import com.example.newfido.msg.Version;

/**
 * Created by sorin.teican on 24-Feb-17.
 */
 
// DISC
public class DiscoveryData {
    public Version[] supportedUAFVersions;
    public String clientVendor;
    public Version clientVersion;
    public Authenticator[] availableAuthenticators;
}
