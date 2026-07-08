import { useMemo, useRef, useState } from 'react'
import { categories } from '../data/editorial'
import { uploadArticleMedia } from '../services/articles'
import { MarkdownPreview } from './MarkdownPreview'
import { MediaUploader } from './MediaUploader'

const emptyArticle = {
  title: '',
  category: 'design',
  summary: '',
  content: '',
  coverImageUrl: '',
  tags: '',
}

const maxTags = 5
const maxTagLength = 50
const maxContentImages = 10
const maxContentLength = 50000

export function ArticleEditor({ initialArticle, requestWithAuth, saving, onSave, onPublish, notify }) {
  const contentRef = useRef(null)
  const [form, setForm] = useState(() => ({
    ...emptyArticle,
    ...initialArticle,
    tags: Array.isArray(initialArticle?.tags) ? initialArticle.tags.join(', ') : initialArticle?.tags || '',
  }))
  const [showCoverUrlInput, setShowCoverUrlInput] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [contentImageUploading, setContentImageUploading] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  const tags = useMemo(() => parseTags(form.tags), [form.tags])
  const selectedCategory = useMemo(
    () => categories.find((category) => category.slug === form.category),
    [form.category],
  )

  const payload = useMemo(() => ({
    title: form.title,
    category: form.category,
    summary: form.summary,
    content: form.content,
    coverImageUrl: form.coverImageUrl,
    tags,
  }), [form, tags])

  const update = (field) => (event) => {
    const value = event.target.value
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
    setFieldErrors((current) => ({ ...current, [field]: '' }))
  }

  const upload = async (file) => {
    setError('')
    setFieldErrors((current) => ({ ...current, coverImageUrl: '' }))
    setUploading(true)
    try {
      const media = await requestWithAuth((accessToken) => uploadArticleMedia(file, accessToken))
      setForm((current) => ({ ...current, coverImageUrl: media.secureUrl }))
    } catch (requestError) {
      showRequestError(requestError)
    } finally {
      setUploading(false)
    }
  }

  const insertContentImage = async (file) => {
    setError('')
    if (markdownImageCount(form.content) >= maxContentImages) {
      const message = `Articles can include up to ${maxContentImages} images.`
      setFieldErrors((current) => ({ ...current, content: message }))
      setError(message)
      notify?.(message, { title: 'Image limit reached' })
      return
    }
    setContentImageUploading(true)
    try {
      const media = await requestWithAuth((accessToken) => uploadArticleMedia(file, accessToken))
      const imageMarkdown = `![Article image](${media.secureUrl})`
      const textarea = contentRef.current
      const start = textarea?.selectionStart ?? form.content.length
      const end = textarea?.selectionEnd ?? form.content.length
      const prefix = form.content.slice(0, start)
      const suffix = form.content.slice(end)
      const needsLeadingBreak = prefix && !prefix.endsWith('\n') ? '\n\n' : ''
      const needsTrailingBreak = suffix && !suffix.startsWith('\n') ? '\n\n' : ''
      const nextContent = `${prefix}${needsLeadingBreak}${imageMarkdown}${needsTrailingBreak}${suffix}`
      setForm((current) => ({ ...current, content: nextContent }))
      requestAnimationFrame(() => {
        const cursor = start + needsLeadingBreak.length + imageMarkdown.length
        contentRef.current?.focus()
        contentRef.current?.setSelectionRange(cursor, cursor)
      })
      notify?.('The image was inserted into your story and will render in Preview.', { title: 'Image inserted' })
    } catch (requestError) {
      showRequestError(requestError)
    } finally {
      setContentImageUploading(false)
    }
  }

  const save = async (event) => {
    event.preventDefault()
    setError('')
    if (!validate('save')) {
      return
    }
    try {
      await onSave(payload)
    } catch (requestError) {
      showRequestError(requestError)
    }
  }

  const publish = async () => {
    setError('')
    if (!validate('publish')) {
      return
    }
    try {
      await onPublish(payload)
    } catch (requestError) {
      showRequestError(requestError)
    }
  }

  const validate = (mode) => {
    const nextErrors = {}
    if (!form.title.trim()) {
      nextErrors.title = 'Title is required.'
    }
    if (!form.category) {
      nextErrors.category = 'Choose a category.'
    }
    const invalidTag = tags.find((tag) => tag.length > maxTagLength)
    if (tags.length > maxTags) {
      nextErrors.tags = `Use up to ${maxTags} focused tags.`
    } else if (invalidTag) {
      nextErrors.tags = `Tags must be ${maxTagLength} characters or fewer.`
    }
    if (mode === 'publish' && !form.summary.trim()) {
      nextErrors.summary = 'Summary is required before publishing.'
    }
    if (!form.content.trim()) {
      nextErrors.content = 'Write some Markdown content before saving.'
    } else if (form.content.trim().length > maxContentLength) {
      nextErrors.content = `Content must be ${maxContentLength.toLocaleString()} characters or fewer.`
    } else if (markdownImageCount(form.content) > maxContentImages) {
      nextErrors.content = `Articles can include up to ${maxContentImages} images.`
    }
    if (mode === 'publish' && !form.coverImageUrl.trim()) {
      nextErrors.coverImageUrl = 'Add a cover image before publishing.'
    }

    setFieldErrors(nextErrors)
    const [firstError] = Object.values(nextErrors)
    if (firstError) {
      setError(firstError)
      notify?.(firstError, { title: mode === 'publish' ? 'Publish needs one more thing' : 'Draft is missing details' })
      return false
    }
    return true
  }

  const showRequestError = (requestError) => {
    const message = friendlyArticleError(requestError.message)
    const mappedErrors = fieldErrorFromMessage(message)
    setFieldErrors((current) => ({ ...current, ...mappedErrors }))
    setError(message)
    notify?.(message, { title: 'Could not save article' })
  }

  return (
    <form className="editor-layout" noValidate onSubmit={save}>
      <section className="editor-form">
        <label>
          Title
          <input
            aria-invalid={Boolean(fieldErrors.title)}
            value={form.title}
            onChange={update('title')}
          />
          {fieldErrors.title && <span className="field-error">{fieldErrors.title}</span>}
        </label>
        <div className="editor-row">
          <div className="editor-field">
            <div className="editor-label-row">
              <label htmlFor="article-category">Category</label>
              {selectedCategory?.description && (
                <span className="category-info">
                  <button aria-label={`About ${selectedCategory.label}`} type="button">?</button>
                  <span className="category-tooltip" role="tooltip">
                    {selectedCategory.description}
                  </span>
                </span>
              )}
            </div>
            <select
              aria-invalid={Boolean(fieldErrors.category)}
              id="article-category"
              value={form.category}
              onChange={update('category')}
            >
              {categories.map((category) => (
                <option key={category.slug} value={category.slug}>{category.label}</option>
              ))}
            </select>
            {fieldErrors.category && <span className="field-error">{fieldErrors.category}</span>}
          </div>
          <label>
            Tags
            <input
              aria-invalid={Boolean(fieldErrors.tags)}
              placeholder="ai, writing, productivity"
              value={form.tags}
              onChange={update('tags')}
            />
            {fieldErrors.tags && <span className="field-error">{fieldErrors.tags}</span>}
          </label>
        </div>
        <label>
          Summary
          <textarea
            aria-invalid={Boolean(fieldErrors.summary)}
            maxLength="500"
            rows="3"
            value={form.summary}
            onChange={update('summary')}
          />
          {fieldErrors.summary && <span className="field-error">{fieldErrors.summary}</span>}
        </label>
        <div className="editor-cover-control">
          <span className="editor-field-label">Cover image</span>
          <div className={`editor-cover-dropzone ${form.coverImageUrl ? 'has-image' : 'is-empty'}`}>
            {form.coverImageUrl ? (
              <>
                <img alt="" className="editor-cover-preview" src={form.coverImageUrl} />
                <div className="editor-cover-overlay">
                  <div className="editor-cover-overlay-buttons">
                    <MediaUploader busy={uploading} label="Replace image" onUpload={upload} />
                    <button
                      className="text-button inline-pill-light"
                      type="button"
                      style={{ color: '#fff', borderColor: 'rgba(255,255,255,0.4)', background: 'rgba(255,255,255,0.15)' }}
                      onClick={() => setShowCoverUrlInput((current) => !current)}
                    >
                      {showCoverUrlInput ? 'Hide URL' : 'Edit URL'}
                    </button>
                    <button
                      className="text-button inline-pill-light danger"
                      type="button"
                      style={{ color: '#fca5a5', borderColor: 'rgba(239,68,68,0.4)', background: 'rgba(239,68,68,0.15)' }}
                      onClick={() => setForm((current) => ({ ...current, coverImageUrl: '' }))}
                    >
                      Remove
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="editor-cover-placeholder">
                <p className="placeholder-text">Add a high-quality cover image to make your story stand out</p>
                <div className="placeholder-actions">
                  <MediaUploader busy={uploading} label="Upload cover image" onUpload={upload} />
                  <button
                    className="text-button"
                    type="button"
                    onClick={() => setShowCoverUrlInput((current) => !current)}
                  >
                    {showCoverUrlInput ? 'Hide image URL' : 'Use image URL instead'}
                  </button>
                </div>
              </div>
            )}
          </div>
          {fieldErrors.coverImageUrl && <span className="field-error">{fieldErrors.coverImageUrl}</span>}
          {showCoverUrlInput && (
            <label style={{ marginTop: '12px' }}>
              Image URL
              <input
                placeholder="https://res.cloudinary.com/..."
                value={form.coverImageUrl}
                onChange={update('coverImageUrl')}
              />
            </label>
          )}
        </div>

        <div className="editor-markdown-wrapper" style={{ display: 'grid', gap: '9px' }}>
          <div className="editor-content-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="editor-field-label" style={{ margin: 0 }}>Markdown content</span>
            <div className="editor-content-tools" style={{ margin: 0 }}>
              <MediaUploader busy={contentImageUploading} label="Insert image" onUpload={insertContentImage} />
            </div>
          </div>
          <textarea
            aria-invalid={Boolean(fieldErrors.content)}
            className="editor-content"
            ref={contentRef}
            rows="16"
            value={form.content}
            onChange={update('content')}
          />
          {fieldErrors.content && <span className="field-error">{fieldErrors.content}</span>}
          <div className="editor-markdown-tip">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="tip-icon">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <span>
              Tip: Embed external web images using standard Markdown syntax: <code>![Alt Text](URL)</code>
            </span>
          </div>
        </div>
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
        <h2 className="preview-title">{form.title || 'Untitled article'}</h2>
        <p className="preview-summary">{form.summary || 'A concise summary helps readers decide where to spend their attention.'}</p>
        <MarkdownPreview content={form.content} />
      </aside>
    </form>
  )
}

