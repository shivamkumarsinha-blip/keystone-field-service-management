import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { workOrderApi } from '../api/workOrderApi';
import { partsApi } from '../api/partsApi';
import { userApi } from '../api/userApi';
import type { WorkOrderDto, WorkOrderStatusHistoryDto } from '../types/workOrder';
import type { PartDto } from '../types/part';
import type { UserDto } from '../types/auth';
import { useAuth } from '../auth/AuthContext';
import { StatusBadge } from '../components/common/StatusBadge';
import { SlaBadge } from '../components/common/SlaBadge';
import { LoadingState } from '../components/common/LoadingState';
import { ErrorText } from '../components/common/ErrorText';
import { StatusActionBar } from '../components/workorders/StatusActionBar';
import { extractErrorMessage } from '../utils/errors';

export function WorkOrderDetailsPage() {
  const { id } = useParams();
  const workOrderId = Number(id);
  const { user } = useAuth();

  const [workOrder, setWorkOrder] = useState<WorkOrderDto | null>(null);
  const [history, setHistory] = useState<WorkOrderStatusHistoryDto[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [parts, setParts] = useState<PartDto[]>([]);
  const [selectedPart, setSelectedPart] = useState<number | ''>('');
  const [partQty, setPartQty] = useState(1);
  const [minutes, setMinutes] = useState(30);
  const [timeNote, setTimeNote] = useState('');

  const [technicians, setTechnicians] = useState<UserDto[]>([]);
  const [assignTechId, setAssignTechId] = useState<number | ''>('');

  function reload() {
    workOrderApi.get(workOrderId).then(setWorkOrder).catch((e) => setError(extractErrorMessage(e)));
    workOrderApi.history(workOrderId).then(setHistory).catch(() => {});
  }

  useEffect(reload, [workOrderId]);

  useEffect(() => {
    if (user?.role === 'TECHNICIAN') {
      partsApi.list().then((res) => setParts(res.content)).catch(() => {});
    }
    if (user?.role === 'DISPATCHER' || user?.role === 'MANAGER') {
      userApi.list().then((res) => setTechnicians(res.content.filter((u) => u.role === 'TECHNICIAN'))).catch(() => {});
    }
  }, [user]);

  if (error) return <ErrorText message={error} />;
  if (!workOrder) return <LoadingState />;

  const isOwningTechnician = user?.role === 'TECHNICIAN' && workOrder.assignedTechnicianId === user.id;
  const canDispatch = user?.role === 'DISPATCHER' || user?.role === 'MANAGER';

  async function logParts() {
    if (!selectedPart) return;
    try {
      await workOrderApi.logParts(workOrderId, selectedPart, partQty);
      reload();
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  }

  async function logTime() {
    try {
      await workOrderApi.logTime(workOrderId, minutes, timeNote);
      setTimeNote('');
      reload();
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  }

  async function assign() {
    if (!assignTechId) return;
    try {
      await workOrderApi.assign(workOrderId, assignTechId);
      reload();
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h2 style={{ marginBottom: 4 }}>{workOrder.code} — {workOrder.title}</h2>
          <p className="muted">{workOrder.customerName} · {workOrder.siteName}</p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <StatusBadge status={workOrder.status} /> <SlaBadge state={workOrder.slaState} />
        </div>
      </div>

      <div className="card" style={{ marginBottom: 16 }}>
        <p>{workOrder.description || <span className="muted">No description provided.</span>}</p>
        <p className="muted">Priority: {workOrder.priority} · SLA due: {workOrder.slaDueAt ? new Date(workOrder.slaDueAt).toLocaleString() : '—'}</p>
        <p className="muted">Assigned to: {workOrder.assignedTechnicianName ?? 'Unassigned'}</p>

        {(isOwningTechnician || user?.role === 'MANAGER') && (
          <StatusActionBar workOrder={workOrder} onChanged={reload} />
        )}
      </div>

      {canDispatch && (
        <div className="card" style={{ marginBottom: 16 }}>
          <h3 style={{ marginTop: 0 }}>Dispatch</h3>
          <div style={{ display: 'flex', gap: 8 }}>
            <select value={assignTechId} onChange={(e) => setAssignTechId(Number(e.target.value) || '')}>
              <option value="">Select technician…</option>
              {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
            </select>
            <button className="btn btn-primary" onClick={assign}>Assign / Reassign</button>
          </div>
        </div>
      )}

      {isOwningTechnician && (
        <div className="card-grid" style={{ marginBottom: 16 }}>
          <div className="card">
            <h3 style={{ marginTop: 0 }}>Log parts used</h3>
            <div className="form-field">
              <label>Part</label>
              <select value={selectedPart} onChange={(e) => setSelectedPart(Number(e.target.value) || '')}>
                <option value="">Select part…</option>
                {parts.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.quantityInStock} in stock)</option>)}
              </select>
            </div>
            <div className="form-field">
              <label>Quantity</label>
              <input type="number" min={1} value={partQty} onChange={(e) => setPartQty(Number(e.target.value))} />
            </div>
            <button className="btn btn-primary" onClick={logParts}>Log part usage</button>
          </div>

          <div className="card">
            <h3 style={{ marginTop: 0 }}>Log time</h3>
            <div className="form-field">
              <label>Minutes</label>
              <input type="number" min={1} value={minutes} onChange={(e) => setMinutes(Number(e.target.value))} />
            </div>
            <div className="form-field">
              <label>Note</label>
              <input value={timeNote} onChange={(e) => setTimeNote(e.target.value)} />
            </div>
            <button className="btn btn-primary" onClick={logTime}>Log time</button>
          </div>
        </div>
      )}

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Status history</h3>
        <table>
          <thead><tr><th>When</th><th>From</th><th>To</th><th>By</th><th>Note</th></tr></thead>
          <tbody>
            {history.map((h) => (
              <tr key={h.id}>
                <td>{new Date(h.changedAt).toLocaleString()}</td>
                <td>{h.previousStatus ?? '—'}</td>
                <td>{h.newStatus}</td>
                <td>{h.changedByName}</td>
                <td>{h.note ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
