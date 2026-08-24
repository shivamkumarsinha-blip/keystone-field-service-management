import { useEffect, useState, type FormEvent } from 'react';
import { userApi } from '../api/userApi';
import type { UserDto, Role } from '../types/auth';
import { LoadingState } from '../components/common/LoadingState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

const ROLES: Role[] = ['DISPATCHER', 'TECHNICIAN', 'MANAGER', 'CUSTOMER'];

export function UsersPage() {
  const [items, setItems] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showNew, setShowNew] = useState(false);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<Role>('TECHNICIAN');
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    userApi.list().then((res) => setItems(res.content)).catch((e) => setError(extractErrorMessage(e))).finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await userApi.create({ fullName, email, password, role });
      setFullName(''); setEmail(''); setPassword('');
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
        <h2 style={{ margin: 0 }}>Users</h2>
        <button className="btn btn-primary" onClick={() => setShowNew((v) => !v)}>
          {showNew ? 'Close' : '+ New user'}
        </button>
      </div>

      {showNew && (
        <form className="card" style={{ marginBottom: 16 }} onSubmit={handleCreate}>
          <div className="form-field"><label>Full name</label><input value={fullName} onChange={(e) => setFullName(e.target.value)} required /></div>
          <div className="form-field"><label>Email</label><input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></div>
          <div className="form-field"><label>Temporary password</label><input type="password" minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} required /></div>
          <div className="form-field">
            <label>Role</label>
            <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
              {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          <button className="btn btn-primary" disabled={submitting}>{submitting ? 'Creating…' : 'Create user'}</button>
        </form>
      )}

      <ErrorText message={error} />
      {loading ? <LoadingState /> : (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Active</th></tr></thead>
            <tbody>
              {items.map((u) => (
                <tr key={u.id}><td>{u.fullName}</td><td>{u.email}</td><td>{u.role}</td><td>{u.active ? 'Yes' : 'No'}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
