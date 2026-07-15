import { useEffect, useState } from 'react'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { archiveArticle, deleteArticle, formatCount, getArticleClapCount, listMyArticles, publishArticle } from '../services/articles'

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
        const items = await requestWithAuth(async (token) => {
          const counts = await Promise.all(
            (result.items || []).map((article) =>
              getArticleClapCount(article.id, token)
                .then((data) => Number(data?.clapCount || 0))
                .catch(() => Number(article.stats?.clapCount || 0))
            )
          )
          return (result.items || []).map((article, index) => ({
            ...article,
            stats: {
              ...(article.stats || {}),
              clapCount: counts[index],
            },
          }))
        })
        if (active) {
          setState({
            loading: false,
            articles: items,
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
      const items = await requestWithAuth(async (token) => {
        const counts = await Promise.all(
          (result.items || []).map((article) =>
            getArticleClapCount(article.id, token)
              .then((data) => Number(data?.clapCount || 0))
              .catch(() => Number(article.stats?.clapCount || 0))
          )
        )
        return (result.items || []).map((article, index) => ({
          ...article,
          stats: {
            ...(article.stats || {}),
            clapCount: counts[index],
          },
        }))
      })
      if (result.items.length === 0 && page > 0) {
        setPage(page - 1)
      } else {
        setState({
          loading: false,
          articles: items,
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
    <main className="my-articles-page">
      <section className="dashboard-page-hero page-container">
        <div>
          <span className="dashboard-kicker">Writer dashboard</span>
          <h1>Your stories</h1>
          <p>Draft, publish, and manage every story from one focused workspace.</p>
        </div>
        <button className="dashboard-primary-action" type="button" onClick={() => navigate('/write')}>
          <span aria-hidden="true">+</span> New story
        </button>
      </section>

      <section className="page-container dashboard-content-section">
        <div className="article-dashboard-toolbar">
          <div>
            <h2>Articles</h2>
            <p>{state.loading ? 'Loading your workspace...' : `${state.articles.length} ${state.articles.length === 1 ? 'story' : 'stories'} in this view`}</p>
          </div>
          <div aria-label="Filter articles by status" className="article-status-tabs" role="tablist">
            {['', 'DRAFT', 'PUBLISHED', 'ARCHIVED'].map((item) => (
              <button
                aria-selected={status === item}
                className={status === item ? 'active' : ''}
                key={item || 'all'}
                role="tab"
                type="button"
                onClick={() => {
                  setStatus(item)
                  setPage(0)
                }}
              >
                {item ? `${item.charAt(0)}${item.slice(1).toLowerCase()}` : 'All'}
              </button>
            ))}
          </div>
        </div>

        {state.loading && <div className="loading-state">Loading your articles...</div>}
        {state.error && <div className="empty-state"><h2>Could not load articles.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && state.articles.length === 0 && (
          <div className="empty-state">
            <h2>No articles here yet.</h2>
            <p>Start a draft and it will show up in this dashboard.</p>
          </div>
        )}

        <div className="managed-article-list">
          {state.articles.map((article) => (
            <article className="managed-article-card" key={article.id}>
              <img alt="" className="managed-article-cover" src={article.image} />
              <div className="managed-article-copy">
                <span className={`article-status-badge status-${article.status?.toLowerCase()}`}>{article.status}</span>
                <h3>{article.title}</h3>
                <p>{article.summary}</p>
                <div className="managed-article-meta">
                  <span>{formatCount(article.stats?.viewCount)} views</span>
                  <span>{formatCount(article.stats?.clapCount)} claps</span>
                  {article.date && <span>{article.date}</span>}
                </div>
              </div>
              <div className="managed-article-actions">
                <button className="article-action-primary" type="button" onClick={() => navigate(`/articles/${article.id}/edit`)}>Edit</button>
                {article.status !== 'PUBLISHED' && (
                  <button type="button" onClick={() => runAction((token) => publishArticle(article.id, token))}>Publish</button>
                )}
                {article.status !== 'ARCHIVED' && (
                  <button type="button" onClick={() => runAction((token) => archiveArticle(article.id, token))}>Archive</button>
                )}
                {['DRAFT', 'ARCHIVED'].includes(article.status) && (
                  <button className="article-action-danger" type="button" onClick={() => setPendingDelete(article)}>Delete</button>
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
