import { useEffect, useState } from 'react'
import { ArticleMeta } from '../components/ArticleCard'
import { BookmarkButton } from '../components/BookmarkButton'
import { AuthorBadge } from '../components/AuthorBadge'
import { MarkdownPreview } from '../components/MarkdownPreview'
import { SiteFooter } from '../components/SiteFooter'
import { formatCount, getArticleBySlug, recordArticleView } from '../services/articles'

export function ArticleDetailPage({ slug, navigate, requestWithAuth, notify }) {
  const [state, setState] = useState({ loading: true, article: null, error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const article = await getArticleBySlug(slug)
        recordArticleView(article.id, { source: 'web' }).catch(() => null)
        if (active) {
          setState({ loading: false, article, error: '' })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, article: null, error: error.message })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [slug])

  if (state.loading) {
    return <main className="page-container loading-state">Loading story...</main>
  }

  if (state.error || !state.article) {
    return (
      <main>
        <section className="page-container empty-state">
          <h2>Story not found.</h2>
          <p>{state.error || 'This story is not published yet.'}</p>
          <button className="pill-button" type="button" onClick={() => navigate('/')}>Back home</button>
        </section>
        <SiteFooter />
      </main>
    )
  }

  const { article } = state

  return (
    <main>
      <article className="article-detail">
        <header className="article-detail-header page-container">
          <div className="eyebrow-row">
            <span>{article.category}</span>
            <span aria-hidden="true">&middot;</span>
            <span>{article.readTime}</span>
            <span aria-hidden="true">&middot;</span>
            <span>{formatCount(article.stats?.viewCount)} views</span>
          </div>
          <h1>{article.title}</h1>
          <p>{article.summary}</p>
          <div className="article-detail-meta">
            <AuthorBadge author={article.author} navigate={navigate} />
            <div className="meta-row">
              <ArticleMeta article={article} />
              <BookmarkButton articleId={article.id} saved={article.bookmarked} requestWithAuth={requestWithAuth} notify={notify} />
            </div>
          </div>
        </header>
        <img alt="" className="article-detail-cover" src={article.image} />
        <section className="article-content page-container">
          <MarkdownPreview content={article.content} />
        </section>
      </article>
      <SiteFooter />
    </main>
  )
}
