package com.example.newfido.msg;

/**
 * Created by sorin.teican on 01-Mar-17.
 */
 

public class MetadataStatement {
    public String AAID;
    public String description;
    public short authenticatorVersion;
    public Version[] upv;
    public String assertionScheme;
    public short authenticationAlgorithm;
    public short publicKeyAlgAndEncoding;
    public short[] attestationTypes;
    public VerificationMethodANDCombinations[] userVerificationDetails;
    public short keyProtection;
    public short matcherProtection;
    public long attachmentHint;
    public boolean isSecondFactoryOnly;
    public short tcDisplay;
    public String tcDisplayContentType;
    public DisplayPNGCharacteristicsDescriptor[] tcDisplayPNGCharacteristics;
    public String[] attestationRootCertificates;
    public String icon;
}
