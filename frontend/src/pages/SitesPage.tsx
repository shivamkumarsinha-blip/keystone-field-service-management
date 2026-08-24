import { useEffect, useState, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { siteApi } from '../api/siteApi';
import type { SiteDto } from '../types/site';
import { LoadingState } from '../components/common/LoadingState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

export function SitesPage() {
  const { customerId } = useParams();
  const custId = Number(customerId);

  const [items, setItems] = useState<SiteDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState('');
  const [addressLine, setAddressLine] = useState('');
  const [city, setCity] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    siteApi.listByCustomer(custId).then((res) => setItems(res.content)).catch((e) => setError(extractErrorMessage(e))).finally(() => setLoading(false));
  }

  useEffect(load, [custId]);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await siteApi.create(custId, { name, addressLine, city: city || undefined });
      setName(''); setAddressLine(''); setCity('');
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h2>Sites</h2>

      <form className="card" style={{ marginBottom: 16 }} onSubmit={handleCreate}>
        <h3 style={{ marginTop: 0 }}>Add a site</h3>
        <div className="form-field"><label>Name</label><input value={name} onChange={(e) => setName(e.target.value)} required /></div>
        <div className="form-field"><label>Address</label><input value={addressLine} onChange={(e) => setAddressLine(e.target.value)} required /></div>
        <div className="form-field"><label>City</label><input value={city} onChange={(e) => setCity(e.target.value)} /></div>
        <button className="btn btn-primary" disabled={submitting}>{submitting ? 'Adding…' : 'Add site'}</button>
      </form>

      <ErrorText message={error} />
      {loading ? <LoadingState /> : (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>Address</th><th>City</th></tr></thead>
            <tbody>
              {items.map((s) => (
                <tr key={s.id}><td>{s.name}</td><td>{s.addressLine}</td><td>{s.city ?? '—'}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
