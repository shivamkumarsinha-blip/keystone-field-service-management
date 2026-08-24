import { api } from './axios';
import type { UserDto, Role } from '../types/auth';
import type { PageResponse } from '../types/page';

export const userApi = {
  list: () => api.get<PageResponse<UserDto>>('/users', { params: { size: 100 } }).then((r) => r.data),
  create: (payload: { fullName: string; email: string; password: string; role: Role }) =>
    api.post<UserDto>('/users', payload).then((r) => r.data),
};
