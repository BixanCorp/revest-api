package com.bixan.revest.auth.config;

import com.bixan.revest.config.EnvironmentConfig;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Autowired
    private EnvironmentConfig environmentConfig;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void initializeFirebase() {
        try {
            String firebaseConfigJson = environmentConfig.getFirebaseConfig();

            if (firebaseConfigJson == null || firebaseConfigJson.trim().isEmpty()) {
                log.warn("Firebase configuration JSON is not provided. Firebase authentication will not be available.");
                firebaseInitialized = false;
                return;
            }

            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = new ByteArrayInputStream(firebaseConfigJson.getBytes());

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                log.info("Firebase has been initialized successfully for profile: {}",
                        environmentConfig.getActiveProfile());
            } else {
                firebaseInitialized = true;
                log.info("Firebase app already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            firebaseInitialized = false;
            log.warn("Firebase will not be available due to initialization failure");
        }
    }

    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
    public FirebaseApp firebaseApp() {
        if (!firebaseInitialized || FirebaseApp.getApps().isEmpty()) {
            throw new RuntimeException("Firebase is not initialized. Please check your configuration.");
        }
        return FirebaseApp.getInstance();
    }

    public boolean isFirebaseEnabled() {
        return firebaseInitialized;
    }
}