import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { customerApi } from '../api/customerApi';
import type { CustomerDto } from '../types/customer';
import { LoadingState } from '../components/common/LoadingState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

export function CustomersPage() {
  const [items, setItems] = useState<CustomerDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showNew, setShowNew] = useState(false);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    customerApi.list().then((res) => setItems(res.content)).catch((e) => setError(extractErrorMessage(e))).finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await customerApi.create({ name, contactEmail: email || undefined, contactPhone: phone || undefined });
      setName(''); setEmail(''); setPhone('');
      setShowNew(false);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>Customers</h2>
        <button className="btn btn-primary" onClick={() => setShowNew((v) => !v)}>
          {showNew ? 'Close' : '+ New customer'}
        </button>
      </div>

      {showNew && (
        <form className="card" style={{ marginBottom: 16 }} onSubmit={handleCreate}>
          <div className="form-field"><label>Name</label><input value={name} onChange={(e) => setName(e.target.value)} required /></div>
          <div className="form-field"><label>Contact email</label><input type="email" value={email} onChange={(e) => setEmail(e.target.value)} /></div>
          <div className="form-field"><label>Contact phone</label><input value={phone} onChange={(e) => setPhone(e.target.value)} /></div>
          <button className="btn btn-primary" disabled={submitting}>{submitting ? 'Creating…' : 'Create customer'}</button>
        </form>
      )}

      <ErrorText message={error} />
      {loading ? <LoadingState /> : (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>Contact</th><th>Sites</th></tr></thead>
            <tbody>
              {items.map((c) => (
                <tr key={c.id}>
                  <td>{c.name}</td>
                  <td>{c.contactEmail ?? '—'} {c.contactPhone ? `· ${c.contactPhone}` : ''}</td>
                  <td><Link to={`/customers/${c.id}/sites`}>Manage sites</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
