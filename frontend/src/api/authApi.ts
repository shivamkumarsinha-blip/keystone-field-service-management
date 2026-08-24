import { api } from './axios';
import type { LoginResponse } from '../types/auth';

export const authApi = {
  login: (email: string, password: string) =>
    api.post<LoginResponse>('/auth/login', { email, password }).then((r) => r.data),
};
