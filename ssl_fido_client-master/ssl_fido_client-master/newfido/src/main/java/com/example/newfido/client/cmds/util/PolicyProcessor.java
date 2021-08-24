package com.example.newfido.client.cmds.util;

import android.app.Activity;
import android.content.Intent;

import com.example.newfido.client.activity.ClientEntrypoint;
import com.example.newfido.client.db.controller.AuthenticatorInfoController;
import com.example.newfido.msg.MatchCriteria;
import com.example.newfido.msg.Policy;
import com.example.newfido.msg.Version;
import com.example.newfido.msg.asm.ASMRequest;
import com.example.newfido.msg.asm.Request;
import com.example.newfido.msg.asm.obj.AppRegistration;
import com.example.newfido.msg.asm.obj.AuthenticatorInfo;
import com.example.newfido.msg.asm.obj.GetRegistrationsOut;
import com.example.newfido.tlv.Tags;
import com.example.newfido.tlv.TagsEnum;
import com.example.newfido.tlv.TlvAssertionParser;
import com.google.gson.Gson;


import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Created by sorin.teican on 23-Feb-17.
 */
 
public class PolicyProcessor {

    public interface GetRegistrationsResponse {
        public void onResponse(GetRegistrationsOut registrationsOut);
    }

    private Activity mContext;
    private AuthenticatorInfoController mAuthenticatorInfoController;
    private GetRegistrationsResponse mGetRegistrationsASMResponse;

    private Gson mGson;

    private boolean mHasKeyIDs;
    private boolean mHasAuthenticatorVersion;

    public PolicyProcessor(Activity context, AuthenticatorInfoController authenticatorInfoController) {
        mContext = context;
        mAuthenticatorInfoController = authenticatorInfoController;
    }

    /**
     * processPolicy
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet PolicyProcessor-processPolicy}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the authenticator match with the server policy.
     * 
     * @see AuthenticatorInfo
     * @see MatchCriteria
     * 
     * <p>REG 1.2.1.4.1.5
     * <p>AUTH 1.2.1.2.1.4
     * @param serverPolicy
     * @return
     */
    public List<AuthenticatorInfo> processPolicy(Policy serverPolicy) {
        // BEGIN: PolicyProcessor-processPolicy
        List<AuthenticatorInfo> authenticatorInfos = mAuthenticatorInfoController.getAllAuthenticatorsInfo();
        for (Iterator<AuthenticatorInfo> it = authenticatorInfos.iterator(); it.hasNext(); ) {
            AuthenticatorInfo info = it.next();
            if (serverPolicy.disallowed != null) {
                for (MatchCriteria criteria : serverPolicy.disallowed) {
                    if (matchCriteriaAuthenticator(criteria, info) > 0)
                        it.remove();
                }
            }
        }

        for (MatchCriteria[] criterias : serverPolicy.accepted) {
            for (MatchCriteria criteria : criterias) {
                for (AuthenticatorInfo info : authenticatorInfos)
                    info.score = matchCriteriaAuthenticator(criteria, info);
            }
        }

        // Descending
        Collections.sort(authenticatorInfos, new Comparator<AuthenticatorInfo>() {
            @Override
            public int compare(AuthenticatorInfo lhs, AuthenticatorInfo rhs) {
                return lhs.score > rhs.score ? -1 : (lhs.score < rhs.score) ? 1 : 0;
            }
        });

        return authenticatorInfos;
        // END: PolicyProcessor-processPolicy
    }

