import { useEffect, useState } from 'react'
import { SiteFooter } from '../components/SiteFooter'
import { archiveArticle, formatCount, listMyArticles, publishArticle } from '../services/articles'

export function MyArticlesPage({ requestWithAuth, navigate, notify }) {
  const [status, setStatus] = useState('')
  const [state, setState] = useState({ loading: true, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const page = await requestWithAuth((token) => listMyArticles({ status }, token))
        if (active) {
          setState({ loading: false, articles: page.items, error: '' })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, articles: [], error: error.message })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [requestWithAuth, status])

  const runAction = async (action) => {
    try {
      await requestWithAuth(action)
      const page = await requestWithAuth((token) => listMyArticles({ status }, token))
      setState({ loading: false, articles: page.items, error: '' })
    } catch (error) {
      notify?.(friendlyArticleError(error.message), { title: 'Could not update article' })
    }
  }

  return (
    <main>
      <section className="writer-hero page-container">
        <span className="form-eyebrow">Dashboard</span>
        <h1>My articles.</h1>
        <p>Manage drafts, published stories, and archived pieces from one calm desk.</p>
        <button className="pill-button" type="button" onClick={() => navigate('/write')}>New story</button>
      </section>

      <section className="page-container dashboard-section">
        <div className="dashboard-tabs">
          {['', 'DRAFT', 'PUBLISHED', 'ARCHIVED'].map((item) => (
            <button
              className={status === item ? 'active' : ''}
              key={item || 'all'}
              type="button"
              onClick={() => setStatus(item)}
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
                <span className="dashboard-stat">{formatCount(article.stats?.viewCount)} views</span>
              </div>
              <div className="row-actions">
                <button type="button" onClick={() => navigate(`/articles/${article.id}/edit`)}>Edit</button>
                {article.status !== 'PUBLISHED' && (
                  <button type="button" onClick={() => runAction((token) => publishArticle(article.id, token))}>Publish</button>
                )}
                {article.status !== 'ARCHIVED' && (
                  <button type="button" onClick={() => runAction((token) => archiveArticle(article.id, token))}>Archive</button>
                )}
              </div>
            </article>
          ))}
        </div>
      </section>
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
