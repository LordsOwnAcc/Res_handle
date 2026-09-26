package com.resumeintel.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document")
public class DocumentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    /** Object-storage key/URL (e.g. S3/R2/Render disk path) — binary bytes never live in Postgres. */
    @Column(name = "storage_path", nullable = false)
    private String storagePath;
    @Column(name = "size_bytes")
    private long sizeBytes;
    @Column(name = "imported_at")
    private Instant importedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public Instant getImportedAt() { return importedAt; }
    public void setImportedAt(Instant importedAt) { this.importedAt = importedAt; }
}
