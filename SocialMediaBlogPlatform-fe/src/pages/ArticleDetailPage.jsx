import { useEffect, useMemo, useState } from 'react'
import { ArticleMeta } from '../components/ArticleCard'
import { AuthorBadge } from '../components/AuthorBadge'
import { MarkdownPreview } from '../components/MarkdownPreview'
import { SiteFooter } from '../components/SiteFooter'
import { formatCount, getArticleBySlug, recordArticleView } from '../services/articles'
import { createComment, editComment, listArticleComments } from '../services/comments'
import { getPublicUsers } from '../services/users'

const COMMENT_MAX_LENGTH = 5000

function formatCommentDate(value) {
  if (!value) {
    return 'Just now'
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

function CommentComposer({ articleId, session, requestWithAuth, navigate, onCreated }) {
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const remaining = COMMENT_MAX_LENGTH - content.length

  const handleSubmit = async (event) => {
    event.preventDefault()
    const trimmed = content.trim()

    if (!session) {
      navigate('/login')
      return
    }

    if (!trimmed) {
      setError('Comment content is required.')
      return
    }

    if (trimmed.length > COMMENT_MAX_LENGTH) {
      setError(`Comment content must not exceed ${COMMENT_MAX_LENGTH} characters.`)
      return
    }

    setSubmitting(true)
    setError('')

    try {
      const created = await requestWithAuth((token) => createComment(articleId, { content: trimmed }, token))
      onCreated(created)
      setContent('')
    } catch (requestError) {
      setError(requestError.message || 'Could not post your comment.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="comment-composer" onSubmit={handleSubmit}>
      <div className="comment-composer-header">
        <div>
          <span className="form-eyebrow">Join the discussion</span>
          <h2 id="comments-title">Comments</h2>
        </div>
        {!session && (
          <button className="text-button" type="button" onClick={() => navigate('/login')}>
            Log in to comment
          </button>
        )}
      </div>
      <label>
        <span className="editor-field-label">Your comment</span>
        <textarea
          aria-invalid={Boolean(error)}
          disabled={submitting}
          maxLength={COMMENT_MAX_LENGTH}
          onChange={(event) => {
            setContent(event.target.value)
            if (error) {
              setError('')
            }
          }}
          placeholder={session ? 'Share your thoughts...' : 'Log in before posting a comment.'}
          rows="5"
          value={content}
        />
      </label>
      <div className="comment-composer-actions">
        <span className={remaining < 0 ? 'comment-count danger' : 'comment-count'}>
          {remaining} characters left
        </span>
        <button className="submit-button" disabled={submitting || !content.trim()} type="submit">
          {submitting ? 'Posting...' : 'Post comment'}
        </button>
      </div>
      {error && <p className="form-error">{error}</p>}
    </form>
  )
}

async function enrichComments(comments) {
  const authorMap = await getPublicUsers((comments || []).map((comment) => comment.authorId)).catch(() => new Map())
  return (comments || []).map((comment) => ({
    ...comment,
    author: authorMap.get(comment.authorId) || null,
  }))
}

function CommentList({ comments, currentUserId, onEdit }) {
  const [editingId, setEditingId] = useState('')
  const [draft, setDraft] = useState('')
  const [savingId, setSavingId] = useState('')
  const [error, setError] = useState('')

  if (!comments.length) {
    return (
      <div className="comment-empty">
        <p>No comments yet. Start the conversation.</p>
      </div>
    )
  }

  return (
    <div className="comment-list">
      {comments.map((comment) => {
        const isMine = currentUserId && String(comment.authorId) === String(currentUserId)
        const authorName = comment.author?.displayName || comment.author?.username || `Reader ${String(comment.authorId || '').slice(0, 6)}`
        const avatarLabel = (isMine ? 'You' : authorName).charAt(0).toUpperCase()
        const isEditing = editingId === comment.id
        const remaining = COMMENT_MAX_LENGTH - draft.length

        const startEditing = () => {
          setEditingId(comment.id)
          setDraft(comment.content)
          setError('')
        }

        const cancelEditing = () => {
          setEditingId('')
          setDraft('')
          setError('')
        }

        const saveEditing = async (event) => {
          event.preventDefault()
          const trimmed = draft.trim()
          if (!trimmed) {
            setError('Comment content is required.')
            return
          }
          if (trimmed.length > COMMENT_MAX_LENGTH) {
            setError(`Comment content must not exceed ${COMMENT_MAX_LENGTH} characters.`)
            return
          }
          setSavingId(comment.id)
          setError('')
          try {
            await onEdit(comment, trimmed)
            cancelEditing()
          } catch (editError) {
            setError(editError.message || 'Could not update your comment.')
          } finally {
            setSavingId('')
          }
        }

        return (
          <article className="comment-item" key={comment.id}>
            {comment.author?.avatarUrl ? (
              <img alt="" className="comment-avatar" src={comment.author.avatarUrl} />
            ) : (
              <div className="comment-avatar" aria-hidden="true">
                {avatarLabel}
              </div>
            )}
            <div>
              <div className="comment-item-meta">
                <strong>{isMine ? 'You' : authorName}</strong>
                <span>{formatCommentDate(comment.createdAt)}</span>
                {comment.editedAt && <span>edited</span>}
                {isMine && !isEditing && (
                  <button className="comment-action-button" type="button" onClick={startEditing}>
                    Edit
                  </button>
                )}
              </div>
              {isEditing ? (
                <form className="comment-edit-form" onSubmit={saveEditing}>
                  <textarea
                    aria-invalid={Boolean(error)}
                    disabled={savingId === comment.id}
                    maxLength={COMMENT_MAX_LENGTH}
                    onChange={(event) => {
                      setDraft(event.target.value)
                      if (error) {
                        setError('')
                      }
                    }}
                    rows="4"
                    value={draft}
                  />
                  <div className="comment-edit-actions">
                    <span className={remaining < 0 ? 'comment-count danger' : 'comment-count'}>
                      {remaining} characters left
                    </span>
                    <button className="text-button muted" disabled={savingId === comment.id} type="button" onClick={cancelEditing}>
                      Cancel
                    </button>
                    <button className="submit-button" disabled={savingId === comment.id || !draft.trim()} type="submit">
                      {savingId === comment.id ? 'Saving...' : 'Save'}
                    </button>
                  </div>
                  {error && <p className="form-error">{error}</p>}
                </form>
              ) : (
                <p>{comment.content}</p>
              )}
            </div>
          </article>
        )
      })}
    </div>
  )
}

export function ArticleDetailPage({ slug, navigate, session, requestWithAuth }) {
  const [state, setState] = useState({ loading: true, article: null, error: '' })
  const [comments, setComments] = useState([])
  const [commentsState, setCommentsState] = useState({ loading: false, error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const article = await getArticleBySlug(slug)
        recordArticleView(article.id, { source: 'web' }).catch(() => null)
        if (active) {
          setState({ loading: false, article, error: '' })
          setCommentsState({ loading: true, error: '' })
        }
        try {
          const articleComments = await listArticleComments(article.id)
          const enrichedComments = await enrichComments(articleComments)
          if (active) {
            setComments(enrichedComments)
            setCommentsState({ loading: false, error: '' })
          }
        } catch (commentsError) {
          if (active) {
            setComments([])
            setCommentsState({ loading: false, error: commentsError.message || 'Could not load comments.' })
          }
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
  }, [slug])

  const currentUserId = useMemo(() => session?.user?.id, [session])

  const handleCommentCreated = (comment) => {
    setComments((current) => [
      {
        ...comment,
        author: session?.user || null,
      },
      ...current,
    ])
  }

  const handleCommentEdited = async (comment, content) => {
    const updated = await requestWithAuth((token) => editComment(comment.id, { content }, token))
    setComments((current) => current.map((item) => (
      item.id === comment.id
        ? { ...updated, author: item.author }
        : item
    )))
  }

  if (state.loading) {
    return <main className="page-container loading-state">Loading story...</main>
  }

  if (state.error || !state.article) {
    return (
      <main>
        <section className="page-container empty-state">
          <h2>Story not found.</h2>
          <p>{state.error || 'This story is not published yet.'}</p>
          <button className="pill-button" type="button" onClick={() => navigate('/')}>Back home</button>
        </section>
        <SiteFooter />
      </main>
    )
  }

  const { article } = state

  return (
    <main>
      <article className="article-detail">
        <header className="article-detail-header page-container">
          <div className="eyebrow-row">
            <span>{article.category}</span>
            <span aria-hidden="true">&middot;</span>
            <span>{article.readTime}</span>
            <span aria-hidden="true">&middot;</span>
            <span>{formatCount(article.stats?.viewCount)} views</span>
          </div>
          <h1>{article.title}</h1>
          <p>{article.summary}</p>
          <div className="article-detail-meta">
            <AuthorBadge author={article.author} navigate={navigate} />
            <ArticleMeta article={article} />
          </div>
        </header>
        <img alt="" className="article-detail-cover" src={article.image} />
        <section className="article-content page-container">
          <MarkdownPreview content={article.content} />
        </section>
      </article>
      <section className="comments-section page-container" aria-labelledby="comments-title">
        <CommentComposer
          articleId={article.id}
          navigate={navigate}
          onCreated={handleCommentCreated}
          requestWithAuth={requestWithAuth}
          session={session}
        />
        <div className="comments-panel">
          {commentsState.loading && <p className="comment-loading">Loading comments...</p>}
          {commentsState.error && <p className="form-error">{commentsState.error}</p>}
          <CommentList comments={comments} currentUserId={currentUserId} onEdit={handleCommentEdited} />
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
