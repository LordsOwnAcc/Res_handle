package com.resumeintel.controller;

import com.resumeintel.dto.DashboardDtos.DashboardStats;
import com.resumeintel.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping
    public DashboardStats stats() { return dashboardService.getStats(); }
}
