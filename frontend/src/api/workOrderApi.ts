import { api } from './axios';
import type { WorkOrderDto, WorkOrderStatusHistoryDto, WorkOrderStatus } from '../types/workOrder';
import type { PageResponse } from '../types/page';

export const workOrderApi = {
  search: (params: { status?: WorkOrderStatus; technicianId?: number; siteId?: number; q?: string; page?: number; size?: number }) =>
    api.get<PageResponse<WorkOrderDto>>('/work-orders', { params: { size: 50, ...params } }).then((r) => r.data),
  get: (id: number) => api.get<WorkOrderDto>(`/work-orders/${id}`).then((r) => r.data),
  history: (id: number) => api.get<WorkOrderStatusHistoryDto[]>(`/work-orders/${id}/history`).then((r) => r.data),
  create: (payload: { title: string; description?: string; priority: string; customerId: number; siteId: number }) =>
    api.post<WorkOrderDto>('/work-orders', payload).then((r) => r.data),
  assign: (id: number, technicianId: number) =>
    api.post<WorkOrderDto>(`/work-orders/${id}/assign`, { technicianId }).then((r) => r.data),
  changeStatus: (id: number, newStatus: WorkOrderStatus, note?: string) =>
    api.post<WorkOrderDto>(`/work-orders/${id}/status`, { newStatus, note }).then((r) => r.data),
  logParts: (id: number, partId: number, quantity: number) =>
    api.post<WorkOrderDto>(`/work-orders/${id}/parts`, { partId, quantity }).then((r) => r.data),
  logTime: (id: number, minutes: number, note?: string) =>
    api.post<WorkOrderDto>(`/work-orders/${id}/time`, { minutes, note }).then((r) => r.data),
};
