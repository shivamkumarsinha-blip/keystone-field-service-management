import { api } from './axios';
import type { CustomerDto } from '../types/customer';
import type { PageResponse } from '../types/page';

export const customerApi = {
  list: (search = '') =>
    api.get<PageResponse<CustomerDto>>('/customers', { params: { search, size: 50 } }).then((r) => r.data),
  create: (payload: { name: string; contactEmail?: string; contactPhone?: string }) =>
    api.post<CustomerDto>('/customers', payload).then((r) => r.data),
  /** For a CUSTOMER-role user: resolves their own organization server-side. */
  getMine: () => api.get<CustomerDto>('/customers/me').then((r) => r.data),
};
