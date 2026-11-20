package com.sip.hsm.controllers;

import com.sip.hsm.dto.DashboardStats;
import com.sip.hsm.services.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /api/dashboard/stats
    @GetMapping("/stats")
    public DashboardStats getStats() {
        return dashboardService.getStats();
    }
}
