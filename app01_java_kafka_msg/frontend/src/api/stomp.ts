import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import type { ChatMessage, LifecycleEvent } from './types'

// PLAN_SSDLC.md §7.2: STOMP over the /ws handshake endpoint
// (chatdelivery.infrastructure.WebSocketConfig). The handshake goes
// through the same-origin dev proxy (vite.config.ts) so the browser
// attaches the access_token cookie automatically — the backend's
// SecurityConfig requires it for every non-/api/v1/auth/** request,
// WebSocket handshakes included.
function wsUrl(): string {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws`
}

interface PendingSubscription {
  token: symbol
  destination: string
  handler: (frame: IMessage) => void
}

let client: Client | undefined
// Subscriptions requested before the STOMP CONNECT frame lands. A single
// onConnect callback drains all of them — assigning client.onConnect
// per-call would silently overwrite any earlier pending subscription.
const pending: PendingSubscription[] = []
// Keyed by a per-call token, not by destination: React StrictMode (and any
// fast mount/unmount/remount) can request the same destination twice in a
// row, and each call's cleanup must only ever affect *that* call's own
// subscription — never a different call's subscription to the same topic.
const active = new Map<symbol, StompSubscription>()

function getClient(): Client {
  if (client) return client

  client = new Client({ brokerURL: wsUrl(), reconnectDelay: 5000 })
  client.onConnect = () => {
    for (const { token, destination, handler } of pending.splice(0)) {
      active.set(token, client!.subscribe(destination, handler))
    }
  }
  client.activate()
  return client
}

function subscribe<T>(destination: string, onMessage: (payload: T) => void): () => void {
  const c = getClient()
  const token = Symbol(destination)
  const handler = (frame: IMessage) => onMessage(JSON.parse(frame.body) as T)

  if (c.connected) {
    active.set(token, c.subscribe(destination, handler))
  } else {
    pending.push({ token, destination, handler })
  }

  return () => {
    const subscription = active.get(token)
    if (subscription) {
      subscription.unsubscribe()
      active.delete(token)
      return
    }
    // Not connected yet — this call's subscription is still sitting in
    // `pending`. Without removing it here, it would still get activated
    // by the eventual onConnect drain even though the caller already
    // unmounted, and (per React StrictMode's mount->cleanup->mount) a
    // second call for the same destination would then also be pending,
    // leaving two live subscriptions to one topic and every message
    // delivered — and rendered — twice.
    const index = pending.findIndex((p) => p.token === token)
    if (index !== -1) pending.splice(index, 1)
  }
}

/** Live message updates for a room (delivery status transitions, PLAN_SSDLC.md §7.2). */
export function subscribeToRoom(roomId: string, onMessage: (message: ChatMessage) => void): () => void {
  return subscribe(`/topic/rooms/${roomId}`, onMessage)
}

/** Live DDD/Kafka lifecycle events for the education dashboard (US-2.1/US-2.2). */
export function subscribeToEducation(roomId: string, onEvent: (event: LifecycleEvent) => void): () => void {
  return subscribe(`/topic/education/${roomId}`, onEvent)
}
