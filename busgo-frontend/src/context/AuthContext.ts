import { createContext } from 'react'
import type { LoginPayload, RegisterPayload, User } from '../types/api'

export interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  isAdmin: boolean
  /** True while a stored token is being checked after a page load. */
  isLoading: boolean
  login: (payload: LoginPayload) => Promise<void>
  /** Creates the account and logs the user in. */
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
