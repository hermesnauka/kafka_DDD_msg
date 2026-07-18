import { Navigate, Route, Routes, Link } from 'react-router-dom'
import './App.css'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { LoginPage } from './auth/LoginPage'
import { RegisterPage } from './auth/RegisterPage'
import { RequireAuth } from './auth/RequireAuth'
import { RoomsPage } from './chat/RoomsPage'
import { RoomPage } from './chat/RoomPage'

function Shell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth()
  return (
    <>
      <nav className="app-nav">
        <Link to="/">Rooms</Link>
        <span className="spacer" />
        {user && (
          <>
            <span className="user">{user.displayName}</span>
            <button type="button" onClick={() => void logout()}>
              Log out
            </button>
          </>
        )}
      </nav>
      {children}
    </>
  )
}

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <RequireAuth>
              <Shell>
                <RoomsPage />
              </Shell>
            </RequireAuth>
          }
        />
        <Route
          path="/rooms/:roomId"
          element={
            <RequireAuth>
              <Shell>
                <RoomPage />
              </Shell>
            </RequireAuth>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}

export default App
