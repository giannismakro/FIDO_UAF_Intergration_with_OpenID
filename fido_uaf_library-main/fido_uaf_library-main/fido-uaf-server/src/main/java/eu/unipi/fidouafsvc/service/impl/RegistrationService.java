package eu.unipi.fidouafsvc.service.impl;

import com.google.gson.Gson;
import eu.unipi.fido.uaf.msg.OperationHeader;
import eu.unipi.fido.uaf.msg.RegistrationRequest;
import eu.unipi.fido.uaf.msg.RegistrationResponse;
import eu.unipi.fidouafsvc.dao.RegistrationRecordDao;
import eu.unipi.fidouafsvc.dao.TrustedFacetDao;
import eu.unipi.fidouafsvc.model.TrustedFacet;
import eu.unipi.fidouafsvc.storage.RegistrationRecord;
import eu.unipi.fidouafsvc.storage.RequestAccountant;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SQLQuery;
import eu.unipi.fidouafsvc.model.RegistrationRecordModel;
import eu.unipi.fidouafsvc.storage.RegistrationRecord;

import org.springframework.transaction.annotation.Transactional;
import java.lang.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * This class impliments the registration service.
 */

@Service
public class RegistrationService {
	//@Autowired
	//private SessionFactory sessionFactory;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	
	Gson gson = new Gson();

	@Autowired
	TrustedFacetDao trustedFacetDao;

	@Autowired
	RegistrationRecordDao registrationRecordDao;

	@Autowired
	FetchRequestService fetchRequestService;

	@Autowired
	ProcessResponseService processResponseService;

	RequestAccountant accountant = RequestAccountant.getInstance();

	/**
	 * regReqUsername
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet RegistrationService-regReqUsername}
     * %%% END SOURCE CODE %%%
	 * <p>This function process the registration request
	 * 
	 * <p>REGreq 1.2
	 * @see RegistrationRequest
	 * @see eu.unipi.fidouafsvc.service.impl.FetchRequestService#getRegistrationRequest(String)
	 * @see eu.unipi.fidouafsvc.storage.RequestAccountant#addRegistrationRequest(RegistrationRequest)
	 * 
	 * @param username
	 * @return
	 */
	public RegistrationRequest[] regReqUsername(String username) {
		// BEGIN: RegistrationService-regReqUsername
		RegistrationRequest[] request = new RegistrationRequest[1];
		request[0] = fetchRequestService.getRegistrationRequest(username);
		accountant.addRegistrationRequest(request[0]);
		return request;
		// END: RegistrationService-regReqUsername
	}

	public RegistrationRequest[] regReqUsernameAppId(String username, String appId) {
		RegistrationRequest[] request = regReqUsername(username);
		setAppId(appId, request[0].header);

		return request;
	}

	// FIDOUAFREG IV
	/**
	 * response
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet RegistrationService-response}
     * %%% END SOURCE CODE %%%
	 * <p>This function checks the server response is not empty and stores the registration record
	 * 
	 * <p>REGres 2.2
	 * <p>@see RegistrationRecord
	 * <p>@see RegistrationResponse
	 * <p>{@link eu.unipi.fidouafsvc.service.impl.ProcessResponseService#processRegResponse(RegistrationResponse)}
	 * <p>{@link eu.unipi.fidouafsvc.dao.RegistrationRecordDao#addRegistrationRecords(RegistrationRecord[])}
	 * 
	 * @param payload
	 * @return
	 */
	public RegistrationRecord[] response(String payload) {
		// BEGIN: RegistrationService-response
		RegistrationRecord[] result = null;
		try {
			if (!payload.isEmpty()) {
				RegistrationResponse[] fromJson = gson.fromJson(payload, RegistrationResponse[].class);

				RegistrationResponse response = fromJson[0];
				result = processResponseService.processRegResponse(response);
				if (result[0].status.equals("1200")) {
					try {
						registrationRecordDao.addRegistrationRecords(result);
					} catch (HibernateException e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String sStackTrace = sw.toString();
						logger.log(Level.INFO, "@@ <-- This are our balls <start>");
						logger.log(Level.INFO, sStackTrace);
						logger.log(Level.INFO, "@@ <-- This are our balls <stop>");

						// result = new RegistrationRecord[1];
						// result[0] = new RegistrationRecord();
						logger.log(Level.INFO, "[1501]: Exception while saving registration record");
						result[0].status = "1501";
						//System.out.println("nothing found!" + errors.toString());
					}

					// } else {
					// result = new RegistrationRecord[1];
					// result[0] = new RegistrationRecord();
					// result[0].status = "Error: payload could not be empty";
					// }
				}

			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.log(Level.INFO, "[1498] RegistrationServiceException: " + sw.toString());
			result = new RegistrationRecord[1];
			result[0] = new RegistrationRecord();
			result[0].status = "1498";
		}
		return result;
		// END: RegistrationService-response
	}

	private void setAppId(String appId, OperationHeader header) {
		if (appId == null || appId.isEmpty()) {
			return;
		}

		String decodedAppId = new String(Base64.decodeBase64(appId));
		List<TrustedFacet> facets = trustedFacetDao.listAllTrustedFacets();
		if (facets == null || facets.isEmpty()){
			return;
		}
		int len = facets.size();
		for (int i = 0; i < len; i++) {
			if (decodedAppId.equals(facets.get(i).getName())) {
				header.appID = decodedAppId;
				break;
			}
				
		}
	}
}
 
