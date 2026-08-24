import { useEffect, useState } from 'react';
import { reportApi } from '../api/reportApi';
import type { ReportSummaryDto } from '../types/report';
import { LoadingState } from '../components/common/LoadingState';
import { ErrorText } from '../components/common/ErrorText';
import { extractErrorMessage } from '../utils/errors';

export function DashboardPage() {
  const [summary, setSummary] = useState<ReportSummaryDto | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    reportApi.summary().then(setSummary).catch((e) => setError(extractErrorMessage(e)));
  }, []);

  if (error) return <ErrorText message={error} />;
  if (!summary) return <LoadingState />;

  return (
    <div>
      <h2>Manager Dashboard</h2>

      <div className="card-grid">
        <div className="card stat-card">
          <div className="label">Total work orders</div>
          <div className="value">{summary.totalWorkOrders}</div>
        </div>
        <div className="card stat-card">
          <div className="label">Overdue (SLA breached)</div>
          <div className="value" style={{ color: '#d92d20' }}>{summary.overdueCount}</div>
        </div>
        <div className="card stat-card">
          <div className="label">At risk</div>
          <div className="value" style={{ color: '#b54708' }}>{summary.atRiskCount}</div>
        </div>
        <div className="card stat-card">
          <div className="label">High/urgent open</div>
          <div className="value">{summary.highPriorityOpenCount}</div>
        </div>
      </div>

      <div className="card-grid">
        {Object.entries(summary.countsByStatus).map(([status, count]) => (
          <div className="card stat-card" key={status}>
            <div className="label">{status.replace('_', ' ')}</div>
            <div className="value">{count}</div>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div className="card">
          <h3 style={{ marginTop: 0 }}>Open work by technician</h3>
          <table>
            <thead><tr><th>Technician</th><th>Open jobs</th></tr></thead>
            <tbody>
              {summary.workByTechnician.map((t) => (
                <tr key={t.technicianId}><td>{t.technicianName}</td><td>{t.openCount}</td></tr>
              ))}
              {summary.workByTechnician.length === 0 && <tr><td colSpan={2} className="muted">No assigned open work</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="card">
          <h3 style={{ marginTop: 0 }}>Open work by site</h3>
          <table>
            <thead><tr><th>Site</th><th>Open jobs</th></tr></thead>
            <tbody>
              {summary.workBySite.map((s) => (
                <tr key={s.siteId}><td>{s.siteName}</td><td>{s.openCount}</td></tr>
              ))}
              {summary.workBySite.length === 0 && <tr><td colSpan={2} className="muted">No open work</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
