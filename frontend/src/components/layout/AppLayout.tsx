import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';

const NAV_BY_ROLE: Record<string, { to: string; label: string }[]> = {
  MANAGER: [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/work-orders', label: 'Work Orders' },
    { to: '/work-orders/board', label: 'Board' },
    { to: '/customers', label: 'Customers' },
    { to: '/parts', label: 'Parts' },
    { to: '/users', label: 'Users' },
  ],
  DISPATCHER: [
    { to: '/work-orders', label: 'Work Orders' },
    { to: '/work-orders/board', label: 'Board' },
    { to: '/customers', label: 'Customers' },
  ],
  TECHNICIAN: [
    { to: '/technician/jobs', label: 'My Jobs' },
  ],
  CUSTOMER: [
    { to: '/customer-portal', label: 'My Requests' },
  ],
};

export function AppLayout() {
  const { user, logout } = useAuth();
  const items = user ? NAV_BY_ROLE[user.role] : [];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>KEYSTONE</h1>
        <nav>
          {items.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => (isActive ? 'active' : '')}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="main-area">
        <header className="topbar">
          <div className="muted">Field Service Management</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span>{user?.fullName} <span className="muted">({user?.role})</span></span>
            <button className="btn" onClick={logout}>Log out</button>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
