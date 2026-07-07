import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { SiteFooter } from '../components/SiteFooter'
import { listEditorPicks } from '../services/articles'

export function EditorsPicksPage({ navigate }) {
  const [state, setState] = useState({ loading: true, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = await listEditorPicks({ size: 50 })
        if (active) {
          const curatedOnly = result.filter(
            (article) => article.editorPickRank !== null && article.editorPickRank !== undefined
          )
          curatedOnly.sort((a, b) => a.editorPickRank - b.editorPickRank)
          setState({
            loading: false,
            articles: curatedOnly,
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
  }, [])

  return (
    <main>
      <section className="category-hero">
        <h1>Editor&apos;s Picks</h1>
        <p>Handpicked stories, insightful perspectives, and deep-dives recommended by our editorial team.</p>
      </section>

      <section className="page-container">
        {state.loading && <div className="loading-state">Loading curated stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load curated stories.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && (
          <ArticleList
            articles={state.articles}
            emptyText="No editor picks have been curated yet. Check back soon!"
            navigate={navigate}
          />
        )}
      </section>

      <SiteFooter />
    </main>
  )
}