    /**
     * matchCriteriaAuthenticator
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet PolicyProcessor-matchCriteriaAuthenticator}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the authenticator matches with the given criteria.
     * <p>REG 1.2.1.4.1.5.2
     * <p>AUTH 1.2.1.2.1.4.2
     * 
     * @see GetRegistrationsOut
     */
    private int matchCriteriaAuthenticator(final MatchCriteria criteria, AuthenticatorInfo authenticatorInfo) {
        // BEGIN: PolicyProcessor-matchCriteriaAuthenticator
        final int criteriaMatched[] = new int[13];

        if (criteria.aaid != null && criteria.aaid.length > 0) {
            for (String aaid : criteria.aaid) {
                if (authenticatorInfo.aaid.equals(aaid)) {
                    criteriaMatched[0] = 1;
                    break;
                }
                else criteriaMatched[0] = -1;
            }
        }

        if (criteria.vendorID != null && criteria.vendorID.length > 0) {
            String[] vendor_model = authenticatorInfo.aaid.split("#");
            for (String vendorID : criteria.vendorID) {
                if (vendor_model[0].equals(vendorID)) {
                    criteriaMatched[1] = 1;
                    break;
                }
                else criteriaMatched[1] = -1;
            }
        }

        if (criteria.keyIDs != null && criteria.keyIDs.length > 0) {
            mHasKeyIDs = true;
            mGetRegistrationsASMResponse = new GetRegistrationsResponse() {
                @Override
                public void onResponse(GetRegistrationsOut registrationsOut) {
                    int found = 0;
                    for (String keyID : criteria.keyIDs) {
                        if (checkKeyIDinRegistration(keyID, registrationsOut.appRegs))
                            found++;
                    }
                    if (found == criteria.keyIDs.length)
                        criteriaMatched[2] = 1;
                    else criteriaMatched[2] = -1;
                }
            };
            checkKeyIDs(criteria.keyIDs, authenticatorInfo);
        }

        if (criteria.userVerification != null && criteria.userVerification > 0) {
            if ((authenticatorInfo.userVerification == criteria.userVerification)
                    || (((authenticatorInfo.userVerification & 0x400) == 0) &&
                    ((criteria.userVerification & 0x400) == 0) && ((authenticatorInfo.userVerification & 0x400) != 0)))
                criteriaMatched[3] = 1;
            else criteriaMatched[3] = -1;
        }

        if (criteria.keyProtection != null && criteria.keyProtection > 0) {
            if (authenticatorInfo.keyProtection == criteria.keyProtection)
                criteriaMatched[4] = 1;
            else criteriaMatched[4] = -1;
        }

        if (criteria.matcherProtection != null && criteria.matcherProtection > 0) {
            if (authenticatorInfo.matcherProtection == criteria.matcherProtection)
                criteriaMatched[5] = 1;
            else criteriaMatched[5] = -1;
        }

        if (criteria.attachmentHint != null && criteria.attachmentHint > 0) {
            if (authenticatorInfo.attachmentHint == criteria.attachmentHint)
                criteriaMatched[6] = 1;
            else criteriaMatched[6] = -1;
        }

        if (criteria.tcDisplay != null && criteria.tcDisplay > 0) {
            if (authenticatorInfo.tcDisplay == criteria.tcDisplay)
                criteriaMatched[7] = 1;
            else criteriaMatched[7] = -1;
        }

        if (criteria.authenticationAlgorithms != null && criteria.authenticationAlgorithms.length > 0) {
            for (int authAlg : criteria.authenticationAlgorithms)
                if (authAlg == authenticatorInfo.authenticationAlgorithm) {
                    criteriaMatched[8] = 1;
                    break;
                } else criteriaMatched[8] = -1;
        }

        if (criteria.assertionSchemes != null && criteria.assertionSchemes.length > 0) {
            for (String assertionScheme : criteria.assertionSchemes) {
                if (assertionScheme.equals(authenticatorInfo.assertionScheme)) {
                    criteriaMatched[9] = 1;
                    break;
                } else criteriaMatched[9] = -1;
            }
        }

        if (criteria.attestationTypes != null && criteria.attestationTypes.length > 0) {
            for (int attestationType : criteria.attestationTypes)
                for (int at : authenticatorInfo.attestationTypes) {
                    if (attestationType == at) {
                        criteriaMatched[10] = 1;
                        break;
                    } else criteriaMatched[10] = -1;
                }
        }

        if (criteria.authenticatorVersion != null && criteria.authenticatorVersion > -1) {
            mHasAuthenticatorVersion = true;
        }

        int sum = 0;
        for (int matched : criteriaMatched) {
            if (matched == -1)
                return -1;
            sum += matched;
        }

        return sum;
        // END: PolicyProcessor-matchCriteriaAuthenticator
    }

    private boolean checkKeyIDinRegistration(String keyID, AppRegistration[] appRegs) {
        for (AppRegistration appReg : appRegs) {
            for (String appRegKeyID : appReg.keyIDs)
                if (appRegKeyID.equals(keyID))
                    return true;
        }

        return false;
    }

    /**
     * checkVersion
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet PolicyProcessor-checkVersion}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the authenticator match with the policy of the server.
     * @see TlvAssertionParser
     * @see Tags
     * @see MatchCriteria
     * 
     * @param policy
     * @param assertion
     * @return
     * @throws Exception
     */
    public boolean checkVersion(Policy policy, String assertion) throws Exception {
        // BEGIN: PolicyProcessor-checkVersion
        TlvAssertionParser tlvAssertionParser = new TlvAssertionParser();
        Tags assertionTags = tlvAssertionParser.parse(assertion);

        if (assertionTags.getTags().containsKey(TagsEnum.TAG_ASSERTION_INFO.id)) {
            byte[] value = assertionTags.getTags().get(TagsEnum.TAG_ASSERTION_INFO.id).get(0).value;
            int version = ByteBuffer.allocate(4).wrap(value).getInt();

            for (MatchCriteria[] criterias : policy.accepted) {
                for (MatchCriteria criteria : criterias)
                    if (criteria.authenticatorVersion == version)
                        return true;
            }
            return false;
        } else return false;
        // END: PolicyProcessor-checkVersion
    }

    private void checkKeyIDs(String[] keyIDs, AuthenticatorInfo info) {
        ASMRequest asmRequest = new ASMRequest();
        asmRequest.requestType = Request.GetRegistrations;
        asmRequest.asmVersion = new Version(1, 0);
        asmRequest.authenticatorIndex = info.authenticatorIndex;

        Intent request = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        request.addCategory("android.intent.category.DEFAULT");
        request.setType("application/fido.uaf_asm+json");
        request.putExtra("message", mGson.toJson(asmRequest));

        mContext.startActivityForResult(request, ClientEntrypoint.GET_REGISTRATIONS_REQ_CODE);
    }

    /**
     * hasAuthenticatorVersion
     * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet PolicyProcessor-hasAuthenticatorVersion}
     * %%% END SOURCE CODE %%%
     * <p>This function checks if the authenticator has version.
     * 
     * @return
     */
    public boolean hasAuthenticatorVersion() {
        // BEGIN: PolicyProcessor-hasAuthenticatorVersion
        return mHasAuthenticatorVersion;
        // END: PolicyProcessor-hasAuthenticatorVersion
    }

    public boolean hasKeyIDs() {
        return mHasKeyIDs;
    }
}
