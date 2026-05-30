import { useMemo, useState } from 'react'
import { categories } from '../data/editorial'
import { uploadArticleMedia } from '../services/articles'
import { MarkdownPreview } from './MarkdownPreview'
import { MediaUploader } from './MediaUploader'

const emptyArticle = {
  title: '',
  slug: '',
  category: 'design',
  summary: '',
  content: '',
  coverImageUrl: '',
  tags: '',
}

export function ArticleEditor({ initialArticle, token, saving, onSave, onPublish }) {
  const [form, setForm] = useState(() => ({
    ...emptyArticle,
    ...initialArticle,
    tags: Array.isArray(initialArticle?.tags) ? initialArticle.tags.join(', ') : initialArticle?.tags || '',
  }))
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  const payload = useMemo(() => ({
    title: form.title,
    slug: form.slug,
    category: form.category,
    summary: form.summary,
    content: form.content,
    coverImageUrl: form.coverImageUrl,
    tags: form.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
  }), [form])

  const update = (field) => (event) => {
    const value = event.target.value
    setForm((current) => ({
      ...current,
      [field]: value,
      ...(field === 'title' && !current.slug ? { slug: slugify(value) } : {}),
    }))
  }

  const upload = async (file) => {
    setError('')
    setUploading(true)
    try {
      const media = await uploadArticleMedia(file, token)
      setForm((current) => ({ ...current, coverImageUrl: media.secureUrl }))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploading(false)
    }
  }

  const save = async (event) => {
    event.preventDefault()
    setError('')
    try {
      await onSave(payload)
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  const publish = async () => {
    setError('')
    try {
      await onPublish(payload)
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <form className="editor-layout" onSubmit={save}>
      <section className="editor-form">
        <label>
          Title
          <input required value={form.title} onChange={update('title')} />
        </label>
        <div className="editor-row">
          <label>
            Slug
            <input pattern="[a-z0-9-]{3,120}" required value={form.slug} onChange={update('slug')} />
          </label>
          <label>
            Category
            <select value={form.category} onChange={update('category')}>
              {categories.map((category) => (
                <option key={category.slug} value={category.slug}>{category.label}</option>
              ))}
            </select>
          </label>
        </div>
        <label>
          Summary
          <textarea maxLength="500" required rows="3" value={form.summary} onChange={update('summary')} />
        </label>
        <label>
          Cover image URL
          <input value={form.coverImageUrl} onChange={update('coverImageUrl')} />
        </label>
        {form.coverImageUrl && <img alt="" className="editor-cover-preview" src={form.coverImageUrl} />}
        <MediaUploader busy={uploading} label="Upload cover image" onUpload={upload} />
        <label>
          Tags
          <input placeholder="design, web, culture" value={form.tags} onChange={update('tags')} />
        </label>
        <label>
          Markdown content
          <textarea className="editor-content" required rows="16" value={form.content} onChange={update('content')} />
        </label>
        {error && <p className="form-error">{error}</p>}
        <div className="editor-actions">
          <button className="submit-button" disabled={saving} type="submit">
            {saving ? 'Saving...' : 'Save draft'}
          </button>
          {onPublish && (
            <button className="outline-pill inline-pill" disabled={saving} type="button" onClick={publish}>
              Publish
            </button>
          )}
        </div>
      </section>
      <aside className="editor-preview">
        <span className="form-eyebrow">Preview</span>
        <h2>{form.title || 'Untitled article'}</h2>
        <p>{form.summary || 'A concise summary helps readers decide where to spend their attention.'}</p>
        <MarkdownPreview content={form.content} />
      </aside>
    </form>
  )
}

function slugify(value) {
  return value
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 120)
}
