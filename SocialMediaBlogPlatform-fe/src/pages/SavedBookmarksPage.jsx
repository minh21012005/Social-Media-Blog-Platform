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
    <main>
      <section className="writer-hero dashboard-hero page-container">
        <div>
          <span className="form-eyebrow">Library</span>
          <h1>Saved bookmarks.</h1>
          <p>Pick up where you left off and continue reading any time.</p>
        </div>
      </section>

      <section className="page-container dashboard-section">
        {state.loading && <div className="loading-state">Loading saved stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load bookmarks.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && state.items.length === 0 && (
          <div className="empty-state">
            <h2>No saved stories yet.</h2>
            <p>Open any article and tap the bookmark icon to save it here.</p>
          </div>
        )}

        <div className="article-table">
          {state.items.map(({ article, bookmarkedAt }) => (
            <article className="article-table-row" key={article.id}>
              <img alt="" src={article.image} />
              <div>
                <span className="article-category">Saved</span>
                <h3>{article.title}</h3>
                <p>{article.summary}</p>
                <span className="dashboard-stat">Saved on {new Date(bookmarkedAt).toLocaleDateString()}</span>
              </div>
              <div className="row-actions">
                <button type="button" onClick={() => navigate(`/articles/${article.slug}`)}>Read</button>
                <button
                  className="danger-action"
                  disabled={removingId === article.id}
                  type="button"
                  onClick={() => handleRemove(article.id)}
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
