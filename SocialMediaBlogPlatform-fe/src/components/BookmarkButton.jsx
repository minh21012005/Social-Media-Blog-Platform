import { useState } from 'react'
import { bookmarkArticle, removeBookmark } from '../services/interactions'
import { loadAuth } from '../services/auth'

export function BookmarkButton({ articleId, saved = false, requestWithAuth, notify }) {
  const [isSaved, setSaved] = useState(Boolean(saved))
  const [loading, setLoading] = useState(false)

  const performWithAuth = async (fn) => {
    if (requestWithAuth) {
      return requestWithAuth((token) => fn(token))
    }
    const auth = loadAuth()
    if (!auth?.accessToken) {
      if (notify) notify('Please sign in to bookmark articles', { type: 'error' })
      window.location = '/login'
      throw new Error('Not authenticated')
    }
    return fn(auth.accessToken)
  }

  const toggle = async () => {
    if (loading) return
    setLoading(true)
    const prev = isSaved
    setSaved(!prev)
    try {
      if (prev) {
        await performWithAuth((token) => removeBookmark(articleId, token))
        if (notify) notify('Removed bookmark', { type: 'info' })
      } else {
        await performWithAuth((token) => bookmarkArticle(articleId, token))
        if (notify) notify('Saved to bookmarks', { type: 'success' })
      }
    } catch (err) {
      setSaved(prev)
      if (notify) notify(err.message || 'Could not update bookmark', { type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <button className={`bookmark-button ${isSaved ? 'saved' : ''}`} type="button" onClick={toggle} aria-pressed={isSaved} disabled={loading}>
      {isSaved ? 'Saved' : 'Save'}
    </button>
  )
}
