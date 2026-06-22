import { useState, useEffect } from 'react'
import { getPresenceStatus } from '../services/presence'

export function AuthorBadge({ author, navigate }) {
  const [isOnline, setIsOnline] = useState(false)

  useEffect(() => {
    if (!author?.id) return

    let mounted = true
    const checkPresence = async () => {
      try {
        const statusMap = await getPresenceStatus([author.id])
        if (mounted && statusMap[author.id]) {
          setIsOnline(true)
        } else if (mounted) {
          setIsOnline(false)
        }
      } catch (err) {
        // ignore errors silently for presence
      }
    }

    checkPresence()
    // Poll every 30 seconds
    const intervalId = setInterval(checkPresence, 30000)

    return () => {
      mounted = false
      clearInterval(intervalId)
    }
  }, [author?.id])

  const open = (event) => {
    if (!navigate || !author?.username) {
      return
    }
    event.preventDefault()
    navigate(`/author/${author.username}`)
  }

  return (
    <a className="author-badge" href={author?.username ? `/author/${author.username}` : '#'} onClick={open}>
      <div style={{ position: 'relative', display: 'inline-block' }}>
        <img alt="" src={author?.avatar} />
        {isOnline && (
          <span 
            style={{
              position: 'absolute',
              bottom: '2px',
              right: '2px',
              width: '10px',
              height: '10px',
              backgroundColor: '#10b981',
              borderRadius: '50%',
              border: '2px solid white'
            }} 
            title="Online"
          />
        )}
      </div>
      <span>{author?.name || 'Chronicle Writer'}</span>
    </a>
  )
}
