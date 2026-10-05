package com.chubb.claims.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chubb.claims.dashboard.dto.ExposureResponse;
import com.chubb.claims.dashboard.dto.WorkloadResponse;
import com.chubb.claims.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/workload")
    public ResponseEntity<WorkloadResponse> getWorkload() {

        return ResponseEntity.ok(
                dashboardService.getWorkload()
        );
    }

    @GetMapping("/exposure")
    public ResponseEntity<ExposureResponse> getExposure() {

        return ResponseEntity.ok(
                dashboardService.getExposure()
        );
    }
}