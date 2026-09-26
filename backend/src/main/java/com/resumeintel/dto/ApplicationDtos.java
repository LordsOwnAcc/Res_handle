package com.resumeintel.dto;

import com.resumeintel.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class ApplicationDtos {
    public record CreateApplicationRequest(
        @NotBlank String companyName,
        @NotBlank String jobTitle,
        Long jdId,
        @NotNull Long resumeId,
        ApplicationStatus status
    ) {}

    public record UpdateStatusRequest(@NotNull ApplicationStatus status) {}

    public record ApplicationSummary(
        Long id, String companyName, String jobTitle, String resumeName,
        ApplicationStatus status, Instant dateApplied, Float matchScoreAtApplyTime
    ) {}
}
