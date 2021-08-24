package com.example.newfido.msg.client;


import com.example.newfido.msg.DisplayPNGCharacteristicsDescriptor;
import com.example.newfido.msg.Version;

/**
 * Created by sorin.teican on 24-Feb-17.
 */
 
// DISC
public class Authenticator {
    public String title;
    public String aaid;
    public String description;
    public Version[] supportedUAFVersions;
    public String assertionScheme;
    public short authenticationAlgorithm;
    public short[] attestationTypes;
    public long userVerification;
    public short keyProtection;
    public short matcherProtection;
    public long attachmentHint;
    public boolean isSecondFactorOnly;
    public short tcDisplay;
    public String tcDisplayContentType;
    public DisplayPNGCharacteristicsDescriptor[] tcDisplayPNGCharacteristics;
    public String icon;
    public String[] supportedExtensionIDs;
}
