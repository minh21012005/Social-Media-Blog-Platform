import { useEffect, useMemo, useState } from 'react'
import { ArticleMeta } from '../components/ArticleCard'
import { AuthorBadge } from '../components/AuthorBadge'
import { MarkdownPreview } from '../components/MarkdownPreview'
import { SiteFooter } from '../components/SiteFooter'
import { formatCount, getArticleBySlug, recordArticleView } from '../services/articles'
import { createComment, createCommentReply, deleteComment, editComment, listArticleComments, listCommentReplies } from '../services/comments'
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

function CommentList({ articleAuthorId, comments, currentUserId, onDelete, onEdit, onLoadReplies, onReply, onRequireLogin }) {
  const [editingId, setEditingId] = useState('')
  const [draft, setDraft] = useState('')
  const [deletingId, setDeletingId] = useState('')
  const [replyDraft, setReplyDraft] = useState('')
  const [replyError, setReplyError] = useState('')
  const [replyingId, setReplyingId] = useState('')
  const [replyingToId, setReplyingToId] = useState('')
  const [savingId, setSavingId] = useState('')
  const [error, setError] = useState('')
  const [expandedReplies, setExpandedReplies] = useState({})
  const [repliesByComment, setRepliesByComment] = useState({})
  const [confirmDeleteComment, setConfirmDeleteComment] = useState(null)

  const getReplyCount = (comment) => Number(comment.stats?.replyCount || 0)

  const startEditingComment = (comment) => {
    setEditingId(comment.id)
    setDraft(comment.content)
    setReplyingToId('')
    setError('')
  }

  const cancelEditing = () => {
    setEditingId('')
    setDraft('')
    setError('')
  }

  const saveCommentEditing = async (event, comment) => {
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
      const updatedComment = await onEdit(comment, trimmed)
      if (comment.parentCommentId) {
        setRepliesByComment((current) => {
          const parentState = current[comment.parentCommentId]
          if (!parentState) {
            return current
          }
          return {
            ...current,
            [comment.parentCommentId]: {
              ...parentState,
              items: parentState.items.map((reply) => (
                reply.id === comment.id
                  ? { ...updatedComment, author: reply.author }
                  : reply
              )),
            },
          }
        })
      }
      cancelEditing()
    } catch (editError) {
      setError(editError.message || 'Could not update your comment.')
    } finally {
      setSavingId('')
    }
  }

  const toggleReplies = async (comment) => {
    const commentId = comment.id
    const isExpanded = Boolean(expandedReplies[commentId])

    if (isExpanded) {
      setExpandedReplies((current) => ({ ...current, [commentId]: false }))
      return
    }

    setExpandedReplies((current) => ({ ...current, [commentId]: true }))

    if (repliesByComment[commentId]) {
      return
    }

    setRepliesByComment((current) => ({
      ...current,
      [commentId]: { error: '', items: [], loading: true },
    }))

    try {
      const replies = await onLoadReplies(commentId)
      setRepliesByComment((current) => ({
        ...current,
        [commentId]: { error: '', items: replies, loading: false },
      }))
    } catch (replyError) {
      setRepliesByComment((current) => ({
        ...current,
        [commentId]: { error: replyError.message || 'Could not load replies.', items: [], loading: false },
      }))
    }
  }

  const renderReplies = (comment) => {
    const commentId = comment.id
    const replyState = repliesByComment[commentId]

    if (!expandedReplies[commentId]) {
      return null
    }

    return (
      <div className="comment-replies">
        {replyState?.loading && <p className="comment-reply-status">Loading replies...</p>}
        {replyState?.error && <p className="comment-reply-status error">{replyState.error}</p>}
        {replyState?.items?.map((reply) => renderReply(reply))}
      </div>
    )
  }

  const renderReply = (reply) => {
    const isMine = currentUserId && String(reply.authorId) === String(currentUserId)
    const isArticleOwner = currentUserId && String(articleAuthorId) === String(currentUserId)
    const canDelete = isMine || isArticleOwner
    const authorName = reply.author?.displayName || reply.author?.username || `Reader ${String(reply.authorId || '').slice(0, 6)}`
    const avatarLabel = (isMine ? 'You' : authorName).charAt(0).toUpperCase()
    const isEditing = editingId === reply.id
    const remaining = COMMENT_MAX_LENGTH - draft.length

    return (
      <article className="comment-item comment-reply-comment" key={reply.id}>
        {reply.author?.avatarUrl ? (
          <img alt="" className="comment-avatar" src={reply.author.avatarUrl} />
        ) : (
          <div className="comment-avatar" aria-hidden="true">
            {avatarLabel}
          </div>
        )}
        <div>
          <div className="comment-item-meta">
            <strong>{isMine ? 'You' : authorName}</strong>
            <span>{formatCommentDate(reply.createdAt)}</span>
            {reply.editedAt && <span>edited</span>}
            {isMine && !isEditing && (
              <button className="comment-action-button" disabled={deletingId === reply.id} type="button" onClick={() => startEditingComment(reply)}>
                Edit
              </button>
            )}
            {canDelete && !isEditing && (
              <button className="comment-action-button danger" disabled={deletingId === reply.id} type="button" onClick={() => requestDeleteComment(reply)}>
                {deletingId === reply.id ? 'Deleting...' : 'Delete'}
              </button>
            )}
          </div>
          {isEditing ? (
            <form className="comment-edit-form" onSubmit={(event) => saveCommentEditing(event, reply)}>
              <textarea
                aria-invalid={Boolean(error)}
                disabled={savingId === reply.id}
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
                <button className="text-button muted" disabled={savingId === reply.id} type="button" onClick={cancelEditing}>
                  Cancel
                </button>
                <button className="submit-button" disabled={savingId === reply.id || !draft.trim()} type="submit">
                  {savingId === reply.id ? 'Saving...' : 'Save'}
                </button>
              </div>
              {error && <p className="form-error">{error}</p>}
            </form>
          ) : (
            <p>{reply.content}</p>
          )}
        </div>
      </article>
    )
  }

  const renderRepliesToggle = (comment) => {
    const replyCount = getReplyCount(comment)
    if (!replyCount) {
      return null
    }

    const isExpanded = Boolean(expandedReplies[comment.id])
    return (
      <button className="comment-replies-toggle" type="button" onClick={() => toggleReplies(comment)}>
        {isExpanded ? 'Hide replies' : 'Show replies'}
      </button>
    )
  }

  const startReplying = (comment) => {
    if (!currentUserId) {
      onRequireLogin()
      return
    }
    setReplyingToId(comment.id)
    setReplyDraft('')
    setReplyError('')
    setError('')
  }

  const cancelReplying = () => {
    setReplyingToId('')
    setReplyDraft('')
    setReplyError('')
  }

  const submitReply = async (event, comment) => {
    event.preventDefault()
    const trimmed = replyDraft.trim()
    if (!trimmed) {
      setReplyError('Reply content is required.')
      return
    }
    if (trimmed.length > COMMENT_MAX_LENGTH) {
      setReplyError(`Reply content must not exceed ${COMMENT_MAX_LENGTH} characters.`)
      return
    }

    setReplyingId(comment.id)
    setReplyError('')
    try {
      const createdReply = await onReply(comment, trimmed)
      const refreshedReplies = await onLoadReplies(comment.id).catch(() => [createdReply])
      setExpandedReplies((current) => ({ ...current, [comment.id]: true }))
      setRepliesByComment((current) => ({
        ...current,
        [comment.id]: {
          error: '',
          items: refreshedReplies,
          loading: false,
        },
      }))
      cancelReplying()
    } catch (replyRequestError) {
      setReplyError(replyRequestError.message || 'Could not post your reply.')
    } finally {
      setReplyingId('')
    }
  }

  const closeDeleteDialog = () => {
    if (deletingId) {
      return
    }
    setConfirmDeleteComment(null)
    setError('')
  }

  const requestDeleteComment = (comment) => {
    setConfirmDeleteComment(comment)
    setError('')
  }

  const confirmDeleteSelectedComment = async () => {
    if (!confirmDeleteComment) {
      return
    }

    setDeletingId(confirmDeleteComment.id)
    setError('')
    try {
      await onDelete(confirmDeleteComment)
      if (confirmDeleteComment.parentCommentId) {
        setRepliesByComment((current) => {
          const parentState = current[confirmDeleteComment.parentCommentId]
          if (!parentState) {
            return current
          }
          return {
            ...current,
            [confirmDeleteComment.parentCommentId]: {
              ...parentState,
              items: parentState.items.filter((reply) => reply.id !== confirmDeleteComment.id),
            },
          }
        })
      }
      if (editingId === confirmDeleteComment.id) {
        setEditingId('')
        setDraft('')
      }
      setConfirmDeleteComment(null)
    } catch (deleteError) {
      setError(deleteError.message || 'Could not delete your comment.')
    } finally {
      setDeletingId('')
    }
  }

  if (!comments.length) {
    return (
      <div className="comment-empty">
        <p>No comments yet. Start the conversation.</p>
      </div>
    )
  }

  return (
    <>
      <div className="comment-list">
        {comments.map((comment) => {
        const isMine = currentUserId && String(comment.authorId) === String(currentUserId)
        const isArticleOwner = currentUserId && String(articleAuthorId) === String(currentUserId)
        const canDelete = isMine || isArticleOwner
        const authorName = comment.author?.displayName || comment.author?.username || `Reader ${String(comment.authorId || '').slice(0, 6)}`
        const avatarLabel = (isMine ? 'You' : authorName).charAt(0).toUpperCase()
        const isEditing = editingId === comment.id
        const isReplying = replyingToId === comment.id
        const remaining = COMMENT_MAX_LENGTH - draft.length
        const replyRemaining = COMMENT_MAX_LENGTH - replyDraft.length
        const isDeleted = comment.status === 'DELETED'
        const replyCount = getReplyCount(comment)

        if (isDeleted) {
          return (
            <article className="comment-item comment-item-deleted" key={comment.id}>
              <div className="deleted-comment-card">
                <div className="deleted-comment-header">
                  <div>
                    <p className="deleted-comment-title">Comment deleted</p>
                    {replyCount > 0 && (
                      <p className="deleted-comment-count">
                        ({replyCount} {replyCount === 1 ? 'reply' : 'replies'})
                      </p>
                    )}
                  </div>
                </div>
                {renderRepliesToggle(comment)}
                {renderReplies(comment)}
              </div>
            </article>
          )
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
                  <button className="comment-action-button" disabled={deletingId === comment.id} type="button" onClick={() => startEditingComment(comment)}>
                    Edit
                  </button>
                )}
                {canDelete && !isEditing && (
                  <>
                    <button className="comment-action-button danger" disabled={deletingId === comment.id} type="button" onClick={() => requestDeleteComment(comment)}>
                      {deletingId === comment.id ? 'Deleting...' : 'Delete'}
                    </button>
                  </>
                )}
                {!isEditing && (
                  <button className="comment-action-button" disabled={replyingId === comment.id} type="button" onClick={() => startReplying(comment)}>
                    Reply
                  </button>
                )}
              </div>
              {isEditing ? (
                <form className="comment-edit-form" onSubmit={(event) => saveCommentEditing(event, comment)}>
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
                <>
                  <p>{comment.content}</p>
                  {isReplying && (
                    <form className="comment-reply-form" onSubmit={(event) => submitReply(event, comment)}>
                      <textarea
                        aria-invalid={Boolean(replyError)}
                        disabled={replyingId === comment.id}
                        maxLength={COMMENT_MAX_LENGTH}
                        onChange={(event) => {
                          setReplyDraft(event.target.value)
                          if (replyError) {
                            setReplyError('')
                          }
                        }}
                        placeholder="Write a reply..."
                        rows="3"
                        value={replyDraft}
                      />
                      <div className="comment-reply-actions">
                        <span className={replyRemaining < 0 ? 'comment-count danger' : 'comment-count'}>
                          {replyRemaining} characters left
                        </span>
                        <button className="text-button muted" disabled={replyingId === comment.id} type="button" onClick={cancelReplying}>
                          Cancel
                        </button>
                        <button className="submit-button" disabled={replyingId === comment.id || !replyDraft.trim()} type="submit">
                          {replyingId === comment.id ? 'Replying...' : 'Reply'}
                        </button>
                      </div>
                      {replyError && <p className="form-error">{replyError}</p>}
                    </form>
                  )}
                  {renderRepliesToggle(comment)}
                  {renderReplies(comment)}
                </>
              )}
            </div>
          </article>
        )
        })}
      </div>
      {confirmDeleteComment && (
        <div className="comment-delete-backdrop" role="presentation" onClick={closeDeleteDialog}>
          <div
            aria-labelledby="delete-comment-title"
            aria-modal="true"
            className="comment-delete-dialog"
            role="dialog"
            onClick={(event) => event.stopPropagation()}
          >
            <p className="comment-delete-kicker">Delete comment</p>
            <h2 id="delete-comment-title">Do you want to delete this comment?</h2>
            <p>
              This action will remove the comment from the discussion. If it has replies, it will be shown as a deleted comment.
            </p>
            {error && <p className="form-error">{error}</p>}
            <div className="comment-delete-actions">
              <button className="text-button muted" disabled={Boolean(deletingId)} type="button" onClick={closeDeleteDialog}>
                Cancel
              </button>
              <button className="comment-delete-confirm" disabled={Boolean(deletingId)} type="button" onClick={confirmDeleteSelectedComment}>
                {deletingId ? 'Deleting...' : 'Delete comment'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
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
          const articleComments = session
            ? await requestWithAuth((token) => listArticleComments(article.id, token))
            : await listArticleComments(article.id)
          const enrichedComments = await enrichComments(articleComments)
          if (active) {
            setComments(enrichedComments)
            setCommentsState({ loading: false, error: '' })
          }
        } catch (commentsError) {
          console.error('Could not load comments.', commentsError)
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
  }, [requestWithAuth, session, slug])

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
    return updated
  }

  const handleCommentDeleted = async (comment) => {
    await requestWithAuth((token) => deleteComment(comment.id, token))
    if (comment.parentCommentId) {
      setComments((current) => current.map((item) => {
        if (item.id !== comment.parentCommentId) {
          return item
        }
        const currentReplyCount = Number(item.stats?.replyCount || 0)
        return {
          ...item,
          stats: {
            ...(item.stats || {}),
            replyCount: Math.max(0, currentReplyCount - 1),
          },
        }
      }))
      return
    }
    const replyCount = Number(comment.stats?.replyCount || 0)
    if (!comment.parentCommentId && replyCount > 0) {
      setComments((current) => current.map((item) => (
        item.id === comment.id
          ? {
              ...item,
              content: 'Comment deleted',
              deletedAt: new Date().toISOString(),
              editedAt: null,
              status: 'DELETED',
            }
          : item
      )))
      return
    }
    setComments((current) => current.filter((item) => item.id !== comment.id))
  }

  const handleLoadReplies = async (commentId) => {
    const replies = session
      ? await requestWithAuth((token) => listCommentReplies(commentId, token))
      : await listCommentReplies(commentId)
    return enrichComments(replies)
  }

  const handleCommentReplied = async (comment, content) => {
    const created = await requestWithAuth((token) => createCommentReply(comment.id, { content }, token))
    const enrichedReply = {
      ...created,
      author: session?.user || null,
    }
    setComments((current) => current.map((item) => {
      if (item.id !== comment.id) {
        return item
      }
      const currentReplyCount = Number(item.stats?.replyCount || 0)
      return {
        ...item,
        stats: {
          ...(item.stats || {}),
          replyCount: currentReplyCount + 1,
        },
      }
    }))
    return enrichedReply
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
          <CommentList
            articleAuthorId={article.author?.id}
            comments={comments}
            currentUserId={currentUserId}
            onDelete={handleCommentDeleted}
            onEdit={handleCommentEdited}
            onLoadReplies={handleLoadReplies}
            onReply={handleCommentReplied}
            onRequireLogin={() => navigate('/login')}
          />
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
