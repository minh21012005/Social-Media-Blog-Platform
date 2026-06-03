import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { listPublishedArticles } from '../services/articles'

export function SearchPage({ query, navigate }) {
  const [page, setPage] = useState(0)
  const [state, setState] = useState({ loading: true, articles: [], error: '', page: 0, totalPages: 0, totalItems: 0 })

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = query
          ? await listPublishedArticles({ q: query, page, size: 12, sort: 'latest' })
          : { items: [], page: 0, totalPages: 0, totalItems: 0 }
        if (active) {
          setState({
            loading: false,
            articles: result.items,
            error: '',
            page: result.page || 0,
            totalPages: result.totalPages || 0,
            totalItems: result.totalItems || 0,
          })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, articles: [], error: error.message, page: 0, totalPages: 0, totalItems: 0 })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [page, query])

  return (
    <main>
      <section className="search-hero page-container">
        <span className="form-eyebrow">Search</span>
        <h1>{query ? `Results for "${query}"` : 'Search Chronicle.'}</h1>
        <p>
          {query
            ? `${state.totalItems} ${state.totalItems === 1 ? 'story' : 'stories'} found across published articles.`
            : 'Use the search icon above to find stories by title, summary, or content.'}
        </p>
      </section>

      <section className="category-grid page-container">
        {state.loading && <div className="loading-state">Searching stories...</div>}
        {state.error && <div className="empty-state"><h2>Search failed.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && (
          <ArticleList
            articles={state.articles}
            emptyText="No published stories matched this search."
            navigate={navigate}
          />
        )}
      </section>

      <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
      <SiteFooter />
    </main>
  )
}
