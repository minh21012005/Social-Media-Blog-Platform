import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { SiteFooter } from '../components/SiteFooter'
import { listTrendingArticles } from '../services/articles'

export function TrendingPage({ navigate, mutedUserIds = new Set() }) {
  const [state, setState] = useState({ loading: true, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = await listTrendingArticles({ size: 50 })
        if (active) {
          const filtered = result.filter((article) => !article.author || !mutedUserIds.has(article.author.id))
          setState({
            loading: false,
            articles: filtered,
            error: '',
          })
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
  }, [mutedUserIds])

  return (
    <main>
      <section className="category-hero">
        <h1>Trending Articles</h1>
        <p>The most read, shared, and discussed stories across our platform right now.</p>
      </section>

      <section className="page-container" style={{ paddingTop: '48px', paddingBottom: '78px' }}>
        {state.loading && <div className="loading-state">Loading trending stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load trending stories.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && (
          <ArticleList
            articles={state.articles}
            emptyText="No trending stories found. Check back later!"
            navigate={navigate}
          />
        )}
      </section>

      <SiteFooter />
    </main>
  )
}
