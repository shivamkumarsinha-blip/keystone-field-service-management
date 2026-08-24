import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { workOrderApi } from '../api/workOrderApi';
import type { WorkOrderDto, WorkOrderStatus } from '../types/workOrder';
import { SlaBadge } from '../components/common/SlaBadge';
import { LoadingState } from '../components/common/LoadingState';

const COLUMNS: WorkOrderStatus[] = ['NEW', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CLOSED'];

/** A simple read-only Kanban view of open work, grouped by status. */
export function WorkOrderBoardPage() {
  const [items, setItems] = useState<WorkOrderDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    workOrderApi.search({ size: 200 }).then((res) => setItems(res.content)).finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingState />;

  return (
    <div>
      <h2>Work Order Board</h2>
      <div className="kanban">
        {COLUMNS.map((status) => {
          const columnItems = items.filter((i) => i.status === status);
          return (
            <div className="kanban-column" key={status}>
              <h3>{status.replace('_', ' ')} ({columnItems.length})</h3>
              {columnItems.map((w) => (
                <Link to={`/work-orders/${w.id}`} key={w.id} style={{ color: 'inherit' }}>
                  <div className="kanban-card">
                    <strong>{w.code}</strong>
                    <div>{w.title}</div>
                    <div className="muted">{w.assignedTechnicianName ?? 'Unassigned'}</div>
                    <SlaBadge state={w.slaState} />
                  </div>
                </Link>
              ))}
              {columnItems.length === 0 && <div className="muted" style={{ fontSize: 12 }}>Empty</div>}
            </div>
          );
        })}
      </div>
    </div>
  );
}
