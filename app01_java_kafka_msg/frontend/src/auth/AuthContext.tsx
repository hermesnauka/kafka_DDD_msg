import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import * as api from '../api/client'
import type { User } from '../api/types'

interface AuthState {
  user: User | undefined
  // 'loading': checking for an existing session (GET /users/me) on first
  // mount. 'anonymous' and 'authenticated' are both settled states — the
  // distinction matters so a login form doesn't flash before the initial
  // session check resolves.
  status: 'loading' | 'anonymous' | 'authenticated'
}

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, displayName: string) => Promise<User>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ user: undefined, status: 'loading' })

  useEffect(() => {
    let cancelled = false
    api
      .currentUser()
      .then((user) => {
        if (!cancelled) setState({ user, status: 'authenticated' })
      })
      .catch(() => {
        // No session cookie, or it's expired/invalid — this is the normal
        // logged-out case, not a failure worth surfacing as an error.
        if (!cancelled) setState({ user: undefined, status: 'anonymous' })
      })
    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const user = await api.login(email, password)
    setState({ user, status: 'authenticated' })
  }, [])

  const register = useCallback(
    (email: string, password: string, displayName: string) => api.register(email, password, displayName),
    [],
  )

  const logout = useCallback(async () => {
    await api.logout()
    setState({ user: undefined, status: 'anonymous' })
  }, [])

  const value = useMemo(() => ({ ...state, login, register, logout }), [state, login, register, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
