import { useEffect, useState } from 'react'
import './AdminDashboardPage.css'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { categories } from '../data/editorial'
import { curateArticle, listEditorPicks, listFeaturedArticles, listPublishedArticles } from '../services/articles'

const EMPTY_PAGE = { loading: true, articles: [], error: '', page: 0, totalPages: 0, totalItems: 0 }
const EMPTY_CURATION = { loading: true, featured: null, editorPicks: [], error: '' }

export function AdminDashboardPage({ requestWithAuth, notify }) {
  const [page, setPage] = useState(0)
  const [state, setState] = useState(EMPTY_PAGE)
  const [activeCurations, setActiveCurations] = useState(EMPTY_CURATION)
  const [searchInput, setSearchInput] = useState('')
  const [filters, setFilters] = useState({ query: '', category: '', sort: 'latest' })
  const [updatingId, setUpdatingId] = useState('')
  const [confirmData, setConfirmData] = useState(null)

  useEffect(() => {
    let active = true

    async function loadCurations() {
      try {
        const [featuredRows, editorRows] = await Promise.all([
          listFeaturedArticles({ size: 20 }),
          listEditorPicks({ size: 20 }),
        ])
        if (!active) return
        setActiveCurations({
          loading: false,
          featured: featuredRows.find((article) => article.featuredRank != null) || null,
          editorPicks: editorRows
            .filter((article) => article.editorPickRank != null)
            .sort((left, right) => left.editorPickRank - right.editorPickRank),
          error: '',
        })
      } catch (error) {
        if (active) {
          setActiveCurations({ loading: false, featured: null, editorPicks: [], error: error.message })
        }
      }
    }

    loadCurations()
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true

    async function loadArticles() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = await listPublishedArticles({
          page,
          size: 10,
          q: filters.query,
          category: filters.category,
          sort: filters.sort,
        })
        if (!active) return
        setState({
          loading: false,
          articles: result.items || [],
          error: '',
          page: result.page || 0,
          totalPages: result.totalPages || 0,
          totalItems: result.totalItems || 0,
        })
      } catch (error) {
        if (active) {
          setState({ ...EMPTY_PAGE, loading: false, error: error.message })
        }
      }
    }

    loadArticles()
    return () => {
      active = false
    }
  }, [filters, page])

  const reload = async () => {
    const [result, featuredRows, editorRows] = await Promise.all([
      listPublishedArticles({
        page,
        size: 10,
        q: filters.query,
        category: filters.category,
        sort: filters.sort,
      }),
      listFeaturedArticles({ size: 20 }),
      listEditorPicks({ size: 20 }),
    ])

    setState({
      loading: false,
      articles: result.items || [],
      error: '',
      page: result.page || 0,
      totalPages: result.totalPages || 0,
      totalItems: result.totalItems || 0,
    })
    setActiveCurations({
      loading: false,
      featured: featuredRows.find((article) => article.featuredRank != null) || null,
      editorPicks: editorRows
        .filter((article) => article.editorPickRank != null)
        .sort((left, right) => left.editorPickRank - right.editorPickRank),
      error: '',
    })
  }

  const saveCuration = async (article, payload, successMessage) => {
    if (!article || updatingId) return
    setUpdatingId(article.id)
    try {
      await requestWithAuth((token) => curateArticle(article.id, payload, token))
      await reload()
      notify?.(successMessage, { title: 'Curation saved', type: 'success' })
    } catch (error) {
      notify?.(error.message || 'Could not update curation.', { title: 'Curation failed', type: 'error' })
    } finally {
      setUpdatingId('')
    }
  }

  const requestFeaturedToggle = (article) => {
    const isFeatured = article.featuredRank === 1
    if (isFeatured) {
      setConfirmData({
        title: 'Remove Featured story?',
        message: `“${article.title}” will no longer appear in the Featured position.`,
        confirmLabel: 'Remove Featured',
        onConfirm: () => saveCuration(article, { featuredRank: null, editorPickRank: article.editorPickRank }, 'Article removed from Featured.'),
      })
      return
    }

    const current = activeCurations.featured
    setConfirmData({
      title: current ? 'Replace Featured story?' : 'Set as Featured story?',
      message: current
        ? `“${current.title}” currently holds the Featured position. It will be replaced by “${article.title}”.`
        : `“${article.title}” will become the Featured story.`,
      confirmLabel: current ? 'Replace story' : 'Set Featured',
      onConfirm: () => saveCuration(article, { featuredRank: 1, editorPickRank: null }, 'Featured story updated.'),
    })
  }

  const requestEditorPickChange = (article, rawRank) => {
    const nextRank = rawRank === '' ? null : Number(rawRank)
    if (nextRank === article.editorPickRank) return

    const current = nextRank == null
      ? null
      : activeCurations.editorPicks.find((item) => item.editorPickRank === nextRank && item.id !== article.id)

    setConfirmData({
      title: nextRank == null ? 'Remove Editor’s Pick?' : current ? `Replace Editor’s Pick #${nextRank}?` : `Assign Editor’s Pick #${nextRank}?`,
      message: nextRank == null
        ? `“${article.title}” will be removed from Editor’s Picks.`
        : current
          ? `“${current.title}” currently holds rank #${nextRank}. It will be replaced by “${article.title}”.`
          : `“${article.title}” will be assigned Editor’s Pick rank #${nextRank}.`,
      confirmLabel: nextRank == null ? 'Remove pick' : current ? 'Replace story' : 'Assign rank',
      onConfirm: () => saveCuration(
        article,
        { featuredRank: null, editorPickRank: nextRank },
        nextRank == null ? 'Article removed from Editor’s Picks.' : `Editor’s Pick #${nextRank} updated.`,
      ),
    })
  }

  const submitSearch = (event) => {
    event.preventDefault()
    setPage(0)
    setFilters((current) => ({ ...current, query: searchInput.trim() }))
  }

  const updateFilter = (field) => (event) => {
    setPage(0)
    setFilters((current) => ({ ...current, [field]: event.target.value }))
  }

  const clearFilters = () => {
    setSearchInput('')
    setPage(0)
    setFilters({ query: '', category: '', sort: 'latest' })
  }

  const hasFilters = Boolean(filters.query || filters.category || filters.sort !== 'latest')
  const editorSlots = Array.from({ length: 5 }, (_, index) => {
    const rank = index + 1
    return { rank, article: activeCurations.editorPicks.find((item) => item.editorPickRank === rank) || null }
  })

  return (
    <main className="admin-dashboard-page">
      <section className="admin-page-hero page-container">
        <div>
          <span className="dashboard-kicker">Admin console</span>
          <h1>Editorial curation</h1>
          <p>Manage the homepage lineup, then search the published library for the next story to promote.</p>
        </div>
        <span className="admin-library-count">{state.totalItems} published</span>
      </section>

      <section className="page-container admin-active-section" aria-labelledby="active-curation-title">
        <div className="admin-section-heading">
          <div>
            <h2 id="active-curation-title">Active curation</h2>
            <p>Every homepage position is visible here, regardless of where the article appears in the library.</p>
          </div>
        </div>

        {activeCurations.loading && <div className="loading-state">Loading active curation...</div>}
        {activeCurations.error && <div className="empty-state"><h2>Could not load active curation.</h2><p>{activeCurations.error}</p></div>}
        {!activeCurations.loading && !activeCurations.error && (
          <div className="curation-slot-grid">
            <CurationSlot
              article={activeCurations.featured}
              busy={updatingId === activeCurations.featured?.id}
              label="Featured"
              onRemove={() => requestFeaturedToggle(activeCurations.featured)}
            />
            {editorSlots.map(({ rank, article }) => (
              <CurationSlot
                article={article}
                busy={updatingId === article?.id}
                key={rank}
                label={`Editor’s Pick #${rank}`}
                onRemove={() => requestEditorPickChange(article, '')}
              />
            ))}
          </div>
        )}
      </section>

      <section className="page-container admin-library-section" aria-labelledby="published-library-title">
        <div className="admin-section-heading">
          <div>
            <h2 id="published-library-title">Published library</h2>
            <p>Newest stories appear first. Search or filter to curate an older article.</p>
          </div>
        </div>

        <div className="admin-library-toolbar">
          <form className="admin-search-form" onSubmit={submitSearch}>
            <input
              aria-label="Search published articles"
              placeholder="Search title, summary, content, or tag"
              type="search"
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
            />
            <button type="submit">Search</button>
          </form>
          <select aria-label="Filter by category" value={filters.category} onChange={updateFilter('category')}>
            <option value="">All categories</option>
            {categories.map((category) => <option key={category.slug} value={category.slug}>{category.label}</option>)}
          </select>
          <select aria-label="Sort published articles" value={filters.sort} onChange={updateFilter('sort')}>
            <option value="latest">Newest</option>
            <option value="views">Most viewed</option>
            <option value="popular">Most popular</option>
          </select>
          {hasFilters && <button className="admin-clear-filters" type="button" onClick={clearFilters}>Clear filters</button>}
        </div>

        {state.loading && <div className="loading-state">Loading published articles...</div>}
        {state.error && <div className="empty-state"><h2>Could not load articles.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && state.articles.length === 0 && (
          <div className="empty-state"><h2>No matching published articles.</h2><p>Try another search term or clear the current filters.</p></div>
        )}

        {!state.loading && !state.error && state.articles.length > 0 && (
          <div className="admin-curation-list">
            {state.articles.map((article) => (
              <article className="admin-curation-row" key={article.id}>
                <button
                  aria-label={article.featuredRank === 1 ? 'Remove from Featured' : 'Set as Featured'}
                  className={`admin-feature-toggle${article.featuredRank === 1 ? ' is-active' : ''}`}
                  disabled={Boolean(updatingId)}
                  title={article.featuredRank === 1 ? 'Remove from Featured' : 'Set as Featured'}
                  type="button"
                  onClick={() => requestFeaturedToggle(article)}
                >
                  <span aria-hidden="true">{article.featuredRank === 1 ? '\u2605' : '\u2606'}</span>
                </button>
                <img alt="" src={article.image} />
                <div className="admin-curation-copy">
                  <span className="article-category">{article.category}</span>
                  <h3>{article.title}</h3>
                  <p>By <strong>{article.author.name}</strong> · {article.date}</p>
                  <div className="admin-curation-badges">
                    {article.featuredRank === 1 && <span className="curation-badge featured">Featured</span>}
                    {article.editorPickRank != null && <span className="curation-badge editor-pick">Editor’s Pick #{article.editorPickRank}</span>}
                  </div>
                </div>
                <div className="curation-controls">
                  <label>
                    Editor’s Pick
                    <select
                      disabled={Boolean(updatingId)}
                      value={article.editorPickRank ?? ''}
                      onChange={(event) => requestEditorPickChange(article, event.target.value)}
                    >
                      <option value="">Not selected</option>
                      {[1, 2, 3, 4, 5].map((rank) => <option key={rank} value={rank}>Rank #{rank}</option>)}
                    </select>
                  </label>
                </div>
              </article>
            ))}
          </div>
        )}

        <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
      </section>
      <SiteFooter />

      {confirmData && (
        <div className="curation-confirm-backdrop" role="presentation" onClick={() => setConfirmData(null)}>
          <div aria-labelledby="curation-confirm-title" aria-modal="true" className="curation-confirm-dialog" role="dialog" onClick={(event) => event.stopPropagation()}>
            <p className="curation-confirm-kicker">Confirm curation</p>
            <h2 id="curation-confirm-title">{confirmData.title}</h2>
            <p>{confirmData.message}</p>
            <div className="curation-confirm-actions">
              <button className="text-button muted" type="button" onClick={() => setConfirmData(null)}>Cancel</button>
              <button
                className="curation-confirm-confirm"
                type="button"
                onClick={() => {
                  const action = confirmData.onConfirm
                  setConfirmData(null)
                  action()
                }}
              >
                {confirmData.confirmLabel || 'Confirm'}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  )
}

function CurationSlot({ article, busy, label, onRemove }) {
  return (
    <article className={`curation-slot${article ? ' is-filled' : ' is-empty'}`}>
      <span className="curation-slot-label">{label}</span>
      {article ? (
        <>
          <img alt="" src={article.image} />
          <div>
            <strong>{article.title}</strong>
            <span>{article.author.name}</span>
          </div>
          <button disabled={busy} type="button" onClick={onRemove}>{busy ? 'Updating...' : 'Remove'}</button>
        </>
      ) : (
        <p>No story assigned</p>
      )}
    </article>
  )
}