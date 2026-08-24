import { useEffect, useState, type FormEvent } from 'react';
import { partsApi } from '../api/partsApi';
import type { PartDto } from '../types/part';
import { LoadingState } from '../components/common/LoadingState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

export function PartsPage() {
  const [items, setItems] = useState<PartDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showNew, setShowNew] = useState(false);
  const [name, setName] = useState('');
  const [sku, setSku] = useState('');
  const [qty, setQty] = useState(0);
  const [cost, setCost] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    partsApi.list().then((res) => setItems(res.content)).catch((e) => setError(extractErrorMessage(e))).finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await partsApi.create({ name, sku, quantityInStock: qty, unitCost: cost });
      setName(''); setSku(''); setQty(0); setCost(0);
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
        <h2 style={{ margin: 0 }}>Parts Inventory</h2>
        <button className="btn btn-primary" onClick={() => setShowNew((v) => !v)}>
          {showNew ? 'Close' : '+ New part'}
        </button>
      </div>

      {showNew && (
        <form className="card" style={{ marginBottom: 16 }} onSubmit={handleCreate}>
          <div className="form-field"><label>Name</label><input value={name} onChange={(e) => setName(e.target.value)} required /></div>
          <div className="form-field"><label>SKU</label><input value={sku} onChange={(e) => setSku(e.target.value)} required /></div>
          <div className="form-field"><label>Starting stock</label><input type="number" min={0} value={qty} onChange={(e) => setQty(Number(e.target.value))} /></div>
          <div className="form-field"><label>Unit cost</label><input type="number" min={0} step="0.01" value={cost} onChange={(e) => setCost(Number(e.target.value))} /></div>
          <button className="btn btn-primary" disabled={submitting}>{submitting ? 'Creating…' : 'Create part'}</button>
        </form>
      )}

      <ErrorText message={error} />
      {loading ? <LoadingState /> : (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>SKU</th><th>In stock</th><th>Unit cost</th></tr></thead>
            <tbody>
              {items.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.sku}</td>
                  <td style={{ color: p.quantityInStock < 5 ? '#d92d20' : undefined }}>{p.quantityInStock}</td>
                  <td>${p.unitCost.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
