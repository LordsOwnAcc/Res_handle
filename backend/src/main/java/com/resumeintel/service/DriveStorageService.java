package com.resumeintel.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

@Service
public class DriveStorageService {
    private final Drive drive; // null if GOOGLE_DRIVE_CREDENTIALS_BASE64 isn't configured

    @Value("${google.drive.folder-id:}")
    private String folderId;

    // ObjectProvider: the Drive bean only exists when credentials are configured.
    public DriveStorageService(ObjectProvider<Drive> driveProvider) {
        this.drive = driveProvider.getIfAvailable();
    }

    public boolean isConfigured() {
        return drive != null && folderId != null && !folderId.isBlank();
    }

    public record UploadResult(String fileId, String viewLink, String fileName) {}

    /** Uploads into the shared folder and makes the file viewable via link (not public-indexed). */
    public UploadResult upload(MultipartFile multipartFile) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException(
                "Google Drive isn't configured. Set GOOGLE_DRIVE_CREDENTIALS_BASE64 and GOOGLE_DRIVE_FOLDER_ID " +
                "(see README's \"Google Drive setup\" section) to enable file uploads."
            );
        }

        File metadata = new File();
        metadata.setName(multipartFile.getOriginalFilename());
        metadata.setParents(Collections.singletonList(folderId));

        ByteArrayContent content = new ByteArrayContent(multipartFile.getContentType(), multipartFile.getBytes());

        File uploaded = drive.files().create(metadata, content)
            .setFields("id, webViewLink, name")
            .execute();

        // "Anyone with the link can view" — matches what a resume-sharing link needs. Not
        // publicly indexed/searchable; still restricted to link holders.
        Permission permission = new Permission().setType("anyone").setRole("reader");
        drive.permissions().create(uploaded.getId(), permission).execute();

        return new UploadResult(uploaded.getId(), uploaded.getWebViewLink(), uploaded.getName());
    }
}
