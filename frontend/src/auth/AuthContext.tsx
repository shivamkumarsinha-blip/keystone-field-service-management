import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { authApi } from '../api/authApi';
import type { UserDto } from '../types/auth';

interface AuthContextValue {
  user: UserDto | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(() => {
    const stored = localStorage.getItem('keystone_user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password);
    localStorage.setItem('keystone_token', response.token);
    localStorage.setItem('keystone_user', JSON.stringify(response.user));
    setUser(response.user);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('keystone_token');
    localStorage.removeItem('keystone_user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
