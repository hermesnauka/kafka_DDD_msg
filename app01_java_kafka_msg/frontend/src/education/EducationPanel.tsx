import { useEffect, useState } from 'react'
import { subscribeToEducation } from '../api/stomp'
import type { LifecycleEvent } from '../api/types'

const MAX_EVENTS = 50

/**
 * US-2.1/US-2.2: a live timeline of the DDD-aggregate-state-change / Kafka
 * event lifecycle for this room — sourced entirely from
 * /topic/education/{roomId} (analyticseducation's own consumer group on
 * chat.room.events, republished as educational.telemetry). This panel
 * never touches message content; the wire payload structurally has none
 * (FR-6, analyticseducation.infrastructure.ChatRoomEventEnvelope).
 */
export function EducationPanel({ roomId }: { roomId: string }) {
  const [events, setEvents] = useState<LifecycleEvent[]>([])

  useEffect(() => {
    setEvents([])
    return subscribeToEducation(roomId, (event) => {
      setEvents((prev) => [...prev.slice(-(MAX_EVENTS - 1)), event])
    })
  }, [roomId])

  return (
    <section aria-label="DDD & Kafka event lifecycle">
      <h2>Event lifecycle (live)</h2>
      {events.length === 0 && <p>Send a message to see its Kafka lifecycle here.</p>}
      {events.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Event</th>
              <th>Message</th>
              <th>Partition</th>
              <th>Offset</th>
              <th>Consumer group</th>
              <th>At</th>
            </tr>
          </thead>
          <tbody>
            {events.map((e, i) => (
              <tr key={`${e.messageId}-${e.eventType}-${i}`}>
                <td>{e.eventType}</td>
                <td>{e.messageId.slice(0, 8)}</td>
                <td>{e.kafkaPartition}</td>
                <td>{e.kafkaOffset}</td>
                <td>{e.consumerGroup}</td>
                <td>{new Date(e.occurredAt).toLocaleTimeString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
