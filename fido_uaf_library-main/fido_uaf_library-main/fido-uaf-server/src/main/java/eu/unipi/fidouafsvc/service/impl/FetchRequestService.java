package eu.unipi.fidouafsvc.service.impl;

import eu.unipi.fido.uaf.msg.AuthenticationRequest;
import eu.unipi.fido.uaf.msg.RegistrationRequest;
import eu.unipi.fidouafsvc.model.FidoConfig;
import eu.unipi.fidouafsvc.ops.AuthenticationRequestGeneration;
import eu.unipi.fidouafsvc.ops.RegistrationRequestGeneration;
import eu.unipi.fidouafsvc.storage.StorageInterface;
import eu.unipi.fidouafsvc.util.RequestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by sorin.teican on 8/29/2016.
 */

/*
 * This class impliments the request service.
 */

@Service
public class FetchRequestService {

	@Autowired
	private NotaryServiceImpl notaryService;

	@Autowired
	@Qualifier("storageDao")
	private StorageInterface storageDao;

	@Autowired
	private RequestHelper requestHelper;

	@Value("${appId}")
	private String appId;
	private String[] aaids = null;

	@Autowired
	public FetchRequestService(FidoConfig config) {
		appId = config.getAppId();
		aaids = config.getAaids();
	}

	/**
	 * getRegistrationRequest
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FetchRequestService-getRegistrationRequest}
     * %%% END SOURCE CODE %%%
	 * <p>This function creates a registration request and returns it
	 * 
	 * <p>REGreq 1.2.1
	 * @see RegistrationRequest
	 * @see eu.unipi.fidouafsvc.ops.RegistrationRequestGeneration#createRegistrationRequest(String, eu.unipi.fido.uaf.crypto.Notary)
	 * 
	 * @param username
	 * @return
	 */
	public RegistrationRequest getRegistrationRequest(String username) {
		// BEGIN: FetchRequestService-getRegistrationRequest
		
		RegistrationRequest request = new RegistrationRequestGeneration(appId + "/v1/trustedfacets", aaids,
				requestHelper).createRegistrationRequest(username, notaryService);

		return request;
		// END: FetchRequestService-getRegistrationRequest
	}

	/**
	 * getAuthenticationRequest
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet FetchRequestService-getAuthenticationRequest}
     * %%% END SOURCE CODE %%%
	 * <p>This function creates an authentication request and returns it
	 * 
	 * <p>AUTHreq 1.2.1
	 * @see AuthenticationRequest
	 * @see eu.unipi.fidouafsvc.ops.AuthenticationRequestGeneration#createAuthenticationRequest(eu.unipi.fido.uaf.crypto.Notary)
	 * 
	 * @return
	 */
	public AuthenticationRequest getAuthenticationRequest() {
		// BEGIN: FetchRequestService-getAuthenticationRequest
		AuthenticationRequest request = new AuthenticationRequestGeneration(appId + "/v1/trustedfacets", aaids,
				requestHelper).createAuthenticationRequest(notaryService);

		return request;
		// END: FetchRequestService-getAuthenticationRequest
	}

	public AuthenticationRequest getAuthenticationRequest(String username) {
		AuthenticationRequest request = new AuthenticationRequestGeneration(appId + "/v1/trustedfacets", aaids,
				username, storageDao, requestHelper).createAuthenticationRequest(notaryService);

		return request;
	}
}
