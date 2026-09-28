package com.resumeintel.controller;

import com.resumeintel.dto.ParsedResumeDraft;
import com.resumeintel.service.ResumeFileParser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
public class ResumeImportController {
    private final ResumeFileParser parser;
    public ResumeImportController(ResumeFileParser parser) { this.parser = parser; }

    /**
     * Reads an uploaded resume and returns a DRAFT for the user to review. Nothing is saved
     * here, and the file/text is discarded after the response (never stored or logged).
     */
    @PostMapping(value = "/parse", consumes = "multipart/form-data")
    public ParsedResumeDraft parse(@RequestParam("file") MultipartFile file) throws IOException {
        return parser.parse(parser.extractText(file));
    }
}
