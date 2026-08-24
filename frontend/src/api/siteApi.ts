import { api } from './axios';
import type { SiteDto } from '../types/site';
import type { PageResponse } from '../types/page';

export const siteApi = {
  listByCustomer: (customerId: number) =>
    api.get<PageResponse<SiteDto>>(`/customers/${customerId}/sites`, { params: { size: 100 } }).then((r) => r.data),
  create: (customerId: number, payload: { name: string; addressLine: string; city?: string; state?: string; postalCode?: string }) =>
    api.post<SiteDto>(`/customers/${customerId}/sites`, payload).then((r) => r.data),
  /** For a CUSTOMER-role user: their own organization's sites, resolved server-side. */
  listMine: () =>
    api.get<PageResponse<SiteDto>>('/customers/me/sites', { params: { size: 100 } }).then((r) => r.data),
};
