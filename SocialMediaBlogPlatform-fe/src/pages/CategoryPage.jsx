import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { categories } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'

export function CategoryPage({ slug, navigate }) {
  const category = categories.find((item) => item.slug === slug) ?? categories[0]
  const [state, setState] = useState({ loading: true, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const page = await listPublishedArticles({ category: category.slug, size: 12 })
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
  }, [category.slug])

  return (
    <main>
      <section className="category-hero">
        <h1>{category.label}</h1>
        <p>Exploring the latest trends, deep dives, and expert perspectives in {category.label.toLowerCase()}.</p>
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
