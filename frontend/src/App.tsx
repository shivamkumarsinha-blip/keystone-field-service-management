import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { useAuth } from './auth/AuthContext';
import { homeRouteFor } from './auth/roleUtils';

import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { CustomersPage } from './pages/CustomersPage';
import { SitesPage } from './pages/SitesPage';
import { WorkOrdersPage } from './pages/WorkOrdersPage';
import { WorkOrderDetailsPage } from './pages/WorkOrderDetailsPage';
import { WorkOrderBoardPage } from './pages/WorkOrderBoardPage';
import { TechnicianJobsPage } from './pages/TechnicianJobsPage';
import { CustomerPortalPage } from './pages/CustomerPortalPage';
import { PartsPage } from './pages/PartsPage';
import { UsersPage } from './pages/UsersPage';
import { NotFoundPage } from './pages/NotFoundPage';

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={homeRouteFor(user.role)} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route path="/" element={<HomeRedirect />} />

        <Route path="/dashboard" element={
          <ProtectedRoute allowedRoles={['MANAGER']}><DashboardPage /></ProtectedRoute>
        } />

        <Route path="/customers" element={
          <ProtectedRoute allowedRoles={['DISPATCHER', 'MANAGER']}><CustomersPage /></ProtectedRoute>
        } />
        <Route path="/customers/:customerId/sites" element={
          <ProtectedRoute allowedRoles={['DISPATCHER', 'MANAGER']}><SitesPage /></ProtectedRoute>
        } />

        <Route path="/work-orders" element={
          <ProtectedRoute allowedRoles={['DISPATCHER', 'MANAGER']}><WorkOrdersPage /></ProtectedRoute>
        } />
        <Route path="/work-orders/board" element={
          <ProtectedRoute allowedRoles={['DISPATCHER', 'MANAGER']}><WorkOrderBoardPage /></ProtectedRoute>
        } />
        <Route path="/work-orders/:id" element={
          <ProtectedRoute allowedRoles={['DISPATCHER', 'MANAGER', 'TECHNICIAN']}><WorkOrderDetailsPage /></ProtectedRoute>
        } />

        <Route path="/technician/jobs" element={
          <ProtectedRoute allowedRoles={['TECHNICIAN']}><TechnicianJobsPage /></ProtectedRoute>
        } />

        <Route path="/customer-portal" element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}><CustomerPortalPage /></ProtectedRoute>
        } />

        <Route path="/parts" element={
          <ProtectedRoute allowedRoles={['MANAGER', 'DISPATCHER']}><PartsPage /></ProtectedRoute>
        } />
        <Route path="/users" element={
          <ProtectedRoute allowedRoles={['MANAGER']}><UsersPage /></ProtectedRoute>
        } />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
