import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { workOrderApi } from '../api/workOrderApi';
import type { WorkOrderDto } from '../types/workOrder';
import { StatusBadge } from '../components/common/StatusBadge';
import { SlaBadge } from '../components/common/SlaBadge';
import { LoadingState } from '../components/common/LoadingState';
import { EmptyState } from '../components/common/EmptyState';
import { StatusActionBar } from '../components/workorders/StatusActionBar';

/**
 * Mobile-first "my jobs" view for technicians. The backend's /work-orders search endpoint
 * already forces technicianId = the caller for TECHNICIAN-role users, so this view can never
 * show another technician's jobs even if the frontend were tampered with.
 */
export function TechnicianJobsPage() {
  const [items, setItems] = useState<WorkOrderDto[]>([]);
  const [loading, setLoading] = useState(true);

  function load() {
    setLoading(true);
    workOrderApi.search({ size: 100 }).then((res) => setItems(res.content)).finally(() => setLoading(false));
  }

  useEffect(load, []);

  if (loading) return <LoadingState />;
  if (items.length === 0) return <EmptyState label="No jobs assigned to you right now" />;

  const open = items.filter((i) => i.status !== 'CLOSED' && i.status !== 'CANCELLED');

  return (
    <div>
      <h2>My Jobs</h2>
      {open.map((w) => (
        <div className="job-card" key={w.id}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <strong><Link to={`/work-orders/${w.id}`}>{w.code}</Link> — {w.title}</strong>
            <StatusBadge status={w.status} />
          </div>
          <p className="muted">{w.customerName} · {w.siteName} · Priority: {w.priority}</p>
          <SlaBadge state={w.slaState} />
          <StatusActionBar workOrder={w} onChanged={load} />
        </div>
      ))}
    </div>
  );
}
