import { useEffect, useState } from 'react'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { listPublishedArticles, curateArticle, listFeaturedArticles, listEditorPicks } from '../services/articles'

export function AdminDashboardPage({ requestWithAuth, navigate, notify }) {
  const [page, setPage] = useState(0)
  const [state, setState] = useState({ loading: true, articles: [], error: '', page: 0, totalPages: 0 })
  const [curationStates, setCurationStates] = useState({})
  const [updatingId, setUpdatingId] = useState('')
  const [confirmData, setConfirmData] = useState(null)

  // Track all currently curated articles across all pages to resolve duplicates
  const [activeCurations, setActiveCurations] = useState({ featured: [], editorPicks: [] })

  const loadActiveCurations = async () => {
    try {
      const [feat, edit] = await Promise.all([
        listFeaturedArticles({ size: 20 }),
        listEditorPicks({ size: 20 })
      ])
      setActiveCurations({ featured: feat, editorPicks: edit })
    } catch (err) {
      console.error('Failed to load active curations', err)
    }
  }

  useEffect(() => {
    loadActiveCurations()
  }, [])

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = await listPublishedArticles({ page, size: 10 })
        if (active) {
          setState({
            loading: false,
            articles: result.items,
            error: '',
            page: result.page || 0,
            totalPages: result.totalPages || 0,
          })
          const initialCuration = {}
          result.items.forEach(article => {
            initialCuration[article.id] = {
              featuredRank: article.featuredRank ?? '',
              editorPickRank: article.editorPickRank ?? ''
            }
          })
          setCurationStates(initialCuration)
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
  }, [page])

  const handleToggleFeatured = async (articleId) => {
    if (updatingId) return
    setUpdatingId(articleId)
    const currentCuration = curationStates[articleId] || { featuredRank: '', editorPickRank: '' }
    const isCurrentlyFeatured = String(currentCuration.featuredRank) === '1'
    const nextFeaturedRank = isCurrentlyFeatured ? null : 1
    const nextEditorPickRank = isCurrentlyFeatured
      ? (currentCuration.editorPickRank === '' ? null : parseInt(currentCuration.editorPickRank, 10))
      : null

    try {
      // 1. Resolve Duplicate Featured Rank (if toggling ON)
      if (nextFeaturedRank === 1) {
        const duplicate = activeCurations.featured.find(art => art.id !== articleId && art.featuredRank === 1) ||
          state.articles.find(art => art.id !== articleId && art.featuredRank === 1)
        if (duplicate) {
          await requestWithAuth((token) => curateArticle(duplicate.id, {
            featuredRank: null,
            editorPickRank: duplicate.editorPickRank
          }, token))
        }
      }

      // 2. Save the new curation ranks for current article
      const payload = {
        featuredRank: nextFeaturedRank,
        editorPickRank: nextEditorPickRank
      }
      await requestWithAuth((token) => curateArticle(articleId, payload, token))
      notify?.(
        nextFeaturedRank === 1 ? 'Article promoted to Featured.' : 'Article removed from Featured.',
        { title: 'Curation Saved', type: 'success' }
      )

      // Reload active curations list to keep sync
      await loadActiveCurations()

      // Reload current page to update all badges and inputs
      const result = await listPublishedArticles({ page, size: 10 })
      setState({
        loading: false,
        articles: result.items,
        error: '',
        page: result.page || 0,
        totalPages: result.totalPages || 0,
      })
      const updatedCuration = {}
      result.items.forEach(article => {
        updatedCuration[article.id] = {
          featuredRank: article.featuredRank ?? '',
          editorPickRank: article.editorPickRank ?? ''
        }
      })
      setCurationStates(updatedCuration)

    } catch (err) {
      notify?.(err.message || 'Failed to save curation settings.', { title: 'Error' })
    } finally {
      setUpdatingId('')
    }
  }

  const proceedWithEditorPickChange = async (article, nextEditorPickRank) => {
    const articleId = article.id
    setUpdatingId(articleId)
    try {
      // 1. Resolve Duplicate Editor Pick Rank (checking ranks 1 to 5)
      if (nextEditorPickRank !== null) {
        const duplicate = activeCurations.editorPicks.find(art => art.id !== articleId && art.editorPickRank === nextEditorPickRank) ||
          state.articles.find(art => art.id !== articleId && art.editorPickRank === nextEditorPickRank)
        if (duplicate) {
          await requestWithAuth((token) => curateArticle(duplicate.id, {
            featuredRank: duplicate.featuredRank,
            editorPickRank: null
          }, token))
        }
      }

      // 2. Save the new curation ranks (Featured rank is reset to null due to mutual exclusion)
      const payload = {
        featuredRank: null,
        editorPickRank: nextEditorPickRank
      }
      await requestWithAuth((token) => curateArticle(articleId, payload, token))
      notify?.('Editor Pick rank updated successfully.', { title: 'Curation Saved', type: 'success' })

      // Reload active curations list to keep sync
      await loadActiveCurations()

      // Reload current page to update all badges and inputs
      const result = await listPublishedArticles({ page, size: 10 })
      setState({
        loading: false,
        articles: result.items,
        error: '',
        page: result.page || 0,
        totalPages: result.totalPages || 0,
      })
      const updatedCuration = {}
      result.items.forEach(art => {
        updatedCuration[art.id] = {
          featuredRank: art.featuredRank ?? '',
          editorPickRank: art.editorPickRank ?? ''
        }
      })
      setCurationStates(updatedCuration)

    } catch (err) {
      notify?.(err.message || 'Failed to save curation settings.', { title: 'Error' })
      // Revert select UI state on error
      setCurationStates(prev => ({
        ...prev,
        [articleId]: {
          ...prev[articleId],
          editorPickRank: article.editorPickRank ?? ''
        }
      }))
    } finally {
      setUpdatingId('')
    }
  }

  const handleEditorPickChange = (article, rawValue) => {
    const articleId = article.id
    const currentRank = article.editorPickRank ?? ''
    const nextRankStr = rawValue

    if (String(currentRank) === nextRankStr) {
      return
    }

    const nextEditorPickRank = nextRankStr === '' ? null : parseInt(nextRankStr, 10)

    // Build confirmation message
    let message = ''
    if (nextEditorPickRank === null) {
      message = `Are you sure you want to remove "${article.title}" from Editor's Picks?`
    } else {
      const duplicate = activeCurations.editorPicks.find(art => art.id !== articleId && art.editorPickRank === nextEditorPickRank) ||
        state.articles.find(art => art.id !== articleId && art.editorPickRank === nextEditorPickRank)
      if (duplicate) {
        message = `Editor's Pick Rank ${nextEditorPickRank} is currently assigned to "${duplicate.title}". If you proceed, that article will be removed from Editor's Picks. Do you want to continue?`
      } else {
        message = `Are you sure you want to assign Editor's Pick Rank ${nextEditorPickRank} to "${article.title}"?`
      }
    }

    setConfirmData({
      title: 'Confirm Editor\'s Pick',
      message: message,
      onConfirm: () => {
        setConfirmData(null)
        proceedWithEditorPickChange(article, nextEditorPickRank)
      },
      onCancel: () => {
        setConfirmData(null)
        // Revert select UI state to current value
        setCurationStates(prev => ({
          ...prev,
          [articleId]: {
            ...prev[articleId],
            editorPickRank: article.editorPickRank ?? ''
          }
        }))
      }
    })
  }

  return (
    <main>
      <section className="writer-hero page-container">
        <span className="form-eyebrow">Admin Console</span>
        <h1>Article curation.</h1>
        <p>Promote stories to Featured slot or curate Editor&apos;s Picks to decorate the homepage.</p>
      </section>

      <section className="page-container dashboard-section">
        {state.loading && <div className="loading-state">Loading published articles...</div>}
        {state.error && <div className="empty-state"><h2>Could not load articles.</h2><p>{state.error}</p></div>}

        {!state.loading && !state.error && state.articles.length === 0 && (
          <div className="empty-state">
            <h2>No published articles.</h2>
            <p>Wait for authors to publish some stories first.</p>
          </div>
        )}

        {!state.loading && !state.error && state.articles.length > 0 && (
          <div className="article-table">
            {state.articles.map((article) => {
              const curation = curationStates[article.id] || { featuredRank: '', editorPickRank: '' }
              const isUpdating = updatingId === article.id

              return (
                <article className="article-table-row admin-curation-row" key={article.id} style={{ position: 'relative' }}>
                  <button
                    type="button"
                    onClick={() => handleToggleFeatured(article.id)}
                    disabled={isUpdating}
                    title={String(curation.featuredRank) === '1' ? 'Remove from Featured' : 'Promote to Featured'}
                    style={{
                      position: 'absolute',
                      top: '8px',
                      right: '8px',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer',
                      padding: '4px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: String(curation.featuredRank) === '1' ? '#f59e0b' : '#d1d5db',
                      fontSize: '24px',
                      transition: 'transform 0.2s ease, color 0.2s ease',
                      zIndex: 10,
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.2)'}
                    onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                  >
                    {String(curation.featuredRank) === '1' ? '★' : '☆'}
                  </button>

                  <img alt="" src={article.image} />
                  <div style={{ flex: 1 }}>
                    <span className="article-category">{article.category}</span>
                    <h3>{article.title}</h3>
                    <p style={{ margin: '4px 0 8px', fontSize: '14px', color: 'var(--muted)' }}>
                      By <strong>{article.author.name}</strong> &middot; {article.date}
                    </p>

                    <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
                      {article.featuredRank !== null && article.featuredRank !== undefined && (
                        <span className="curation-badge featured">Featured</span>
                      )}
                      {article.editorPickRank !== null && article.editorPickRank !== undefined && (
                        <span className="curation-badge editor-pick">Editor Pick #{article.editorPickRank}</span>
                      )}
                    </div>
                  </div>

                  <div className="curation-controls" style={{ display: 'flex', alignItems: 'center', minWidth: '220px', paddingRight: '24px' }}>
                    <div className="curation-input-group" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '10px', width: '100%' }}>
                      <span style={{ fontSize: '13px', color: 'var(--ink)' }}>Editor Pick Rank</span>
                      <select
                        style={{ padding: '6px 8px', borderRadius: '4px', border: '1px solid var(--border)', font: 'inherit', width: '110px' }}
                        value={curation.editorPickRank}
                        onChange={(e) => handleEditorPickChange(article, e.target.value)}
                        disabled={isUpdating}
                      >
                        <option value="">None</option>
                        <option value="1">Rank 1</option>
                        <option value="2">Rank 2</option>
                        <option value="3">Rank 3</option>
                        <option value="4">Rank 4</option>
                        <option value="5">Rank 5</option>
                      </select>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}

        <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
      </section>
      <SiteFooter />

      {confirmData && (
        <div className="curation-confirm-backdrop" role="presentation" onClick={confirmData.onCancel}>
          <div
            aria-labelledby="curation-confirm-title"
            aria-modal="true"
            className="curation-confirm-dialog"
            role="dialog"
            onClick={(event) => event.stopPropagation()}
          >
            <p className="curation-confirm-kicker">Confirm Curation</p>
            <h2 id="curation-confirm-title">{confirmData.title}</h2>
            <p>{confirmData.message}</p>
            <div className="curation-confirm-actions">
              <button className="text-button muted" type="button" onClick={confirmData.onCancel}>
                Cancel
              </button>
              <button className="curation-confirm-confirm" type="button" onClick={confirmData.onConfirm}>
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  )
}
