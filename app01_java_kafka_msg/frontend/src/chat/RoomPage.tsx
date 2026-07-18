import { useEffect, useState, type FormEvent } from 'react'
import { useParams } from 'react-router-dom'
import * as api from '../api/client'
import { ApiError } from '../api/client'
import { subscribeToRoom } from '../api/stomp'
import type { ChatMessage } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { EducationPanel } from '../education/EducationPanel'

export function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>()
  const { user } = useAuth()
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loadError, setLoadError] = useState<string>()
  const [content, setContent] = useState('')
  const [sendError, setSendError] = useState<string>()
  const [sending, setSending] = useState(false)

  useEffect(() => {
    if (!roomId) return
    let cancelled = false
    api
      .listMessages(roomId)
      .then((m) => {
        if (!cancelled) setMessages(m)
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err instanceof ApiError ? err.message : 'Failed to load messages.')
      })
    return () => {
      cancelled = true
    }
  }, [roomId])

  useEffect(() => {
    if (!roomId) return
    // US-1.2: the STOMP push is how a message's PENDING -> POLLED ->
    // DELIVERED transition (driven asynchronously by the Kafka consumer
    // side, ProcessMessageSentUseCase) reaches the UI without polling.
    return subscribeToRoom(roomId, (updated) => {
      setMessages((prev) => {
        const exists = prev.some((m) => m.id === updated.id)
        return exists ? prev.map((m) => (m.id === updated.id ? updated : m)) : [...prev, updated]
      })
    })
  }, [roomId])

  async function handleSend(e: FormEvent) {
    e.preventDefault()
    if (!roomId) return
    setSendError(undefined)
    setSending(true)
    try {
      const message = await api.sendMessage(roomId, content)
      setMessages((prev) => [...prev, message])
      setContent('')
    } catch (err) {
      setSendError(err instanceof ApiError ? err.message : 'Failed to send message.')
    } finally {
      setSending(false)
    }
  }

  if (!roomId) return null

  return (
    <main>
      <h1>Room {roomId.slice(0, 8)}</h1>

      {loadError && (
        <p role="alert" className="error">
          {loadError}
        </p>
      )}

      <ul>
        {messages.map((m) => (
          <li key={m.id}>
            <strong>{m.senderId === user?.id ? 'You' : m.senderId.slice(0, 8)}:</strong> {m.content}{' '}
            <em>({m.status})</em>
          </li>
        ))}
      </ul>

      <form onSubmit={handleSend}>
        <label>
          Message
          <input value={content} onChange={(e) => setContent(e.target.value)} required autoFocus />
        </label>
        {sendError && (
          <p role="alert" className="error">
            {sendError}
          </p>
        )}
        <button type="submit" disabled={sending}>
          Send
        </button>
      </form>

      <EducationPanel roomId={roomId} />
    </main>
  )
}
