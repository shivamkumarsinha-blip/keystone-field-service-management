import { api } from './axios';
import type { ReportSummaryDto } from '../types/report';

export const reportApi = {
  summary: () => api.get<ReportSummaryDto>('/reports/summary').then((r) => r.data),
};
