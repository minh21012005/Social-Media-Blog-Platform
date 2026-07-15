import { useEffect, useState } from 'react'
import { SiteFooter } from '../components/SiteFooter'
import { getArticleById, listMyBookmarks, removeBookmarkArticle } from '../services/articles'

export function SavedBookmarksPage({ requestWithAuth, navigate, notify }) {
  const [state, setState] = useState({ loading: true, items: [], error: '' })
  const [removingId, setRemovingId] = useState('')

  useEffect(() => {
    let active = true

    async function loadBookmarks() {
      setState({ loading: true, items: [], error: '' })
      try {
        const rows = await requestWithAuth(async (token) => {
          const bookmarkResponse = await listMyBookmarks(token)
          const bookmarks = Array.isArray(bookmarkResponse)
            ? bookmarkResponse
            : (bookmarkResponse?.items || [])

          return Promise.all(
            (bookmarks || []).map(async (bookmark) => {
              try {
                const article = await getArticleById(bookmark.articleId, token)
                return {
                  article,
                  bookmarkedAt: bookmark.bookmarkedAt,
                }
              } catch {
                return null
              }
            })
          )
        })

        if (!active) {
          return
        }

        setState({
          loading: false,
          items: rows.filter(Boolean),
          error: '',
        })
      } catch (error) {
        if (active) {
          setState({ loading: false, items: [], error: error.message || 'Could not load bookmarks.' })
        }
      }
    }

    loadBookmarks()
    return () => {
      active = false
    }
  }, [requestWithAuth])

  const handleRemove = async (articleId) => {
    setRemovingId(articleId)
    try {
      await requestWithAuth((token) => removeBookmarkArticle(articleId, token))
      setState((current) => ({
        ...current,
        items: current.items.filter((item) => item.article.id !== articleId),
      }))
      notify?.('Bookmark removed.', { title: 'Saved list updated', type: 'success' })
    } catch (error) {
      notify?.(error.message || 'Could not remove bookmark.', { title: 'Action failed' })
    } finally {
      setRemovingId('')
    }
  }

  return (
    <main className="bookmarks-page">
      <section className="bookmarks-hero page-container">
        <div>
          <span className="dashboard-kicker">Your library</span>
          <h1>Stories worth returning to</h1>
          <p>A quiet collection of ideas, essays, and perspectives you saved for later.</p>
        </div>
        {!state.loading && !state.error && (
          <span className="bookmark-count">{state.items.length} saved</span>
        )}
      </section>

      <section className="page-container bookmarks-content-section">
        {state.loading && <div className="loading-state">Loading saved stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load bookmarks.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && state.items.length === 0 && (
          <div className="empty-state">
            <h2>No saved stories yet.</h2>
            <p>Open any article and tap the bookmark icon to save it here.</p>
          </div>
        )}

        <div className="bookmark-grid">
          {state.items.map(({ article, bookmarkedAt }) => (
            <article className="bookmark-card" key={article.id}>
              <button className="bookmark-card-cover" type="button" onClick={() => navigate(`/articles/${article.slug}`)}>
                <img alt="" src={article.image} />
              </button>
              <div className="bookmark-card-body">
                <div className="bookmark-card-labels">
                  <span>{article.category || 'Saved story'}</span>
                  <time dateTime={bookmarkedAt}>Saved {new Date(bookmarkedAt).toLocaleDateString()}</time>
                </div>
                <h3>{article.title}</h3>
                <p>{article.summary}</p>
                <div className="bookmark-card-actions">
                  <button className="bookmark-read-action" type="button" onClick={() => navigate(`/articles/${article.slug}`)}>
                    Read story <span aria-hidden="true">→</span>
                  </button>
                  <button
                    className="bookmark-remove-action"
                    disabled={removingId === article.id}
                    type="button"
                    onClick={() => handleRemove(article.id)}
                  >
                    {removingId === article.id ? 'Removing...' : 'Remove'}
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
