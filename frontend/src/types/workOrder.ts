export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type WorkOrderStatus =
  | 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CLOSED' | 'CANCELLED';
export type SlaState = 'OK' | 'AT_RISK' | 'BREACHED';

export interface WorkOrderDto {
  id: number;
  code: string;
  title: string;
  description?: string;
  priority: Priority;
  status: WorkOrderStatus;
  customerId: number;
  customerName: string;
  siteId: number;
  siteName: string;
  assignedTechnicianId?: number;
  assignedTechnicianName?: string;
  slaDueAt?: string;
  slaState: SlaState;
  createdAt: string;
  updatedAt: string;
  startedAt?: string;
  completedAt?: string;
  closedAt?: string;
}

export interface WorkOrderStatusHistoryDto {
  id: number;
  previousStatus?: WorkOrderStatus;
  newStatus: WorkOrderStatus;
  changedByName: string;
  note?: string;
  changedAt: string;
}

export const STATUS_FLOW: Record<WorkOrderStatus, WorkOrderStatus[]> = {
  NEW: ['ASSIGNED', 'CANCELLED'],
  ASSIGNED: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['ON_HOLD', 'COMPLETED'],
  ON_HOLD: ['IN_PROGRESS'],
  COMPLETED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: [],
};
