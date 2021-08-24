package eu.unipi.fidouafsvc.configuration;

import eu.unipi.fidouafsvc.model.FidoConfig;
import eu.unipi.fidouafsvc.util.RequestHelper;
import eu.unipi.fidouafsvc.util.ResponseHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sorin.teican on 8/30/2016.
 */

/**
 * Load server's default configuration.
 */

@Configuration
@ComponentScan({ "eu.unipi.fidouafsvc.configuration" })
public class OtherConfig {

	@Bean
	public FidoConfig fidoConfig() {
		return new FidoConfig();
	}

	@Bean
	public RequestHelper requestHelper() {
		return new RequestHelper();
	}

	@Bean
	public ResponseHelper responseHelper() {
		return new ResponseHelper();
	}
}
