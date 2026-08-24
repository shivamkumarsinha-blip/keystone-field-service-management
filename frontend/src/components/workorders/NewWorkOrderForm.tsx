import { useEffect, useState, type FormEvent } from 'react';
import { customerApi } from '../../api/customerApi';
import { siteApi } from '../../api/siteApi';
import { workOrderApi } from '../../api/workOrderApi';
import type { CustomerDto } from '../../types/customer';
import type { SiteDto } from '../../types/site';
import type { Priority } from '../../types/workOrder';
import { ErrorText } from '../common/ErrorText';
import { extractErrorMessage } from '../../utils/errors';

const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export function NewWorkOrderForm({ onCreated, onCancel }: { onCreated: () => void; onCancel: () => void }) {
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [sites, setSites] = useState<SiteDto[]>([]);
  const [customerId, setCustomerId] = useState<number | ''>('');
  const [siteId, setSiteId] = useState<number | ''>('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    customerApi.list().then((res) => setCustomers(res.content)).catch((e) => setError(extractErrorMessage(e)));
  }, []);

  useEffect(() => {
    if (!customerId) { setSites([]); setSiteId(''); return; }
    siteApi.listByCustomer(customerId).then((res) => setSites(res.content)).catch((e) => setError(extractErrorMessage(e)));
  }, [customerId]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!customerId || !siteId) {
      setError('Please select a customer and a site');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await workOrderApi.create({ title, description, priority, customerId, siteId });
      onCreated();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3 style={{ marginTop: 0 }}>Raise a new work order</h3>

      <div className="form-field">
        <label>Title</label>
        <input value={title} onChange={(e) => setTitle(e.target.value)} required />
      </div>

      <div className="form-field">
        <label>Description</label>
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
      </div>

      <div style={{ display: 'flex', gap: 12 }}>
        <div className="form-field" style={{ flex: 1 }}>
          <label>Priority</label>
          <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
            {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
        </div>
        <div className="form-field" style={{ flex: 1 }}>
          <label>Customer</label>
          <select value={customerId} onChange={(e) => setCustomerId(Number(e.target.value) || '')} required>
            <option value="">Select customer…</option>
            {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="form-field" style={{ flex: 1 }}>
          <label>Site</label>
          <select value={siteId} onChange={(e) => setSiteId(Number(e.target.value) || '')} required disabled={!customerId}>
            <option value="">Select site…</option>
            {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>
      </div>

      <ErrorText message={error} />

      <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
        <button className="btn btn-primary" disabled={submitting}>{submitting ? 'Creating…' : 'Create work order'}</button>
        <button type="button" className="btn" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}
