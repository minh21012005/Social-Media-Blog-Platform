import { useEffect, useState } from 'react'
import { categories } from '../data/editorial'
import { listEditorPicks, listFeaturedArticles, listPublishedArticles, listTrendingArticles } from '../services/articles'
import { ArrowRightIcon } from '../components/icons'
import { ArticleCard } from '../components/ArticleCard'
import { Newsletter } from '../components/Newsletter'
import { SiteFooter } from '../components/SiteFooter'

export function HomePage({ navigate }) {
  const [state, setState] = useState({ loading: true, featured: null, editorPicks: [], trending: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const [featuredArticles, editorPickArticles, trendingArticles, latestPage] = await Promise.all([
          listFeaturedArticles({ size: 1 }),
          listEditorPicks({ size: 5 }),
          listTrendingArticles({ size: 8 }).catch(() => []),
          listPublishedArticles({ size: 10, sort: 'latest' }).catch(() => ({ items: [] })),
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

          let trending = trendingArticles.filter((article) => !usedIds.has(article.id))
          if (trending.length < 2) {
            const latestArticles = (latestPage?.items || []).filter(
              (article) => !usedIds.has(article.id) && !trending.some((t) => t.id === article.id)
            )
            trending = [...trending, ...latestArticles]
          }
          trending = trending.slice(0, 6)
          
          setState({ loading: false, featured, editorPicks, trending, error: '' })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, featured: null, editorPicks: [], trending: [], error: error.message })
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

  const { featured, editorPicks, trending } = state

  return (
    <main>
      {state.loading && <section className="page-container loading-state">Loading latest stories...</section>}
      {state.error && <section className="page-container empty-state"><h2>Stories could not be loaded.</h2><p>{state.error}</p></section>}

      {!state.loading && !state.error && featured && (
        <section className="hero-section page-container">
          <img 
            alt="" 
            className="hero-image" 
            src={featured.image} 
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(featured.path)}
          />
          <article className="hero-copy">
            <div className="eyebrow-row">
              <span>{featured.category}</span>
              <span aria-hidden="true">&middot;</span>
              <span>{featured.date}</span>
            </div>
            <h1 
              style={{ cursor: 'pointer' }} 
              onClick={() => navigate(featured.path)}
            >
              {featured.title}
            </h1>
            <p>{featured.summary}</p>
            <div 
              className="author-line"
              style={{ cursor: 'pointer' }}
              onClick={(e) => {
                e.preventDefault()
                e.stopPropagation()
                navigate(`/author/${featured.author.username}`)
              }}
            >
              <img alt="" src={featured.author.avatar} />
              <div>
                <strong>{featured.author.name}</strong>
                <span>{featured.readTime}</span>
              </div>
            </div>
          </article>
        </section>
      )}

      {!state.loading && !state.error && !featured && (
        <section className="page-container empty-state">
          <h2>No published stories yet.</h2>
          <p>Create and publish the first Chronicle story from the writer desk.</p>
          <button className="pill-button" type="button" onClick={() => navigate('/write')}>Start writing</button>
        </section>
      )}

      {editorPicks.length > 0 && (
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
              {editorPicks.map((article) => (
                <ArticleCard article={article} key={article.id} navigate={navigate} />
              ))}
            </div>
          </div>
        </section>
      )}

      <div className="homepage-bottom-grid page-container">
        <div className="homepage-main-column">
          {trending.length > 0 && (
            <section className="latest-section">
              <h2>Trending Now</h2>
              <div className="story-list" style={{ display: 'grid', gap: '32px', marginBottom: '32px' }}>
                {trending.map((article) => (
                  <ArticleCard article={article} key={article.id} navigate={navigate} variant="horizontal" />
                ))}
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
