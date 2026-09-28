package com.resumeintel.controller;

import com.resumeintel.model.DocumentEntity;
import com.resumeintel.model.ResumeEntity;
import com.resumeintel.repository.DocumentRepository;
import com.resumeintel.repository.ResumeRepository;
import com.resumeintel.service.DriveStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DriveStorageService driveStorageService;
    private final DocumentRepository documentRepo;
    private final ResumeRepository resumeRepo;

    public DocumentController(DriveStorageService driveStorageService, DocumentRepository documentRepo, ResumeRepository resumeRepo) {
        this.driveStorageService = driveStorageService;
        this.documentRepo = documentRepo;
        this.resumeRepo = resumeRepo;
    }

    /**
     * Uploads the original resume file to the shared Google Drive folder and links it to a
     * resume. The file itself never touches Postgres — only its Drive file ID and view link
     * do (spec's storage layering: original file, extracted data, and AI analysis stay
     * separate — see ResumeEntity's Javadoc).
     */
    @PostMapping(value = "/upload/{resumeId}", consumes = "multipart/form-data")
    public Map<String, Object> upload(@PathVariable Long resumeId, @RequestParam("file") MultipartFile file) throws IOException {
        ResumeEntity resume = resumeRepo.findById(resumeId)
            .orElseThrow(() -> new EntityNotFoundException("Resume " + resumeId + " not found"));

        if (!driveStorageService.isConfigured()) {
            throw new IllegalStateException(
                "File upload isn't set up yet. This backend needs GOOGLE_DRIVE_CREDENTIALS_BASE64 " +
                "and GOOGLE_DRIVE_FOLDER_ID configured — see the README's \"Google Drive setup\" section."
            );
        }

        DriveStorageService.UploadResult uploaded = driveStorageService.upload(file);

        DocumentEntity doc = new DocumentEntity();
        doc.setFileName(uploaded.fileName());
        doc.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        doc.setStoragePath(uploaded.viewLink()); // Drive's webViewLink — the "storage path" for this backend
        doc.setSizeBytes(file.getSize());
        doc.setImportedAt(Instant.now());
        documentRepo.save(doc);

        resume.setDocumentId(doc.getId());
        resumeRepo.save(resume);

        return Map.of(
            "documentId", doc.getId(),
            "fileName", doc.getFileName(),
            "viewLink", doc.getStoragePath()
        );
    }

    @GetMapping("/{id}")
    public DocumentEntity get(@PathVariable Long id) {
        return documentRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Document " + id + " not found"));
    }
}
