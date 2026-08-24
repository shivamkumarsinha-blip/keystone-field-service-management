import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { workOrderApi } from '../api/workOrderApi';
import type { WorkOrderDto, WorkOrderStatus } from '../types/workOrder';
import { StatusBadge } from '../components/common/StatusBadge';
import { SlaBadge } from '../components/common/SlaBadge';
import { LoadingState } from '../components/common/LoadingState';
import { EmptyState } from '../components/common/EmptyState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';
import { NewWorkOrderForm } from '../components/workorders/NewWorkOrderForm';

const STATUSES: WorkOrderStatus[] = ['NEW', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CLOSED', 'CANCELLED'];

export function WorkOrdersPage() {
  const [items, setItems] = useState<WorkOrderDto[]>([]);
  const [status, setStatus] = useState<WorkOrderStatus | ''>('');
  const [q, setQ] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showNew, setShowNew] = useState(false);

  function load() {
    setLoading(true);
    workOrderApi.search({ status: status || undefined, q: q || undefined })
      .then((res) => setItems(res.content))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }

  useEffect(load, [status]);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>Work Orders</h2>
        <button className="btn btn-primary" onClick={() => setShowNew(true)}>+ New work order</button>
      </div>

      {showNew && (
        <div className="card" style={{ marginBottom: 16 }}>
          <NewWorkOrderForm onCreated={() => { setShowNew(false); load(); }} onCancel={() => setShowNew(false)} />
        </div>
      )}

      <div className="card" style={{ marginBottom: 16, display: 'flex', gap: 12 }}>
        <input placeholder="Search title or code…" value={q} onChange={(e) => setQ(e.target.value)}
               onKeyDown={(e) => e.key === 'Enter' && load()} style={{ flex: 1, padding: 8 }} />
        <select value={status} onChange={(e) => setStatus(e.target.value as WorkOrderStatus | '')}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
        <button className="btn" onClick={load}>Search</button>
      </div>

      <ErrorText message={error} />
      {loading ? <LoadingState /> : items.length === 0 ? <EmptyState label="No work orders match your filters" /> : (
        <div className="card">
          <table>
            <thead>
              <tr><th>Code</th><th>Title</th><th>Priority</th><th>Status</th><th>SLA</th><th>Technician</th><th>Site</th></tr>
            </thead>
            <tbody>
              {items.map((w) => (
                <tr key={w.id}>
                  <td><Link to={`/work-orders/${w.id}`}>{w.code}</Link></td>
                  <td>{w.title}</td>
                  <td>{w.priority}</td>
                  <td><StatusBadge status={w.status} /></td>
                  <td><SlaBadge state={w.slaState} /></td>
                  <td>{w.assignedTechnicianName ?? <span className="muted">Unassigned</span>}</td>
                  <td>{w.siteName}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
