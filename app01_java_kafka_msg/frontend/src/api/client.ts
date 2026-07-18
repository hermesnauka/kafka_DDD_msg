import type { ChatMessage, Room, User } from './types'

// Mirrors com.kafkaddd.chat.web.ApiError.
interface ApiErrorBody {
  error: string
  details: string[]
}

export class ApiError extends Error {
  status: number
  details: string[]

  constructor(status: number, body: ApiErrorBody) {
    super(body.error)
    this.status = status
    this.details = body.details
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    // Cookies (access_token/refresh_token) are HttpOnly and same-origin
    // only (SR-2) — 'include' isn't even required same-origin, but is
    // explicit about the intent.
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })

  if (res.status === 204) {
    return undefined as T
  }

  const body = await res.json().catch(() => ({ error: `request to ${path} failed with ${res.status}`, details: [] }))
  if (!res.ok) {
    throw new ApiError(res.status, body)
  }
  return body as T
}

// --- Auth (identity.infrastructure.AuthController / UserController) ---

export function register(email: string, password: string, displayName: string): Promise<User> {
  return request('/api/v1/auth/register', { method: 'POST', body: JSON.stringify({ email, password, displayName }) })
}

export function login(email: string, password: string): Promise<User> {
  return request('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
}

export function refresh(): Promise<void> {
  return request('/api/v1/auth/refresh', { method: 'POST' })
}

export function logout(): Promise<void> {
  return request('/api/v1/auth/logout', { method: 'POST' })
}

export function currentUser(): Promise<User> {
  return request('/api/v1/users/me')
}

export function findUserByEmail(email: string): Promise<User> {
  return request(`/api/v1/users/by-email?email=${encodeURIComponent(email)}`)
}

// --- Chat Delivery (chatdelivery.infrastructure.RoomController) ---

export function listRooms(): Promise<Room[]> {
  return request('/api/v1/rooms')
}

export function createRoom(participantIds: string[]): Promise<Room> {
  return request('/api/v1/rooms', { method: 'POST', body: JSON.stringify({ participantIds }) })
}

export function listMessages(roomId: string): Promise<ChatMessage[]> {
  return request(`/api/v1/rooms/${encodeURIComponent(roomId)}/messages`)
}

export function sendMessage(roomId: string, content: string): Promise<ChatMessage> {
  return request(`/api/v1/rooms/${encodeURIComponent(roomId)}/messages`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  })
}
