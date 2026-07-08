import { useEffect, useMemo, useState } from 'react'
import { AuthorBadge } from '../components/AuthorBadge'
import { MarkdownPreview } from '../components/MarkdownPreview'
import { SiteFooter } from '../components/SiteFooter'
import { formatCount, getArticleBySlug, recordArticleView } from '../services/articles'
import { createComment, createCommentReply, deleteComment, editComment, listArticleComments, listCommentReplies, getArticleCommentCount } from '../services/comments'
import { getPublicUsers } from '../services/users'
import { CommentClapButton } from '../components/CommentClapButton'

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

  if (!session) {
    return (
      <div className="comment-login-cta" style={{
        textAlign: 'center',
        padding: '32px 24px',
        border: '1px solid var(--border)',
        borderRadius: '12px',
        backgroundColor: '#f8fafc',
        marginBottom: '40px',
      }}>
        <p style={{ margin: '0 0 16px 0', color: 'var(--muted)', fontWeight: '600', fontSize: '15px' }}>
          Log in to share your thoughts and join the discussion.
        </p>
        <button className="pill-button" type="button" onClick={() => navigate('/login')} style={{
          backgroundColor: 'var(--ink)',
          color: '#fff',
          borderRadius: '999px',
          padding: '12px 28px',
          fontWeight: '700',
          fontSize: '14px',
          cursor: 'pointer',
          border: 'none',
        }}>
          Log In to Comment
        </button>
      </div>
    )
  }

  return (
    <form className="comment-composer" onSubmit={handleSubmit}>
      <div style={{ marginBottom: '16px' }}>
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
          placeholder="Share your thoughts..."
          rows="4"
          value={content}
          style={{
            width: '100%',
            padding: '16px',
            border: '1px solid var(--border)',
            borderRadius: '12px',
            fontFamily: 'inherit',
            fontSize: '15px',
            color: 'var(--ink)',
            outline: 'none',
            resize: 'vertical',
            transition: 'border-color 0.2s, box-shadow 0.2s',
          }}
        />
      </div>
      <div className="comment-composer-actions" style={{ display: 'flex', justifyContent: remaining <= 1000 ? 'space-between' : 'flex-end', alignItems: 'center' }}>
        {remaining <= 1000 && (
          <span className={remaining < 0 ? 'comment-count danger' : 'comment-count'} style={{ fontSize: '13px', color: 'var(--muted)', fontWeight: '500' }}>
            {remaining} characters left
          </span>
        )}
        <button className="submit-button" disabled={submitting || !content.trim()} type="submit">
          {submitting ? 'Posting...' : 'Post comment'}
        </button>
      </div>
      {error && <p className="form-error" style={{ color: 'var(--red, #ef4444)', marginTop: '8px', fontSize: '14px' }}>{error}</p>}
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

function CommentList({ articleAuthorId, comments, currentUserId, onDelete, onEdit, onLoadReplies, onReply, onRequireLogin, mutedUserIds = new Set(), onPin, onUnpin, onClap, onUndoClap }) {

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
  const [openMenuId, setOpenMenuId] = useState(null)

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
      [commentId]: { error: '', items: [], loading: true, page: 0, hasMore: false },
    }))

    try {
      const data = await onLoadReplies(commentId, 0)
      setRepliesByComment((current) => ({
        ...current,
        [commentId]: { error: '', items: data.items, page: data.page, hasMore: data.hasMore, loading: false },
      }))
    } catch (replyError) {
      setRepliesByComment((current) => ({
        ...current,
        [commentId]: { error: replyError.message || 'Could not load replies.', items: [], loading: false, page: 0, hasMore: false },
      }))
    }
  }

  const loadMoreReplies = async (commentId) => {
    const currentState = repliesByComment[commentId]
    if (!currentState || currentState.loadingMore) return

    setRepliesByComment((current) => ({
      ...current,
      [commentId]: { ...currentState, loadingMore: true },
    }))

    try {
      const nextPage = currentState.page + 1
      const data = await onLoadReplies(commentId, nextPage)
      setRepliesByComment((current) => ({
        ...current,
        [commentId]: {
          error: '',
          items: [...currentState.items, ...data.items],
          page: data.page,
          hasMore: data.hasMore,
          loading: false,
          loadingMore: false
        },
      }))
    } catch (err) {
      setRepliesByComment((current) => ({
        ...current,
        [commentId]: { ...currentState, error: 'Could not load more replies.', loadingMore: false },
      }))
    }
  }

  const handleClapAction = async (comment, isUndo) => {
    const updated = isUndo ? await onUndoClap(comment) : await onClap(comment)
    if (updated && comment.parentCommentId) {
      setRepliesByComment((current) => {
        const parentState = current[comment.parentCommentId]
        if (!parentState) return current
        return {
          ...current,
          [comment.parentCommentId]: {
            ...parentState,
            items: parentState.items.map((reply) => reply.id === comment.id ? updated : reply),
          },
        }
      })
    }
  }

  const renderReplies = (comment) => {
    const commentId = comment.id
    const replyState = repliesByComment[commentId]

    if (!expandedReplies[commentId]) {
      return null
    }

    const filteredReplies = replyState?.items?.filter((reply) => !reply.author || !mutedUserIds.has(reply.author.id)) || []

    return (
      <div className="comment-replies">
        {replyState?.loading && <p className="comment-reply-status">Loading replies...</p>}
        {replyState?.error && <p className="comment-reply-status error">{replyState.error}</p>}
        {filteredReplies.map((reply) => renderReply(reply))}
        {replyState?.hasMore && (
          <button className="text-button" disabled={replyState.loadingMore} type="button" onClick={() => loadMoreReplies(commentId)}>
            {replyState.loadingMore ? 'Loading more replies...' : 'Load more replies'}
          </button>
        )}
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
            <div style={{ position: 'relative', display: 'inline-block', marginLeft: 'auto' }}>
              <button className="comment-action-button" type="button" onClick={() => setOpenMenuId(openMenuId === reply.id ? null : reply.id)}>...</button>
              {openMenuId === reply.id && (
                <div className="comment-menu-popup" style={{ position: 'absolute', top: '100%', right: 0, background: 'var(--surface)', border: '1px solid var(--border)', padding: '4px', borderRadius: '4px', zIndex: 10, minWidth: '120px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                  {isMine && !isEditing && (
                    <button className="comment-action-button" disabled={deletingId === reply.id} type="button" onClick={() => { startEditingComment(reply); setOpenMenuId(null); }} style={{ display: 'block', width: '100%', textAlign: 'left' }}>Edit</button>
                  )}
                  {canDelete && !isEditing && (
                    <button className="comment-action-button danger" disabled={deletingId === reply.id} type="button" onClick={() => { requestDeleteComment(reply); setOpenMenuId(null); }} style={{ display: 'block', width: '100%', textAlign: 'left' }}>Delete</button>
                  )}
                  {reply.clappedByCurrentUser && (
                    <button className="comment-action-button" type="button" onClick={() => { handleClapAction(reply, true); setOpenMenuId(null); }} style={{ display: 'block', width: '100%', textAlign: 'left' }}>Undo claps</button>
                  )}
                  {!isMine && !canDelete && !reply.clappedByCurrentUser && (
                    <span style={{ padding: '4px 8px', color: 'var(--ink-light)', fontSize: '0.875rem', display: 'block' }}>No actions</span>
                  )}
                </div>
              )}
            </div>
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
            <>
              <p>{reply.content}</p>
              <div className="comment-item-actions" style={{ display: 'flex', alignItems: 'center', marginTop: '8px' }}>
                <CommentClapButton comment={reply} onClap={handleClapAction} currentUserId={currentUserId} />
              </div>
            </>
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
      const data = await onLoadReplies(comment.id).catch(() => ({ items: [createdReply], page: 0, hasMore: false }))
      setExpandedReplies((current) => ({ ...current, [comment.id]: true }))
      setRepliesByComment((current) => ({
        ...current,
        [comment.id]: {
          error: '',
          items: data.items,
          page: data.page,
          hasMore: data.hasMore,
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

  const filteredComments = comments.filter((comment) => {
    if (comment.status === 'DELETED') {
      return true
    }
    return !comment.author || !mutedUserIds.has(comment.author.id)
  })

  if (!filteredComments.length) {
    return (
      <div className="comment-empty">
        <p>No comments yet. Start the conversation.</p>
      </div>
    )
  }

  return (
    <>
      <div className="comment-list">
        {filteredComments.map((comment) => {
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
                  {comment.pinnedAt && <span className="comment-pinned-badge" title="Pinned by author" style={{ background: 'var(--accent-light)', color: 'var(--accent)', padding: '2px 6px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>📌 Pinned</span>}
                  <span>{formatCommentDate(comment.createdAt)}</span>
                  {comment.editedAt && <span>edited</span>}
                  {isArticleOwner && !isEditing && (
                    <button className="comment-action-button" type="button" onClick={() => comment.pinnedAt ? onUnpin(comment) : onPin(comment)}>
                      {comment.pinnedAt ? 'Unpin' : 'Pin'}
                    </button>
                  )}
                  <div style={{ position: 'relative', display: 'inline-block', marginLeft: 'auto' }}>
                    <button className="comment-action-button" type="button" onClick={() => setOpenMenuId(openMenuId === comment.id ? null : comment.id)}>...</button>
                    {openMenuId === comment.id && (
                      <div className="comment-menu-popup" style={{ position: 'absolute', top: '100%', right: 0, background: 'var(--surface)', border: '1px solid var(--border)', padding: '4px', borderRadius: '4px', zIndex: 10, minWidth: '120px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                        {isMine && !isEditing && (
                          <button className="comment-action-button" disabled={deletingId === comment.id} type="button" onClick={() => { startEditingComment(comment); setOpenMenuId(null); }} style={{ display: 'block', width: '100%', textAlign: 'left' }}>Edit</button>
                        )}
                        {canDelete && !isEditing && (
                          <button className="comment-action-button danger" disabled={deletingId === comment.id} type="button" onClick={() => { requestDeleteComment(comment); setOpenMenuId(null); }} style={{ display: 'block', width: '100%', textAlign: 'left' }}>Delete</button>
                        )}
                        {comment.clappedByCurrentUser && (
                          <button className="comment-action-button" type="button" onClick={() => { handleClapAction(comment, true); setOpenMenuId(null); }} style={{ display: 'block', width: '100%', textAlign: 'left' }}>Undo claps</button>
                        )}
                        {!isMine && !canDelete && !comment.clappedByCurrentUser && (
                          <span style={{ padding: '4px 8px', color: 'var(--ink-light)', fontSize: '0.875rem', display: 'block' }}>No actions</span>
                        )}
                      </div>
                    )}
                  </div>
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
                    <div className="comment-edit-actions" style={{ display: 'flex', justifyContent: remaining <= 1000 ? 'space-between' : 'flex-end', alignItems: 'center', gap: '8px' }}>
                      {remaining <= 1000 && (
                        <span className={remaining < 0 ? 'comment-count danger' : 'comment-count'}>
                          {remaining} characters left
                        </span>
                      )}
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="text-button muted" disabled={savingId === comment.id} type="button" onClick={cancelEditing}>
                          Cancel
                        </button>
                        <button className="submit-button" disabled={savingId === comment.id || !draft.trim()} type="submit">
                          {savingId === comment.id ? 'Saving...' : 'Save'}
                        </button>
                      </div>
                    </div>
                    {error && <p className="form-error">{error}</p>}
                  </form>
                ) : (
                  <>
                    <p>{comment.content}</p>
                    <div className="comment-item-actions" style={{ display: 'flex', alignItems: 'center', marginTop: '8px', marginBottom: '8px' }}>
                      <CommentClapButton comment={comment} onClap={handleClapAction} currentUserId={currentUserId} />
                      <button className="comment-action-button" disabled={replyingId === comment.id} type="button" onClick={() => startReplying(comment)}>
                        Reply
                      </button>
                    </div>
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
                        <div className="comment-reply-actions" style={{ display: 'flex', justifyContent: replyRemaining <= 1000 ? 'space-between' : 'flex-end', alignItems: 'center', gap: '8px' }}>
                          {replyRemaining <= 1000 && (
                            <span className={replyRemaining < 0 ? 'comment-count danger' : 'comment-count'}>
                              {replyRemaining} characters left
                            </span>
                          )}
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button className="text-button muted" disabled={replyingId === comment.id} type="button" onClick={cancelReplying}>
                              Cancel
                            </button>
                            <button className="submit-button" disabled={replyingId === comment.id || !replyDraft.trim()} type="submit">
                              {replyingId === comment.id ? 'Replying...' : 'Reply'}
                            </button>
                          </div>
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

export function ArticleDetailPage({ slug, navigate, session, requestWithAuth, mutedUserIds = new Set() }) {
  const [state, setState] = useState({ loading: true, article: null, error: '' })
  const [comments, setComments] = useState([])
  const [commentsState, setCommentsState] = useState({ loading: false, error: '' })
  const [commentPage, setCommentPage] = useState(0)
  const [commentSortBy, setCommentSortBy] = useState('NEWEST')
  const [hasMoreComments, setHasMoreComments] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)

  const [commentCount, setCommentCount] = useState(0)

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const article = await getArticleBySlug(slug)
        recordArticleView(article.id, { source: 'web' }).catch(() => null)
        if (active) {
          setState({ loading: false, article, error: '' })
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

  useEffect(() => {
    let active = true
    if (!state.article) return

    getArticleCommentCount(state.article.id)
      .then((res) => {
        if (active && res && typeof res.commentCount === 'number') {
          setCommentCount(res.commentCount)
        }
      })
      .catch(() => {})

    return () => {
      active = false
    }
  }, [state.article])

  useEffect(() => {
    let active = true
    if (!state.article) return

    async function loadComments() {
      setCommentsState({ loading: true, error: '' })
      try {
        const pageResult = session
          ? await requestWithAuth((token) => listArticleComments(state.article.id, token, 0, 10, commentSortBy))
          : await listArticleComments(state.article.id, null, 0, 10, commentSortBy)

        // If the backend has not yet updated to return PageResult, fallback safely
        const items = pageResult.items || pageResult
        const enrichedComments = await enrichComments(items)
        if (active) {
          setComments(enrichedComments)
          setCommentPage(0)
          setHasMoreComments(pageResult.page !== undefined ? pageResult.page + 1 < pageResult.totalPages : false)
          setCommentsState({ loading: false, error: '' })
        }
      } catch (commentsError) {
        console.error('Could not load comments.', commentsError)
        if (active) {
          setComments([])
          setHasMoreComments(false)
          setCommentsState({ loading: false, error: commentsError.message || 'Could not load comments.' })
        }
      }
    }
    loadComments()
    return () => {
      active = false
    }
  }, [requestWithAuth, session, state.article, commentSortBy])

  const handleLoadMoreComments = async () => {
    if (!state.article) return
    setLoadingMore(true)
    try {
      const nextPage = commentPage + 1
      const pageResult = session
        ? await requestWithAuth((token) => listArticleComments(state.article.id, token, nextPage, 10, commentSortBy))
        : await listArticleComments(state.article.id, null, nextPage, 10, commentSortBy)
      const items = pageResult.items || []
      const enrichedComments = await enrichComments(items)

      setComments(prev => [...prev, ...enrichedComments])
      setCommentPage(nextPage)
      setHasMoreComments(pageResult.page !== undefined ? pageResult.page + 1 < pageResult.totalPages : false)
    } catch (err) {
      console.error(err)
    } finally {
      setLoadingMore(false)
    }
  }

  const currentUserId = useMemo(() => session?.user?.id, [session])

  const handleCommentCreated = (comment) => {
    setCommentCount((prev) => prev + 1)
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
    setCommentCount((prev) => Math.max(0, prev - 1))
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

  const handleLoadReplies = async (commentId, page = 0) => {
    const repliesPage = session
      ? await requestWithAuth((token) => listCommentReplies(commentId, token, page, 10))
      : await listCommentReplies(commentId, null, page, 10)
    const items = repliesPage.items || repliesPage
    return {
      items: await enrichComments(items),
      page: repliesPage.page || 0,
      hasMore: repliesPage.page !== undefined ? repliesPage.page + 1 < repliesPage.totalPages : false
    }
  }

  const handleCommentReplied = async (comment, content) => {
    const created = await requestWithAuth((token) => createCommentReply(comment.id, { content }, token))
    setCommentCount((prev) => prev + 1)
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

  const handleCommentPinned = async (comment) => {
    const pinnedComment = await requestWithAuth((token) => import('../services/comments').then(m => m.pinComment(comment.id, token)))
    setComments((current) => current.map((item) => (
      item.id === comment.id
        ? { ...pinnedComment, author: item.author }
        : { ...item, pinnedAt: null } // unpin others
    )))
  }

  const handleCommentUnpinned = async (comment) => {
    const unpinnedComment = await requestWithAuth((token) => import('../services/comments').then(m => m.unpinComment(comment.id, token)))
    setComments((current) => current.map((item) => (
      item.id === comment.id
        ? { ...unpinnedComment, author: item.author }
        : item
    )))
  }

  const handleCommentClap = async (comment) => {
    if (!session) {
      navigate('/login')
      return null
    }
    const { clapComment } = await import('../services/comments')
    await requestWithAuth((token) => clapComment(comment.id, token))

    const count = Number(comment.stats?.clapCount || 0)
    const updated = {
      ...comment,
      clappedByCurrentUser: true,
      stats: { ...comment.stats, clapCount: count + 1 }
    }

    setComments((current) => current.map((item) => item.id === comment.id ? updated : item))
    return updated
  }

  const handleCommentUndoClap = async (comment) => {
    const { undoClapComment } = await import('../services/comments')
    const response = await requestWithAuth((token) => undoClapComment(comment.id, token))
    const removedCount = Number(response || 1) // default to 1 if api doesn't return data

    const count = Number(comment.stats?.clapCount || 0)
    const updated = {
      ...comment,
      clappedByCurrentUser: false,
      stats: { ...comment.stats, clapCount: Math.max(0, count - removedCount) }
    }

    setComments((current) => current.map((item) => item.id === comment.id ? updated : item))
    return updated
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
            <span style={{ color: 'var(--ink-light)' }}>&middot; {article.date}</span>
          </div>
        </header>
        <img alt="" className="article-detail-cover" src={article.image} />
        <section className="article-content page-container">
          <MarkdownPreview content={article.content} />
          {(() => {
            const tagList = Array.isArray(article.tags)
              ? article.tags
              : (typeof article.tags === 'string'
                  ? article.tags.split(',').map(t => t.trim()).filter(Boolean)
                  : []);
            if (tagList.length === 0) return null;
            return (
              <div className="article-tags-section">
                <div className="article-tags-list">
                  {tagList.map((tag) => (
                    <a
                      href={`/search?tag=${tag}`}
                      key={tag}
                      onClick={(e) => {
                        e.preventDefault();
                        navigate(`/search?tag=${tag}`);
                      }}
                      className="article-tag-pill"
                    >
                      {tag}
                    </a>
                  ))}
                </div>
              </div>
            );
          })()}
        </section>
      </article>
      <section className="comments-section page-container" id="comments-title" aria-labelledby="comments-title">
        <div className="comments-section-header">
          <h2 style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', margin: 0, fontSize: '24px', fontWeight: '800' }}>
            Comments
            <span className="detail-comment-count-badge-inline" style={{
              fontSize: '14px',
              fontWeight: '600',
              padding: '2px 8px',
              borderRadius: '12px',
              backgroundColor: '#f4f4f5',
              border: '1px solid var(--border)',
              color: 'var(--muted)',
            }}>
              {commentCount}
            </span>
          </h2>
          <div className="comments-filters" style={{ margin: 0 }}>
            <label htmlFor="commentSortBy">Sort by:</label>
            <select id="commentSortBy" value={commentSortBy} onChange={(e) => setCommentSortBy(e.target.value)}>
              <option value="NEWEST">Newest</option>
              <option value="OLDEST">Oldest</option>
              <option value="MOST_CLAPS">Most Clapped</option>
            </select>
          </div>
        </div>

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
            mutedUserIds={mutedUserIds}
            onPin={handleCommentPinned}
            onUnpin={handleCommentUnpinned}
            onClap={handleCommentClap}
            onUndoClap={handleCommentUndoClap}
          />
          {hasMoreComments && (
            <button className="text-button" disabled={loadingMore} type="button" onClick={handleLoadMoreComments} style={{ marginTop: '1rem', width: '100%' }}>
              {loadingMore ? 'Loading more comments...' : 'Load more comments'}
            </button>
          )}
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
