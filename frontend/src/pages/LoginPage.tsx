import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { homeRouteFor } from '../auth/roleUtils';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('dispatcher@example.com');
  const [password, setPassword] = useState('Password123!');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      // We don't know the role until after login resolves, so re-read it from storage.
      const stored = localStorage.getItem('keystone_user');
      const role = stored ? JSON.parse(stored).role : 'DISPATCHER';
      navigate(homeRouteFor(role));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-screen">
      <form className="login-box" onSubmit={handleSubmit}>
        <h2 style={{ marginTop: 0 }}>KEYSTONE</h2>
        <p className="muted">Field Service Management Platform</p>

        <div className="form-field">
          <label>Email</label>
          <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
        </div>
        <div className="form-field">
          <label>Password</label>
          <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
        </div>

        <button className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
        <ErrorText message={error} />

        <p className="muted" style={{ marginTop: 16, fontSize: 12 }}>
          Demo accounts (see README): dispatcher@example.com · technician@example.com ·
          manager@example.com · customer@example.com — password: Password123!
        </p>
      </form>
    </div>
  );
}
