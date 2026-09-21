import { apiRequest } from './client'
import type { AuthResponse, LoginPayload, RegisterPayload, User } from '../types/api'

export const authApi = {
  register: (payload: RegisterPayload) =>
    apiRequest<User>('/api/auth/register', { method: 'POST', body: payload, auth: false }),

  login: (payload: LoginPayload) =>
    apiRequest<AuthResponse>('/api/auth/login', { method: 'POST', body: payload, auth: false }),

  logout: () => apiRequest<void>('/api/auth/logout', { method: 'POST' }),

  me: () => apiRequest<User>('/api/auth/me'),
}
