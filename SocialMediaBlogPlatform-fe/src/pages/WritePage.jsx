import { useState } from 'react'
import { ArticleEditor } from '../components/ArticleEditor'
import { SiteFooter } from '../components/SiteFooter'
import { createArticle, publishArticle } from '../services/articles'

export function WritePage({ session, requestWithAuth, navigate }) {
  const [saving, setSaving] = useState(false)

  const save = async (payload) => {
    setSaving(true)
    try {
      const article = await requestWithAuth((token) => createArticle(payload, token))
      navigate('/articles/me')
      return article
    } finally {
      setSaving(false)
    }
  }

  const saveAndPublish = async (payload) => {
    setSaving(true)
    try {
      const article = await requestWithAuth((token) => createArticle(payload, token))
      const published = await requestWithAuth((token) => publishArticle(article.id, token))
      navigate(`/articles/${published.slug}`)
    } finally {
      setSaving(false)
    }
  }

  return (
    <main>
      <section className="writer-hero page-container">
        <span className="form-eyebrow">Writer desk</span>
        <h1>Draft a new story.</h1>
        <p>Write in Markdown, upload a cover image, and publish when the article is ready.</p>
        <strong>{session.user.displayName}</strong>
      </section>
      <section className="page-container writer-section">
        <ArticleEditor onSave={save} onPublish={saveAndPublish} saving={saving} token={session.accessToken} />
      </section>
      <SiteFooter />
    </main>
  )
}
