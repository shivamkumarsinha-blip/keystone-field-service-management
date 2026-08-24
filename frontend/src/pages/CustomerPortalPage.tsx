import { useEffect, useState } from 'react';
import { workOrderApi } from '../api/workOrderApi';
import { siteApi } from '../api/siteApi';
import { customerApi } from '../api/customerApi';
import type { WorkOrderDto, Priority } from '../types/workOrder';
import { StatusBadge } from '../components/common/StatusBadge';
import { LoadingState } from '../components/common/LoadingState';
import { EmptyState } from '../components/common/EmptyState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

/**
 * Simple customer-facing portal. The backend independently scopes every query here to the
 * caller's own organization (via GET /api/customers/me, resolved from the JWT — never from a
 * client-supplied id) — a customer can never see another organization's work orders by editing
 * an id in the request.
 */
export function CustomerPortalPage() {
  const [items, setItems] = useState<WorkOrderDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showNew, setShowNew] = useState(false);

  function load() {
    setLoading(true);
    workOrderApi.search({ size: 100 })
      .then((res) => setItems(res.content))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>My Requests</h2>
        <button className="btn btn-primary" onClick={() => setShowNew((v) => !v)}>
          {showNew ? 'Close' : '+ Raise a request'}
        </button>
      </div>

      {showNew && <RaiseRequestForm onCreated={() => { setShowNew(false); load(); }} />}

      <ErrorText message={error} />
      {loading ? <LoadingState /> : items.length === 0 ? <EmptyState label="You haven't raised any requests yet" /> : (
        <div className="card">
          <table>
            <thead><tr><th>Code</th><th>Title</th><th>Site</th><th>Status</th><th>Raised</th></tr></thead>
            <tbody>
              {items.map((w) => (
                <tr key={w.id}>
                  <td>{w.code}</td>
                  <td>{w.title}</td>
                  <td>{w.siteName}</td>
                  <td><StatusBadge status={w.status} /></td>
                  <td>{new Date(w.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function RaiseRequestForm({ onCreated }: { onCreated: () => void }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [siteId, setSiteId] = useState<number | ''>('');
  const [customerId, setCustomerId] = useState<number | null>(null);
  const [sites, setSites] = useState<{ id: number; name: string }[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    customerApi.getMine()
      .then((c) => setCustomerId(c.id))
      .catch((e) => setError(extractErrorMessage(e)));
    siteApi.listMine()
      .then((res) => setSites(res.content.map((s) => ({ id: s.id, name: s.name }))))
      .catch((e) => setError(extractErrorMessage(e)));
  }, []);

  async function submit() {
    if (!customerId || !siteId) {
      setError('Please select a site');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await workOrderApi.create({ title, description, priority, customerId, siteId });
      onCreated();
    } catch (e) {
      setError(extractErrorMessage(e));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="card" style={{ marginBottom: 16 }}>
      <div className="form-field"><label>Title</label><input value={title} onChange={(e) => setTitle(e.target.value)} /></div>
      <div className="form-field"><label>Description</label><textarea value={description} onChange={(e) => setDescription(e.target.value)} /></div>
      <div className="form-field">
        <label>Priority</label>
        <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
          {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
        </select>
      </div>
      <div className="form-field">
        <label>Site</label>
        <select value={siteId} onChange={(e) => setSiteId(Number(e.target.value) || '')}>
          <option value="">Select site…</option>
          {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
      </div>
      <ErrorText message={error} />
      <button className="btn btn-primary" disabled={submitting} onClick={submit}>
        {submitting ? 'Submitting…' : 'Submit request'}
      </button>
    </div>
  );
}
