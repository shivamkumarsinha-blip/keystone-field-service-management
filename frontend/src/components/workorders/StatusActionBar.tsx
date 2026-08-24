import { useState } from 'react';
import type { WorkOrderDto, WorkOrderStatus } from '../../types/workOrder';
import { STATUS_FLOW } from '../../types/workOrder';
import { workOrderApi } from '../../api/workOrderApi';
import { ErrorText } from '../common/ErrorText';
import { extractErrorMessage } from '../../utils/errors';

const ACTION_LABELS: Partial<Record<WorkOrderStatus, string>> = {
  IN_PROGRESS: 'Start',
  ON_HOLD: 'Put on hold',
  COMPLETED: 'Mark complete',
  CLOSED: 'Close',
  CANCELLED: 'Cancel',
  ASSIGNED: 'Confirm assignment',
};

/**
 * Renders only the transitions the state machine allows from the work order's CURRENT status.
 * This is a UX convenience — the backend re-validates the transition and the actor's role/
 * ownership independently, so this bar being wrong or bypassed can never produce an illegal
 * status change.
 */
export function StatusActionBar({ workOrder, onChanged }: { workOrder: WorkOrderDto; onChanged: () => void }) {
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<WorkOrderStatus | null>(null);
  const nextOptions = STATUS_FLOW[workOrder.status] ?? [];

  async function act(next: WorkOrderStatus) {
    setBusy(next);
    setError(null);
    try {
      await workOrderApi.changeStatus(workOrder.id, next);
      onChanged();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  if (nextOptions.length === 0) return null;

  return (
    <div>
      <div className="job-actions">
        {nextOptions.map((next) => (
          <button
            key={next}
            className={`btn ${next === 'CANCELLED' ? 'btn-danger' : 'btn-primary'}`}
            disabled={busy !== null}
            onClick={() => act(next)}
          >
            {busy === next ? 'Working…' : (ACTION_LABELS[next] ?? next)}
          </button>
        ))}
      </div>
      <ErrorText message={error} />
    </div>
  );
}
