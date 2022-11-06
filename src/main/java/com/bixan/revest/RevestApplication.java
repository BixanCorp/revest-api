package com.bixan.revest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bixan.revest.conf.CorsConfiguration;

@EnableAsync
@SpringBootApplication
public class RevestApplication {
	private static final Logger log = LoggerFactory.getLogger(RevestApplication.class);
	
	@Autowired
	CorsConfiguration corsConfig;

	
	public static void main(String[] args) {
		SpringApplication.run(RevestApplication.class, args);
	}

	@Component
    public class ProjectRunner implements ApplicationRunner {

        public ProjectRunner(ApplicationArguments args) {
        	/*
            boolean debug = args.containsOption("debug");
            List<String> files = args.getNonOptionArgs();
            if (debug) {
                System.out.println(files);
            }
            // if run with "--debug logfile.txt" prints ["logfile.txt"]
             */
        }

		@Override
		public void run(ApplicationArguments args) throws Exception {
			System.out.println("ApplicationRunner.run() called");
			//transfer.start();
		}
    }
    
    @Component
    public class ProjectCommandLineRunner implements CommandLineRunner {

        @Override
        public void run(String... args) {
            System.out.println("CommandLineRunner.run() called");
        }
    }
    
    @Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				String[] origins = corsConfig.getAllowedOrigins().toArray(new String[]{});
				
				log.info("Allowed origins: {}", String.join(",", origins));
				
				registry.addMapping("/**")
					//.allowedOrigins("https://domain2.com")
					.allowedOriginPatterns(origins)
					.allowedMethods("GET", "POST", "PATCH", "OPTIONS", "PUT")
					.allowedHeaders("*")
					//.exposedHeaders("header1", "header2")
					.allowCredentials(true)
					.maxAge(3600);
			}
		};
	}
}
