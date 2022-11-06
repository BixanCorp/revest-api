package com.bixan.revest.conf;

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
	private List<String> allowedOrigins;

	@PostConstruct
	private void init() {
		log.info("Initializing CorsConfiguration class");
	}

	/**
	 * @return the allowedOrigins
	 */
	public List<String> getAllowedOrigins() {
		return allowedOrigins;
	}

	/**
	 * @param allowedOrigins the allowedOrigins to set
	 */
	public void setAllowedOrigins(List<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	

}
