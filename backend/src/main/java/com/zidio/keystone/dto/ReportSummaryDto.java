package com.zidio.keystone.dto;

import java.util.List;
import java.util.Map;

public record ReportSummaryDto(
        long totalWorkOrders,
        Map<String, Long> countsByStatus,
        long overdueCount,
        long atRiskCount,
        long highPriorityOpenCount,
        List<TechnicianLoadDto> workByTechnician,
        List<SiteLoadDto> workBySite
) {
    public record TechnicianLoadDto(Long technicianId, String technicianName, long openCount) {}
    public record SiteLoadDto(Long siteId, String siteName, long openCount) {}
}
