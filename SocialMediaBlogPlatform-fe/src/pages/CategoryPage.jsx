import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { categories } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'

export function CategoryPage({ slug, navigate }) {
  const category = categories.find((item) => item.slug === slug) ?? categories[0]
  const [query, setQuery] = useState('')
  const [sort, setSort] = useState('latest')
  const [page, setPage] = useState(0)
  const [state, setState] = useState({ loading: true, articles: [], error: '', page: 0, totalPages: 0 })

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = await listPublishedArticles({ category: category.slug, q: query, sort, page, size: 12 })
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
  }, [category.slug, page, query, sort])

  return (
    <main>
      <section className="category-hero">
        <h1>{category.label}</h1>
        <p>Exploring the latest trends, deep dives, and expert perspectives in {category.label.toLowerCase()}.</p>
      </section>

      <section className="category-controls page-container">
        <input
          aria-label={`Search ${category.label} stories`}
          placeholder={`Search ${category.label.toLowerCase()} stories`}
          type="search"
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
            setPage(0)
          }}
        />
        <select
          aria-label="Sort stories"
          value={sort}
          onChange={(event) => {
            setSort(event.target.value)
            setPage(0)
          }}
        >
          <option value="latest">Latest</option>
          <option value="views">Most viewed</option>
          <option value="popular">Popular</option>
        </select>
      </section>

      <section className="category-grid page-container">
        {state.loading && <div className="loading-state">Loading {category.label.toLowerCase()} stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load this category.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && (
          <ArticleList
            articles={state.articles}
            emptyText="Once published, articles in this category will appear here."
            navigate={navigate}
          />
        )}
      </section>

      <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
      <SiteFooter />
    </main>
  )
}
