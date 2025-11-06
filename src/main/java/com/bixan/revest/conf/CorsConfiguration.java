package com.bixan.revest.conf;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class CorsConfiguration {
	private static final Logger log = LoggerFactory.getLogger(CorsConfiguration.class);
	
	@Value("${cors.allowed.origin}")
	private String allowedOrigin;

	@PostConstruct
	private void init() {
		log.info("Initializing CorsConfiguration class");
		log.info("CORS allowed origin value: '{}'", allowedOrigin);
	}

	/**
	 * @return the allowedOrigins as a single item list
	 */
	public List<String> getAllowedOrigins() {
		if (allowedOrigin == null || allowedOrigin.trim().isEmpty()) {
			log.warn("CORS allowedOrigin is null or empty, using default localhost:3000");
			return Arrays.asList("http://localhost:3000");
		}
		return Arrays.asList(allowedOrigin);
	}

	/**
	 * @return the allowedOrigin
	 */
	public String getAllowedOrigin() {
		return allowedOrigin;
	}

	/**
	 * @param allowedOrigin the allowedOrigin to set
	 */
	public void setAllowedOrigin(String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}

	

}
