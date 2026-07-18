import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { RoomsPage } from './RoomsPage'

vi.mock('../api/client', async () => {
  const actual = await vi.importActual<typeof import('../api/client')>('../api/client')
  return { ...actual, listRooms: vi.fn(), findUserByEmail: vi.fn(), createRoom: vi.fn() }
})

import * as api from '../api/client'

describe('RoomsPage', () => {
  it('lists rooms returned by the API', async () => {
    vi.mocked(api.listRooms).mockResolvedValue([{ id: 'room-1234abcd', participantIds: ['a', 'b'], messageCount: 3 }])

    render(
      <MemoryRouter>
        <RoomsPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText(/2 participants, 3 messages/)).toBeInTheDocument()
  })

  it('shows an empty-state message when there are no rooms', async () => {
    vi.mocked(api.listRooms).mockResolvedValue([])

    render(
      <MemoryRouter>
        <RoomsPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText(/no rooms yet/i)).toBeInTheDocument()
  })

  it('creates a room by resolving the participant email to a UserId first', async () => {
    vi.mocked(api.listRooms).mockResolvedValue([])
    vi.mocked(api.findUserByEmail).mockResolvedValue({ id: 'user-2', email: 'bob@example.com', displayName: 'Bob' })
    vi.mocked(api.createRoom).mockResolvedValue({ id: 'room-999', participantIds: ['me', 'user-2'], messageCount: 0 })

    render(
      <MemoryRouter>
        <RoomsPage />
      </MemoryRouter>,
    )

    fireEvent.change(await screen.findByLabelText(/start a chat with/i), { target: { value: 'bob@example.com' } })
    fireEvent.click(screen.getByRole('button', { name: /start chat/i }))

    await waitFor(() => expect(api.findUserByEmail).toHaveBeenCalledWith('bob@example.com'))
    await waitFor(() => expect(api.createRoom).toHaveBeenCalledWith(['user-2']))
  })
})
