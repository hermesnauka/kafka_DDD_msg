import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './AuthContext'
import { LoginPage } from './LoginPage'
import { ApiError } from '../api/client'

vi.mock('../api/client', async () => {
  const actual = await vi.importActual<typeof import('../api/client')>('../api/client')
  return { ...actual, currentUser: vi.fn(), login: vi.fn() }
})

import * as api from '../api/client'

function renderLoginPage() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<p>Home</p>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

describe('LoginPage', () => {
  it('logs in and navigates home on success', async () => {
    vi.mocked(api.currentUser).mockRejectedValue(new Error('no session'))
    vi.mocked(api.login).mockResolvedValue({ id: 'user-1', email: 'alice@example.com', displayName: 'Alice' })

    renderLoginPage()

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'alice@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'correct-horse-battery' } })
    fireEvent.click(screen.getByRole('button', { name: /log in/i }))

    await waitFor(() => expect(screen.getByText('Home')).toBeInTheDocument())
  })

  it('shows the backend error message on failed login (SR-1: same message for unknown email or wrong password)', async () => {
    vi.mocked(api.currentUser).mockRejectedValue(new Error('no session'))
    vi.mocked(api.login).mockRejectedValue(new ApiError(401, { error: 'invalid email or password', details: [] }))

    renderLoginPage()

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'alice@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrong' } })
    fireEvent.click(screen.getByRole('button', { name: /log in/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('invalid email or password')
  })

  it('shows a distinct message when the account is locked (SR-1 throttling)', async () => {
    vi.mocked(api.currentUser).mockRejectedValue(new Error('no session'))
    vi.mocked(api.login).mockRejectedValue(
      new ApiError(423, { error: 'account is locked until 2026-01-01T00:00:00Z', details: [] }),
    )

    renderLoginPage()

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'alice@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'correct-horse-battery' } })
    fireEvent.click(screen.getByRole('button', { name: /log in/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('account is locked')
  })
})
