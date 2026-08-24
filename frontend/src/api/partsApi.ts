import { api } from './axios';
import type { PartDto } from '../types/part';
import type { PageResponse } from '../types/page';

export const partsApi = {
  list: (search = '') =>
    api.get<PageResponse<PartDto>>('/parts', { params: { search, size: 100 } }).then((r) => r.data),
  create: (payload: { name: string; sku: string; quantityInStock: number; unitCost: number }) =>
    api.post<PartDto>('/parts', payload).then((r) => r.data),
};
