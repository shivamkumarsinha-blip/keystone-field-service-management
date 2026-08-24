import type { SlaState } from '../../types/workOrder';

export function SlaBadge({ state }: { state: SlaState }) {
  return <span className={`badge badge-${state.toLowerCase()}`}>{state.replace('_', ' ')}</span>;
}
