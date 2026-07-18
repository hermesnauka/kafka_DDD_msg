import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import * as api from '../api/client'
import { ApiError } from '../api/client'
import type { Room } from '../api/types'

export function RoomsPage() {
  const navigate = useNavigate()
  const [rooms, setRooms] = useState<Room[]>()
  const [loadError, setLoadError] = useState<string>()
  const [email, setEmail] = useState('')
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState<string>()

  useEffect(() => {
    let cancelled = false
    api
      .listRooms()
      .then((r) => {
        if (!cancelled) setRooms(r)
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err instanceof ApiError ? err.message : 'Failed to load rooms.')
      })
    return () => {
      cancelled = true
    }
  }, [])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    setCreateError(undefined)
    setCreating(true)
    try {
      // US-1.3: the create-room API only accepts a UserId, so this first
      // resolves the "who do I want to chat with" email into one
      // (UserController.byEmail — added specifically to support this UI).
      const other = await api.findUserByEmail(email)
      const room = await api.createRoom([other.id])
      navigate(`/rooms/${room.id}`)
    } catch (err) {
      setCreateError(err instanceof ApiError ? err.message : 'Could not start a chat with that email.')
    } finally {
      setCreating(false)
    }
  }

  return (
    <main>
      <h1>Your rooms</h1>

      <form onSubmit={handleCreate}>
        <label>
          Start a chat with (email)
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        {createError && (
          <p role="alert" className="error">
            {createError}
          </p>
        )}
        <button type="submit" disabled={creating}>
          {creating ? 'Starting…' : 'Start chat'}
        </button>
      </form>

      <h2>Rooms</h2>
      {loadError && (
        <p role="alert" className="error">
          {loadError}
        </p>
      )}
      {rooms && rooms.length === 0 && <p>No rooms yet — start a chat above.</p>}
      {rooms && rooms.length > 0 && (
        <ul>
          {rooms.map((room) => (
            <li key={room.id}>
              <Link to={`/rooms/${room.id}`}>
                Room {room.id.slice(0, 8)} — {room.participantIds.length} participants, {room.messageCount} messages
              </Link>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
