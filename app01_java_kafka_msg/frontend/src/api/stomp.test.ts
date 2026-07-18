import { beforeEach, describe, expect, it, vi } from 'vitest'

// Regression test for a real bug caught during development: assigning
// `client.onConnect = ...` on every subscribe() call silently overwrote
// any earlier pending subscription requested before the socket connected
// (e.g. a room view mounting both the message list and the education
// panel at once). Fixed by draining a shared queue in a single onConnect
// handler — this test proves both subscriptions survive.
describe('stomp subscription queueing', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('activates every subscription requested before CONNECT, not just the last one', async () => {
    let connectHandler: (() => void) | undefined
    const subscribedDestinations: string[] = []

    vi.doMock('@stomp/stompjs', () => ({
      Client: vi.fn().mockImplementation(function (this: Record<string, unknown>) {
        this.connected = false
        this.activate = vi.fn()
        this.subscribe = vi.fn((destination: string) => {
          subscribedDestinations.push(destination)
          return { unsubscribe: vi.fn() }
        })
        Object.defineProperty(this, 'onConnect', {
          set(fn: () => void) {
            connectHandler = fn
          },
        })
      }),
    }))

    const { subscribeToRoom, subscribeToEducation } = await import('./stomp')

    subscribeToRoom('room-1', () => {})
    subscribeToEducation('room-1', () => {})

    // Not connected yet — nothing should be subscribed to the real client.
    expect(subscribedDestinations).toEqual([])

    connectHandler?.()

    expect(subscribedDestinations).toHaveLength(2)
    expect(subscribedDestinations).toEqual(
      expect.arrayContaining(['/topic/rooms/room-1', '/topic/education/room-1']),
    )
  })

  // Regression test for a second, related real bug: found live via an
  // end-to-end run where React 19 StrictMode's dev-mode mount->cleanup->
  // remount double-invoked an effect subscribing to the same destination.
  // The first call's cleanup ran before CONNECT ever fired, so its
  // subscription was still sitting in the pending queue, not yet
  // "active" — and the old cleanup only knew how to unsubscribe an active
  // one. Net effect: both the first (unmounted) and second (current)
  // handlers ended up subscribed, so every message was delivered — and
  // rendered — twice.
  it('unsubscribing a still-pending subscription (before CONNECT) prevents it from ever activating', async () => {
    let connectHandler: (() => void) | undefined
    const subscribedDestinations: string[] = []

    vi.doMock('@stomp/stompjs', () => ({
      Client: vi.fn().mockImplementation(function (this: Record<string, unknown>) {
        this.connected = false
        this.activate = vi.fn()
        this.subscribe = vi.fn((destination: string) => {
          subscribedDestinations.push(destination)
          return { unsubscribe: vi.fn() }
        })
        Object.defineProperty(this, 'onConnect', {
          set(fn: () => void) {
            connectHandler = fn
          },
        })
      }),
    }))

    const { subscribeToEducation } = await import('./stomp')

    // Simulates React StrictMode: effect runs, then its cleanup runs
    // (component "unmounts") before the socket has connected, then the
    // effect runs again ("remounts").
    const unsubscribeFirst = subscribeToEducation('room-1', () => {})
    unsubscribeFirst()
    subscribeToEducation('room-1', () => {})

    connectHandler?.()

    // Only the second (surviving) subscription should have been activated
    // — not two subscriptions to the same destination.
    expect(subscribedDestinations).toEqual(['/topic/education/room-1'])
  })
})