function friendlyArticleError(message) {
  if (!message) {
    return 'Please review the article and try again.'
  }
  if (message.toLowerCase().includes('cover image')) {
    return 'Add a cover image before publishing.'
  }
  if (message.toLowerCase().includes('slug')) {
    return 'This title is too similar to an existing story. Adjust the title and try again.'
  }
  return message
}

function fieldErrorFromMessage(message) {
  const normalized = message.toLowerCase()
  if (normalized.includes('cover image')) {
    return { coverImageUrl: message }
  }
  if (normalized.includes('title')) {
    return { title: message }
  }
  if (normalized.includes('summary')) {
    return { summary: message }
  }
  if (normalized.includes('content') || normalized.includes('markdown')) {
    return { content: message }
  }
  if (normalized.includes('image')) {
    return { content: message }
  }
  if (normalized.includes('category')) {
    return { category: message }
  }
  if (normalized.includes('tag')) {
    return { tags: message }
  }
  return {}
}

function parseTags(value) {
  return [...new Set(
    (value || '')
      .split(',')
      .map((tag) => tag.trim().toLowerCase())
      .filter(Boolean),
  )]
}

function markdownImageCount(content) {
  return ((content || '').match(/!\[[^\]]*]\([^\s)]+\)/g) || []).length
}
