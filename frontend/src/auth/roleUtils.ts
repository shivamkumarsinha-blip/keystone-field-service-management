import type { Role } from '../types/auth';

export function homeRouteFor(role: Role): string {
  switch (role) {
    case 'MANAGER':
      return '/dashboard';
    case 'DISPATCHER':
      return '/work-orders/board';
    case 'TECHNICIAN':
      return '/technician/jobs';
    case 'CUSTOMER':
      return '/customer-portal';
  }
}
