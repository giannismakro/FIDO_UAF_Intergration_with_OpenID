package eu.unipi.fidouafsvc.service.impl;

import eu.unipi.fidouafsvc.ops.AuthenticationResponseProcessing;
import eu.unipi.fidouafsvc.ops.RegistrationResponseProcessing;
import eu.unipi.fidouafsvc.storage.AuthenticatorRecord;
import eu.unipi.fidouafsvc.storage.RegistrationRecord;
import eu.unipi.fidouafsvc.storage.StorageInterface;
import eu.unipi.fidouafsvc.util.JdbcRadius;
import eu.unipi.fidouafsvc.util.ResponseHelper;
import org.apache.commons.codec.binary.Base64;
import eu.unipi.fido.uaf.msg.AuthenticationResponse;
import eu.unipi.fido.uaf.msg.RegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import eu.unipi.fidouafsvc.authentication.processing.AuthenticationStatusProvider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
/**
 * Created by sorin.teican on 8/29/2016.
 */
/**
 * Reformatted by Makropodis Ioannis on May-18.
 */
/*
 * This class impliments the response service.
 */

@Service
public class ProcessResponseService {

	private static final int SERVER_DATA_EXPIRY_IN_MS = 5 * 60 * 1000;

	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	private NotaryServiceImpl notaryService;

	@Autowired
	@Qualifier("storageDao")
	private StorageInterface storageDao;

	@Autowired
	private ResponseHelper responseHelper;

	private JdbcRadius jdbcRadius = new JdbcRadius();

	// FIDOUAFAUT V
	/**
	 * processAuthResponse
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ProcessResponseService-processAuthResponse}
     * %%% END SOURCE CODE %%%
	 * <p>This function processes the authentication response
	 * 
	 * <p>AUTHres 2.2.1
	 * @see AuthenticatorRecord
	 * @see eu.unipi.fidouafsvc.ops.AuthenticationResponseProcessing#verify(AuthenticationResponse, StorageInterface)
	 * @see eu.unipi.fidouafsvc.service.impl.ProcessResponseService#generateAuthenticationId()
	 * @see eu.unipi.fidouafsvc.service.impl.ProcessResponseService#generateRadiusPassword()
	 * @see eu.unipi.fidouafsvc.storage.StorageInterface#saveAuthenticationId(String, String, String)
	 * 
	 * @param response
	 * @return
	 */
	public AuthenticatorRecord[] processAuthResponse(AuthenticationResponse response) throws IOException{
		// BEGIN: ProcessResponseService-processAuthResponse
		AuthenticatorRecord[] result = null;
		String keycloakuser= "user";
		String fidoauthenticationid="Not Authenticated";
		try {
			
			result = new AuthenticationResponseProcessing(SERVER_DATA_EXPIRY_IN_MS, notaryService, responseHelper)
					.verify(response, storageDao);
			//System.exit(0);
			String authenticationId = generateAuthenticationId();
			fidoauthenticationid=authenticationId;
			String radiusPassword = generateRadiusPassword();
			int len = result.length;
			for (int i = 0; i < len; i++) {
				result[i].authenticationId = authenticationId;
				result[i].radiusPassword = radiusPassword;
				storageDao.saveAuthenticationId(authenticationId, result[i].username, result[0].timestamp);
				// jdbcRadius.writeUser(result[i].username, radiusPassword);
				keycloakuser=result[i].username;
			}
			
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!..............................." + e.getMessage());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.log(Level.INFO, "[1500]: EXCEPTION: " + sw.toString());
			result = new AuthenticatorRecord[1];
			result[0] = new AuthenticatorRecord();
			result[0].status = "1502";
			e.printStackTrace();
		}

		// result[0].status = "1200";
		AuthenticationStatusProvider asp = new AuthenticationStatusProvider(keycloakuser, fidoauthenticationid);

			asp.authenticateUser();
		return result;
		// END: ProcessResponseService-processAuthResponse
	}

	/**
	 * generateAuthenticationId
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ProcessResponseService-generateAuthenticationId}
     * %%% END SOURCE CODE %%%
	 * <p>This function generates and authentication id
	 * 
	 * <p>AUTHres 2.2.1.2
	 * 
	 * @return
	 */
	public String generateAuthenticationId() {
		// BEGIN: ProcessResponseService-generateAuthenticationId
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[12];
		random.nextBytes(bytes);

		return "fido_auth_id_" + Base64.encodeBase64URLSafeString(bytes);
		// END: ProcessResponseService-generateAuthenticationId
	}

	/**
	 * generateRadiusPassword
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ProcessResponseService-generateRadiusPassword}
     * %%% END SOURCE CODE %%%
	 * <p>This function generates a radius password
	 * 
	 * <p>AUTHres 2.2.1.3
	 * 
	 * @return
	 */
	public String generateRadiusPassword() {
		// BEGIN: ProcessResponseService-generateRadiusPassword
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[20];
		random.nextBytes(bytes);

		return Base64.encodeBase64URLSafeString(bytes);
		// END: ProcessResponseService-generateRadiusPassword
	}

	// FIDOUAFREG V
	/**
	 * processRegResponse
	 * <p>%%% BEGIN SOURCE CODE %%%
     * {@codesnippet ProcessResponseService-processRegResponse}
     * %%% END SOURCE CODE %%%
	 * <p>This function processes the registration response
	 * 
	 * <p>REGres 2.2.1
	 * @see RegistrationRecord
	 * @see eu.unipi.fidouafsvc.ops.RegistrationResponseProcessing#processResponse(RegistrationResponse)
	 * 
	 * @param response
	 * @return
	 */
	public RegistrationRecord[] processRegResponse(RegistrationResponse response) {
		// BEGIN: ProcessResponseService-processRegResponse
		RegistrationRecord[] result = null;

		try {
			result = new RegistrationResponseProcessing(SERVER_DATA_EXPIRY_IN_MS, notaryService, responseHelper,
					storageDao).processResponse(response);
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!..............................." + e.getMessage());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.log(Level.INFO, "[1505]: EXCEPTION: " + sw.toString());
			result = new RegistrationRecord[1];
			result[0] = new RegistrationRecord();
			result[0].status = "1505";
		}

		// result[0].status = "1200";

		return result;
		// END: ProcessResponseService-processRegResponse
	}
}
