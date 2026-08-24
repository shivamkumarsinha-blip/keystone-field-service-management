export type Role = 'DISPATCHER' | 'TECHNICIAN' | 'MANAGER' | 'CUSTOMER';

export interface UserDto {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  active: boolean;
}

export interface LoginResponse {
  token: string;
  user: UserDto;
}
