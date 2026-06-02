import { useEffect, useState } from 'react'
import { ArticleEditor } from '../components/ArticleEditor'
import { SiteFooter } from '../components/SiteFooter'
import { listMyArticles, publishArticle, updateArticle } from '../services/articles'

export function EditArticlePage({ articleId, session, requestWithAuth, navigate }) {
  const [state, setState] = useState({ loading: true, article: null, error: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const page = await requestWithAuth((token) => listMyArticles({ size: 50 }, token))
        const article = page.items.find((item) => item.id === articleId)
        if (active) {
          setState({
            loading: false,
            article,
            error: article ? '' : 'Article was not found in your dashboard.',
          })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, article: null, error: error.message })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [articleId, requestWithAuth])

  const save = async (payload) => {
    setSaving(true)
    try {
      const article = await requestWithAuth((token) => updateArticle(articleId, payload, token))
      setState({ loading: false, article, error: '' })
      navigate('/articles/me')
    } finally {
      setSaving(false)
    }
  }

  const saveAndPublish = async (payload) => {
    setSaving(true)
    try {
      await requestWithAuth((token) => updateArticle(articleId, payload, token))
      const published = await requestWithAuth((token) => publishArticle(articleId, token))
      navigate(`/articles/${published.slug}`)
    } finally {
      setSaving(false)
    }
  }

  return (
    <main>
      <section className="writer-hero page-container">
        <span className="form-eyebrow">Edit story</span>
        <h1>{state.article?.title || 'Refine the draft.'}</h1>
        <p>Update the article body, cover image, category, and publishing metadata.</p>
      </section>
      <section className="page-container writer-section">
        {state.loading && <div className="loading-state">Loading article...</div>}
        {state.error && <div className="empty-state"><h2>Could not open editor.</h2><p>{state.error}</p></div>}
        {state.article && (
          <ArticleEditor
            initialArticle={state.article}
            onSave={save}
            onPublish={saveAndPublish}
            requestWithAuth={requestWithAuth}
            saving={saving}
            token={session.accessToken}
          />
        )}
      </section>
      <SiteFooter />
    </main>
  )
}
