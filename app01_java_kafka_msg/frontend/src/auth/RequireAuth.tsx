import { Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { status } = useAuth()

  if (status === 'loading') return <p>Loading…</p>
  if (status === 'anonymous') return <Navigate to="/login" replace />
  return <>{children}</>
}
