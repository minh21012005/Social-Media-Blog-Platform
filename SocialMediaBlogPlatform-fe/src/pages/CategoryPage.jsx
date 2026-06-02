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
  const [state, setState] = useState({ loading: true, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const page = await listPublishedArticles({ category: category.slug, q: query, sort, size: 12 })
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
  }, [category.slug, query, sort])

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
          onChange={(event) => setQuery(event.target.value)}
        />
        <select aria-label="Sort stories" value={sort} onChange={(event) => setSort(event.target.value)}>
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

      {state.articles.length > 0 && <Pagination />}
      <SiteFooter />
    </main>
  )
}
