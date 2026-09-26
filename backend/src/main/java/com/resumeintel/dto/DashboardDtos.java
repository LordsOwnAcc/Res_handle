package com.resumeintel.dto;

import java.util.List;

public class DashboardDtos {
    public record UsageRow(String resumeName, long count) {}

    public record DashboardStats(
        long totalApplications, long active, long interviews, long offers,
        long resumeCount, long jdCount, List<UsageRow> resumeUsage
    ) {}
}
