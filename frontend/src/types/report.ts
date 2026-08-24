export interface TechnicianLoad {
  technicianId: number;
  technicianName: string;
  openCount: number;
}

export interface SiteLoad {
  siteId: number;
  siteName: string;
  openCount: number;
}

export interface ReportSummaryDto {
  totalWorkOrders: number;
  countsByStatus: Record<string, number>;
  overdueCount: number;
  atRiskCount: number;
  highPriorityOpenCount: number;
  workByTechnician: TechnicianLoad[];
  workBySite: SiteLoad[];
}
