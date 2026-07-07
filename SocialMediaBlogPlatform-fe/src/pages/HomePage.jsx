import { useEffect, useState } from 'react'
import { categories } from '../data/editorial'
import { listEditorPicks, listFeaturedArticles, listPublishedArticles, listTrendingArticles } from '../services/articles'
import { ArrowRightIcon } from '../components/icons'
import { ArticleCard } from '../components/ArticleCard'
import { Newsletter } from '../components/Newsletter'
import { SiteFooter } from '../components/SiteFooter'

export function HomePage({ navigate, mutedUserIds = new Set() }) {
  const [state, setState] = useState({ loading: true, featured: null, editorPicks: [], trending: [], latest: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const [featuredArticles, editorPickArticles, trendingArticles, latestPage] = await Promise.all([
          listFeaturedArticles({ size: 1 }),
          listEditorPicks({ size: 5 }),
          listTrendingArticles({ size: 6 }).catch(() => []),
          listPublishedArticles({ size: 8, sort: 'latest' }),
        ])
        if (active) {
          const [featured] = featuredArticles
          const usedIds = new Set(featured ? [featured.id] : [])

          let editorPicks = editorPickArticles.filter((article) => !usedIds.has(article.id))
          if (editorPicks.length < 2) {
            editorPicks = editorPickArticles
          }
          editorPicks = editorPicks.slice(0, 2)
          editorPicks.forEach((article) => usedIds.add(article.id))

          const trending = trendingArticles.filter((article) => !usedIds.has(article.id)).slice(0, 6)
          trending.forEach((article) => usedIds.add(article.id))

          let latest = latestPage.items.filter((article) => !usedIds.has(article.id))
          if (latest.length === 0) {
            latest = latestPage.items
          }
          latest = latest.slice(0, 2)

          setState({ loading: false, featured, editorPicks, trending, latest, error: '' })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, featured: null, editorPicks: [], latest: [], error: error.message })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [])

  const open = (path) => (event) => {
    event.preventDefault()
    navigate(path)
  }

  const { featured, editorPicks, trending, latest } = state

  return (
    <main>
      {state.loading && <section className="page-container loading-state">Loading latest stories...</section>}
      {state.error && <section className="page-container empty-state"><h2>Stories could not be loaded.</h2><p>{state.error}</p></section>}

      {!state.loading && !state.error && filteredFeatured && (
        <section className="hero-section page-container">
          <img
            alt=""
            className="hero-image"
            src={filteredFeatured.image}
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(filteredFeatured.path)}
          />
          <article className="hero-copy">
            <div className="eyebrow-row">
              <span>{filteredFeatured.category}</span>
              <span aria-hidden="true">&middot;</span>
              <span>{filteredFeatured.date}</span>
            </div>
            <h1
              style={{ cursor: 'pointer' }}
              onClick={() => navigate(filteredFeatured.path)}
            >
              {filteredFeatured.title}
            </h1>
            <p>{filteredFeatured.summary}</p>
            <div
              className="author-line"
              style={{ cursor: 'pointer' }}
              onClick={(e) => {
                e.preventDefault()
                e.stopPropagation()
                navigate(`/author/${filteredFeatured.author.username}`)
              }}
            >
              <img alt="" src={filteredFeatured.author.avatar} />
              <div>
                <strong>{filteredFeatured.author.name}</strong>
                <span>{filteredFeatured.readTime}</span>
              </div>
            </div>
          </article>
        </section>
      )}

      {!state.loading && !state.error && !filteredFeatured && (
        <section className="page-container empty-state">
          <h2>No published stories yet.</h2>
          <p>Create and publish the first Chronicle story from the writer desk.</p>
          <button className="pill-button" type="button" onClick={() => navigate('/write')}>Start writing</button>
        </section>
      )}

      {filteredEditorPicks.length > 0 && (
        <section className="section-band">
          <div className="page-container">
            <div className="section-heading">
              <h2>Editor&apos;s Picks</h2>
              <a href="/editors-picks" onClick={open('/editors-picks')}>
                View all
                <ArrowRightIcon />
              </a>
            </div>
            <div className="editor-grid">
              {filteredEditorPicks.map((article) => (
                <ArticleCard article={article} key={article.id} navigate={navigate} />
              ))}
            </div>
          </div>
        </section>
      )}

      {trending.length > 0 && (
        <section className="section-band trending-band">
          <div className="page-container">
            <div className="section-heading">
              <h2>Trending Now 🔥</h2>
              <a href="/articles" onClick={open('/articles')}>
                View all
                <ArrowRightIcon />
              </a>
            </div>
            <div className="trending-grid">
              {trending.map((article, index) => (
                <div className="trending-card" key={article.id} onClick={() => navigate(article.path)}>
                  <span className="trending-rank">#{index + 1}</span>
                  <div className="trending-card-body">
                    <span className="trending-category">{article.category}</span>
                    <h3 className="trending-title">{article.title}</h3>
                    <div className="trending-meta">
                      <img alt="" className="trending-avatar" src={article.author.avatar} />
                      <span>{article.author.name}</span>
                      <span aria-hidden="true">·</span>
                      <span>{article.readTime}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      <div className="homepage-bottom-grid page-container">
        <div className="homepage-main-column">
          {filteredLatest.length > 0 && (
            <section className="latest-section">
              <h2>Latest Stories</h2>
              <div className="story-list" style={{ display: 'grid', gap: '32px', marginBottom: '32px' }}>
                {filteredLatest.map((article) => (
                  <ArticleCard article={article} key={article.id} navigate={navigate} variant="horizontal" />
                ))}
              </div>
              <div style={{ textAlign: 'center' }}>
                <button className="outline-pill" type="button" onClick={() => navigate('/category/design')}>Load More Stories</button>
              </div>
            </section>
          )}
        </div>
        <div className="homepage-sidebar">
          <Newsletter />
          <section className="topics-section">
            <h2>Popular Topics</h2>
            <div className="topic-list" style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {categories.map((category) => (
                <a href={`/category/${category.slug}`} key={category.slug} onClick={open(`/category/${category.slug}`)} style={{ border: '1px solid var(--border)', padding: '6px 12px', borderRadius: '4px', textDecoration: 'none', color: 'var(--ink)', fontSize: '14px', whiteSpace: 'nowrap' }}>
                  {category.label}
                </a>
              ))}
            </div>
          </section>
        </div>
      </div>

      <SiteFooter />
    </main>
  )
}
