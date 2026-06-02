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

export function ArticleEditor({ initialArticle, requestWithAuth, token, saving, onSave, onPublish, notify }) {
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

  const payload = useMemo(() => ({
    title: form.title,
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
    }))
    setFieldErrors((current) => ({ ...current, [field]: '' }))
  }

  const upload = async (file) => {
    setError('')
    setFieldErrors((current) => ({ ...current, coverImageUrl: '' }))
    setUploading(true)
    try {
      const media = requestWithAuth
        ? await requestWithAuth((accessToken) => uploadArticleMedia(file, accessToken))
        : await uploadArticleMedia(file, token)
      setForm((current) => ({ ...current, coverImageUrl: media.secureUrl }))
    } catch (requestError) {
      showRequestError(requestError)
    } finally {
      setUploading(false)
    }
  }

  const insertContentImage = async (file) => {
    setError('')
    setContentImageUploading(true)
    try {
      const media = requestWithAuth
        ? await requestWithAuth((accessToken) => uploadArticleMedia(file, accessToken))
        : await uploadArticleMedia(file, token)
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
    if (mode === 'publish' && !form.summary.trim()) {
      nextErrors.summary = 'Summary is required before publishing.'
    }
    if (!form.content.trim()) {
      nextErrors.content = 'Write some Markdown content before saving.'
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
          <input aria-invalid={Boolean(fieldErrors.title)} value={form.title} onChange={update('title')} />
          {fieldErrors.title && <span className="field-error">{fieldErrors.title}</span>}
        </label>
        <div className="editor-row">
          <label>
            Category
            <select aria-invalid={Boolean(fieldErrors.category)} value={form.category} onChange={update('category')}>
              {categories.map((category) => (
                <option key={category.slug} value={category.slug}>{category.label}</option>
              ))}
            </select>
            {fieldErrors.category && <span className="field-error">{fieldErrors.category}</span>}
          </label>
        </div>
        <label>
          Summary
          <textarea aria-invalid={Boolean(fieldErrors.summary)} maxLength="500" rows="3" value={form.summary} onChange={update('summary')} />
          {fieldErrors.summary && <span className="field-error">{fieldErrors.summary}</span>}
        </label>
        <div className="editor-cover-control">
          <span className="editor-field-label">Cover image</span>
          {form.coverImageUrl && <img alt="" className="editor-cover-preview" src={form.coverImageUrl} />}
          {fieldErrors.coverImageUrl && <span className="field-error">{fieldErrors.coverImageUrl}</span>}
          <div className="editor-cover-actions">
            <MediaUploader busy={uploading} label={form.coverImageUrl ? 'Replace cover image' : 'Upload cover image'} onUpload={upload} />
            <button
              className="text-button"
              type="button"
              onClick={() => setShowCoverUrlInput((current) => !current)}
            >
              {showCoverUrlInput ? 'Hide image URL' : 'Use image URL instead'}
            </button>
            {form.coverImageUrl && (
              <button
                className="text-button muted"
                type="button"
                onClick={() => setForm((current) => ({ ...current, coverImageUrl: '' }))}
              >
                Remove
              </button>
            )}
          </div>
          {showCoverUrlInput && (
            <label>
              Image URL
              <input
                placeholder="https://res.cloudinary.com/..."
                value={form.coverImageUrl}
                onChange={update('coverImageUrl')}
              />
              {fieldErrors.coverImageUrl && <span className="field-error">{fieldErrors.coverImageUrl}</span>}
            </label>
          )}
        </div>
        <label>
          Tags
          <input placeholder="design, web, culture" value={form.tags} onChange={update('tags')} />
        </label>
        <label>
          Markdown content
          <textarea
            aria-invalid={Boolean(fieldErrors.content)}
            className="editor-content"
            ref={contentRef}
            rows="16"
            value={form.content}
            onChange={update('content')}
          />
          {fieldErrors.content && <span className="field-error">{fieldErrors.content}</span>}
        </label>
        <div className="editor-content-tools">
          <MediaUploader busy={contentImageUploading} label="Insert image" onUpload={insertContentImage} />
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
  if (normalized.includes('category')) {
    return { category: message }
  }
  return {}
}
