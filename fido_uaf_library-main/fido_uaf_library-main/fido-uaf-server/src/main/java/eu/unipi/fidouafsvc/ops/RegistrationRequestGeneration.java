package eu.unipi.fidouafsvc.ops;

import eu.unipi.fido.uaf.crypto.Notary;
import eu.unipi.fido.uaf.msg.Operation;
import eu.unipi.fido.uaf.msg.OperationHeader;
import eu.unipi.fido.uaf.msg.RegistrationRequest;
import eu.unipi.fido.uaf.msg.Version;
import eu.unipi.fidouafsvc.util.RequestHelper;

/*
 * This class generates Registration Requests.
 */

public class RegistrationRequestGeneration {

	private RequestHelper requestHelper;

	public static final String APP_ID = "https://uaf.ebay.com/uaf/facets";
	private String appId = APP_ID;
	private String[] acceptedAaids;

	// Empty Constractor
	public RegistrationRequestGeneration() {

	}

	// Constractor with 3 parameters
	public RegistrationRequestGeneration(String appId, String[] acceptedAaids, RequestHelper requestHelper) {
		this.requestHelper = requestHelper;
		this.appId = appId;
		this.acceptedAaids = acceptedAaids;
	}

	// This function creates an Registration request
	// FIDOUAFREG II
	/**
	 * createRegistrationRequest
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet RegistrationRequestGeneration-createRegistrationRequest}
     * %%% END SOURCE CODE %%%
	 * <p>This function creates a registration request
	 * 
	 * <p>REGreq 1.2.1.1
	 * @see RegistrationRequest
	 * @see OperationHeader
	 * @see eu.unipi.fidouafsvc.util.RequestHelper#generateChallenge()
	 * @see eu.unipi.fidouafsvc.util.RequestHelper#generateServerData(String, String, Notary)
	 * @see eu.unipi.fidouafsvc.util.RequestHelper#constructPolicy(String[])
	 * 
	 * @param username
	 * @param notary
	 * @return
	 */
	public RegistrationRequest createRegistrationRequest(String username, Notary notary) {
		// BEGIN: RegistrationRequestGeneration-createRegistrationRequest
		RegistrationRequest regRequest = new RegistrationRequest();
		regRequest.challenge = requestHelper.generateChallenge();
		OperationHeader header = new OperationHeader();
		header.serverData = requestHelper.generateServerData(username, regRequest.challenge, notary);
		regRequest.header = header;
		regRequest.header.op = Operation.Reg;
		regRequest.header.appID = appId;
		regRequest.header.upv = new Version(1, 0);

		regRequest.policy = requestHelper.constructPolicy(acceptedAaids);
		regRequest.username = username;
		return regRequest;
		// END: RegistrationRequestGeneration-createRegistrationRequest
	}
}
