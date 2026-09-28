package com.resumeintel.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

/**
 * Builds a Drive API client from a service account, not user OAuth — there's no login flow,
 * no token refresh to manage, and no per-user complexity. The service account uploads files
 * into a single Drive folder that you (the app owner) create and share with the service
 * account's email as Editor. The files land in *your* Drive, not the service account's own
 * storage. See README "Google Drive setup" for the one-time setup steps.
 *
 * If GOOGLE_DRIVE_CREDENTIALS_BASE64 isn't set, this bean is skipped entirely (see
 * DriveStorageService) — file upload becomes unavailable but the rest of the app (manual
 * resume entry, matching, tracker) works exactly as before. This is opt-in, not required.
 */
@Configuration
public class GoogleDriveConfig {

    @Value("${google.drive.credentials-base64:}")
    private String credentialsBase64;

    @Bean
    public Drive googleDriveClient() throws Exception {
        if (credentialsBase64 == null || credentialsBase64.isBlank()) {
            return null; // DriveStorageService checks for this and fails helpfully at call time
        }
        byte[] decoded = Base64.getDecoder().decode(credentialsBase64.trim());
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(new ByteArrayInputStream(decoded))
            .createScoped(List.of(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials)
        ).setApplicationName("ResumeIntel").build();
    }
}
