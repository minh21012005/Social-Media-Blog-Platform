import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'

export function WebSocketManager({ session, notify }) {
  const clientRef = useRef(null)

  useEffect(() => {
    // Only connect if the user is logged in
    if (!session || !session.accessToken || !session.user) {
      return
    }

    const { accessToken, user } = session

    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws/notifications',
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      debug: () => {
        // You can comment this out in production
        // console.log('[STOMP]', str)
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    })

    client.onConnect = (frame) => {
      console.log('[WebSocket] Connected:', frame)

      // Subscribe to user-specific notifications queue
      // Lệnh subscribe không cần gắn cứng user.id vì Spring Boot sẽ tự phân giải từ Token!
      const subscriptionPath = `/user/queue/notifications`
      client.subscribe(subscriptionPath, (message) => {
        if (message.body) {
          try {
            const event = JSON.parse(message.body)
            // Dispatch event for UI synchronization
            window.dispatchEvent(new CustomEvent('NEW_NOTIFICATION', { detail: event }))
          } catch (e) {
            console.error('[WebSocket] Failed to parse message body:', e)
          }
        }
      })
    }

    client.onStompError = (frame) => {
      console.error('[WebSocket] Broker reported error:', frame.headers['message'])
      console.error('[WebSocket] Additional details:', frame.body)
    }

    client.activate()
    clientRef.current = client

    // Cleanup on unmount or session change
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate()
        clientRef.current = null
      }
    }
  }, [session, notify])

  // Headless component (renders nothing)
  return null
}
