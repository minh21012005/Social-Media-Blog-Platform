import { useEffect, useState } from 'react'
import { SiteFooter } from '../components/SiteFooter'
import { formatCount, getArticleById } from '../services/articles'
import { listMyBookmarks, removeBookmark } from '../services/interactions'

function getBookmarkArticleId(value) {
  if (typeof value === 'string') {
    return value
  }
  if (!value || typeof value !== 'object') {
    return null
  }
  return value.articleId || value.id || null
}

export function BookmarksPage({ requestWithAuth, navigate, notify }) {
  const [state, setState] = useState({ loading: true, items: [], error: '' })
  const [removingId, setRemovingId] = useState('')

  useEffect(() => {
    let active = true

    async function load() {
      setState({ loading: true, items: [], error: '' })
      try {
        const bookmarkValues = await requestWithAuth((token) => listMyBookmarks(token))
        const articleIds = (bookmarkValues || []).map(getBookmarkArticleId).filter(Boolean)

        if (articleIds.length === 0) {
          if (active) {
            setState({ loading: false, items: [], error: '' })
          }
          return
        }

        const loadedArticles = await Promise.all(
          articleIds.map(async (id) => {
            try {
              return await requestWithAuth((token) => getArticleById(id, token))
            } catch {
              return null
            }
          })
        )

        if (active) {
          setState({ loading: false, items: loadedArticles.filter(Boolean), error: '' })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, items: [], error: error.message || 'Could not load bookmarks.' })
        }
      }
    }

    load()
    return () => {
      active = false
    }
  }, [requestWithAuth])

  const handleRemoveBookmark = async (articleId) => {
    if (removingId) {
      return
    }

    setRemovingId(articleId)
    try {
      await requestWithAuth((token) => removeBookmark(articleId, token))
      setState((current) => ({
        ...current,
        items: current.items.filter((item) => item.id !== articleId),
      }))
      notify?.('Removed bookmark.', { title: 'Bookmarks', type: 'success' })
    } catch (error) {
      notify?.(error.message || 'Could not remove bookmark.', { title: 'Bookmarks', type: 'error' })
    } finally {
      setRemovingId('')
    }
  }

  return (
    <main>
      <section className="writer-hero page-container">
        <span className="form-eyebrow">Reading list</span>
        <h1>Your bookmarks.</h1>
        <p>Saved stories stay here so you can pick up where you left off.</p>
      </section>

      <section className="page-container dashboard-section">
        {state.loading && <div className="loading-state">Loading bookmarks...</div>}
        {state.error && <div className="empty-state"><h2>Could not load bookmarks.</h2><p>{state.error}</p></div>}

        {!state.loading && !state.error && state.items.length === 0 && (
          <div className="empty-state">
            <h2>No bookmarks yet.</h2>
            <p>Save stories from the feed or article page and they will appear here.</p>
            <button className="pill-button" type="button" onClick={() => navigate('/')}>Discover stories</button>
          </div>
        )}

        <div className="article-table">
          {state.items.map((article) => (
            <article className="article-table-row" key={article.id}>
              <img alt="" src={article.image} />
              <div>
                <span className="article-category">{article.category}</span>
                <h3>{article.title}</h3>
                <p>{article.summary}</p>
                <span className="dashboard-stat">
                  {formatCount(article.stats?.viewCount)} views
                </span>
              </div>
              <div className="row-actions">
                <button type="button" onClick={() => navigate(article.path)}>
                  Read
                </button>
                <button
                  className="danger-action"
                  disabled={removingId === article.id}
                  type="button"
                  onClick={() => handleRemoveBookmark(article.id)}
                >
                  {removingId === article.id ? 'Removing...' : 'Remove'}
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      <SiteFooter />
    </main>
  )
}
