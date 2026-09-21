import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api/auth'
import { getStoredToken, setUnauthorizedHandler, storeToken } from '../api/client'
import type { LoginPayload, RegisterPayload, User } from '../types/api'
import { AuthContext, type AuthContextValue } from './AuthContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  // If a token is stored we must verify it before deciding what to render.
  const [isLoading, setIsLoading] = useState<boolean>(() => getStoredToken() !== null)

  const clearSession = useCallback(() => {
    storeToken(null)
    setUser(null)
  }, [])

  // Any API call that comes back 401 with a token logs the user out.
  useEffect(() => {
    setUnauthorizedHandler(clearSession)
    return () => setUnauthorizedHandler(null)
  }, [clearSession])

  // Restore the session after a page refresh.
  useEffect(() => {
    if (getStoredToken() === null) return
    let cancelled = false
    authApi
      .me()
      .then((current) => {
        if (!cancelled) setUser(current)
      })
      .catch(() => {
        if (!cancelled) clearSession()
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [clearSession])

  const login = useCallback(async (payload: LoginPayload) => {
    const result = await authApi.login(payload)
    storeToken(result.token)
    setUser(result.user)
  }, [])

  const register = useCallback(
    async (payload: RegisterPayload) => {
      await authApi.register(payload)
      await login({ email: payload.email, password: payload.password })
    },
    [login],
  )

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } catch {
      // The token may already be expired; either way the local session must end.
    } finally {
      clearSession()
    }
  }, [clearSession])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isAdmin: user?.isAdmin ?? false,
      isLoading,
      login,
      register,
      logout,
    }),
    [user, isLoading, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
