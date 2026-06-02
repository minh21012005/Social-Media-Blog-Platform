import { useEffect, useState } from 'react'
import { categories } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'
import { ArrowRightIcon } from '../components/icons'
import { ArticleCard } from '../components/ArticleCard'
import { Newsletter } from '../components/Newsletter'
import { SiteFooter } from '../components/SiteFooter'

export function HomePage({ navigate }) {
  const [state, setState] = useState({ loading: true, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const page = await listPublishedArticles({ size: 8 })
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
  }, [])

  const open = (path) => (event) => {
    event.preventDefault()
    navigate(path)
  }

  const [featured, ...rest] = state.articles
  const editorPicks = rest.slice(0, 2)
  const latest = rest.slice(2, 6)

  return (
    <main>
      {state.loading && <section className="page-container loading-state">Loading latest stories...</section>}
      {state.error && <section className="page-container empty-state"><h2>Stories could not be loaded.</h2><p>{state.error}</p></section>}

      {!state.loading && !state.error && featured && (
        <section className="hero-section page-container">
          <img alt="" className="hero-image" src={featured.image} />
          <article className="hero-copy">
            <div className="eyebrow-row">
              <span>{featured.category}</span>
              <span aria-hidden="true">&middot;</span>
              <span>{featured.date}</span>
            </div>
            <h1>{featured.title}</h1>
            <p>{featured.summary}</p>
            <div className="author-line">
              <img alt="" src={featured.author.avatar} />
              <div>
                <strong>{featured.author.name}</strong>
                <span>{featured.readTime}</span>
              </div>
            </div>
            <button className="outline-pill inline-pill" type="button" onClick={() => navigate(featured.path)}>
              Read story
            </button>
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
              <a href="/category/design" onClick={open('/category/design')}>
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

      {latest.length > 0 && (
        <section className="latest-section page-container">
          <h2>Latest Stories</h2>
          <div className="story-list">
            {latest.map((article) => (
              <ArticleCard article={article} key={article.id} navigate={navigate} variant="horizontal" />
            ))}
          </div>
          <button className="outline-pill" type="button" onClick={() => navigate('/category/design')}>Browse Stories</button>
        </section>
      )}

      <Newsletter />

      <section className="topics-section page-container">
        <h2>Popular Topics</h2>
        <div className="topic-list">
          {categories.map((category) => (
            <a href={`/category/${category.slug}`} key={category.slug} onClick={open(`/category/${category.slug}`)}>
              {category.label}
            </a>
          ))}
        </div>
      </section>

      <SiteFooter />
    </main>
  )
}
