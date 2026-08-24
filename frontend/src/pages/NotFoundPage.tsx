import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="empty-state">
      <h2>404</h2>
      <p>That page doesn't exist.</p>
      <Link to="/">Go home</Link>
    </div>
  );
}
