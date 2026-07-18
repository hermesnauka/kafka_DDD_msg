import { act } from 'react'
import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { EducationPanel } from './EducationPanel'
import type { LifecycleEvent } from '../api/types'

vi.mock('../api/stomp', () => ({ subscribeToEducation: vi.fn() }))

import { subscribeToEducation } from '../api/stomp'

describe('EducationPanel', () => {
  it('shows a placeholder before any lifecycle event arrives', () => {
    vi.mocked(subscribeToEducation).mockReturnValue(() => {})

    render(<EducationPanel roomId="room-1" />)

    expect(screen.getByText(/send a message/i)).toBeInTheDocument()
  })

  it('renders each lifecycle event pushed over the subscription, metadata only', () => {
    let handler: (event: LifecycleEvent) => void = () => {}
    vi.mocked(subscribeToEducation).mockImplementation((_roomId, cb) => {
      handler = cb
      return () => {}
    })

    render(<EducationPanel roomId="room-1" />)

    act(() => {
      handler({
        eventType: 'MessageSent',
        roomId: 'room-1',
        messageId: 'msg-12345678',
        kafkaPartition: 0,
        kafkaOffset: 5,
        consumerGroup: 'analytics-education-service',
        occurredAt: '2026-01-01T00:00:00Z',
      })
    })

    expect(screen.getByText('MessageSent')).toBeInTheDocument()
    expect(screen.getByText('5')).toBeInTheDocument()
    expect(screen.getByText('analytics-education-service')).toBeInTheDocument()
  })

  it('resubscribes when the roomId changes', () => {
    const unsubscribeRoom1 = vi.fn()
    vi.mocked(subscribeToEducation).mockReturnValueOnce(unsubscribeRoom1)

    const { rerender } = render(<EducationPanel roomId="room-1" />)
    expect(subscribeToEducation).toHaveBeenCalledWith('room-1', expect.any(Function))

    vi.mocked(subscribeToEducation).mockReturnValueOnce(() => {})
    rerender(<EducationPanel roomId="room-2" />)

    expect(unsubscribeRoom1).toHaveBeenCalled()
    expect(subscribeToEducation).toHaveBeenCalledWith('room-2', expect.any(Function))
  })
})
