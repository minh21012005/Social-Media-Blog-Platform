import { useEffect, useState } from 'react'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { archiveArticle, deleteArticle, formatCount, listMyArticles, publishArticle } from '../services/articles'

export function MyArticlesPage({ requestWithAuth, navigate, notify }) {
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [state, setState] = useState({ loading: true, articles: [], error: '', page: 0, totalPages: 0 })
  const [pendingDelete, setPendingDelete] = useState(null)

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = await requestWithAuth((token) => listMyArticles({ status, page }, token))
        if (active) {
          setState({
            loading: false,
            articles: result.items,
            error: '',
            page: result.page || 0,
            totalPages: result.totalPages || 0,
          })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, articles: [], error: error.message, page: 0, totalPages: 0 })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [page, requestWithAuth, status])

  const runAction = async (action, options = {}) => {
    try {
      await requestWithAuth(action)
      const result = await requestWithAuth((token) => listMyArticles({ status, page }, token))
      if (result.items.length === 0 && page > 0) {
        setPage(page - 1)
      } else {
        setState({
          loading: false,
          articles: result.items,
          error: '',
          page: result.page || 0,
          totalPages: result.totalPages || 0,
        })
      }
      if (options.successMessage) {
        notify?.(options.successMessage, { title: options.successTitle || 'Article updated', type: 'success' })
      }
    } catch (error) {
      notify?.(friendlyArticleError(error.message), { title: options.errorTitle || 'Could not update article' })
    }
  }

  const deleteDraftOrArchived = (article) => {
    setPendingDelete(null)
    runAction((token) => deleteArticle(article.id, token), {
      successMessage: 'Article deleted.',
      errorTitle: 'Could not delete article',
    })
  }

  return (
    <main>
      <section className="writer-hero dashboard-hero page-container">
        <div>
          <span className="form-eyebrow">Dashboard</span>
          <h1>My articles.</h1>
          <p>Manage drafts, published stories, and archived pieces from one calm desk.</p>
        </div>
        <button className="pill-button" type="button" onClick={() => navigate('/write')}>New story</button>
      </section>

      <section className="page-container dashboard-section">
        <div className="dashboard-tabs">
          {['', 'DRAFT', 'PUBLISHED', 'ARCHIVED'].map((item) => (
            <button
              className={status === item ? 'active' : ''}
              key={item || 'all'}
              type="button"
              onClick={() => {
                setStatus(item)
                setPage(0)
              }}
            >
              {item || 'ALL'}
            </button>
          ))}
        </div>

        {state.loading && <div className="loading-state">Loading your articles...</div>}
        {state.error && <div className="empty-state"><h2>Could not load articles.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && state.articles.length === 0 && (
          <div className="empty-state">
            <h2>No articles here yet.</h2>
            <p>Start a draft and it will show up in this dashboard.</p>
          </div>
        )}
        <div className="article-table">
          {state.articles.map((article) => (
            <article className="article-table-row" key={article.id}>
              <img alt="" src={article.image} />
              <div>
                <span className="article-category">{article.status}</span>
                <h3>{article.title}</h3>
                <p>{article.summary}</p>
                <span className="dashboard-stat">
                  {formatCount(article.stats?.viewCount)} views &middot; {formatCount(article.stats?.likeCount ?? article.stats?.likesCount ?? article.likeCount ?? article.likesCount)} likes
                </span>
              </div>
              <div className="row-actions">
                <button type="button" onClick={() => navigate(`/articles/${article.id}/edit`)}>Edit</button>
                {article.status !== 'PUBLISHED' && (
                  <button type="button" onClick={() => runAction((token) => publishArticle(article.id, token))}>Publish</button>
                )}
                {article.status !== 'ARCHIVED' && (
                  <button type="button" onClick={() => runAction((token) => archiveArticle(article.id, token))}>Archive</button>
                )}
                {['DRAFT', 'ARCHIVED'].includes(article.status) && (
                  <button className="danger-action" type="button" onClick={() => setPendingDelete(article)}>Delete</button>
                )}
              </div>
            </article>
          ))}
        </div>
        <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
      </section>
      {pendingDelete && (
        <div className="confirm-backdrop" role="presentation" onMouseDown={() => setPendingDelete(null)}>
          <section
            aria-labelledby="delete-article-title"
            aria-modal="true"
            className="confirm-dialog"
            role="dialog"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <span className="form-eyebrow">Delete article</span>
            <h2 id="delete-article-title">Remove this story?</h2>
            <p>
              This will remove <strong>{pendingDelete.title}</strong> from your dashboard.
              Audit history and internal records are kept.
            </p>
            <div className="confirm-actions">
              <button className="text-button" type="button" onClick={() => setPendingDelete(null)}>Cancel</button>
              <button className="danger-submit" type="button" onClick={() => deleteDraftOrArchived(pendingDelete)}>Delete article</button>
            </div>
          </section>
        </div>
      )}
      <SiteFooter />
    </main>
  )
}

function friendlyArticleError(message) {
  if (!message) {
    return 'Please review the article and try again.'
  }
  if (message.toLowerCase().includes('cover image')) {
    return 'Add a cover image before publishing this story.'
  }
  return message
}
