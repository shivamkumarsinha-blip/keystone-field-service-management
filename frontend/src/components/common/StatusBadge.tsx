import type { WorkOrderStatus } from '../../types/workOrder';

export function StatusBadge({ status }: { status: WorkOrderStatus }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{status.replace('_', ' ')}</span>;
}
