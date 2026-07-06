import { useEffect, useRef, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { SocialIcon } from '../components/icons'
import { authors } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'
import { blockUser, followUser, getBlockStatus, getFollowCounts, getFollowStatus, unfollowUser } from '../services/follows'
import { getPublicUserByUsername } from '../services/users'
import { getPresenceStatus } from '../services/presence'

export function AuthorPage({ username, navigate, session, requestWithAuth, notify, mutedUserIds = new Set(), onMuteToggle }) {
  const [state, setState] = useState({ loading: true, author: null, articles: [], error: '' })
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [followState, setFollowState] = useState({
    loading: false,
    busy: false,
    following: false,
    blocked: false,
    mutualFollow: false,
    pending: false,
    followers: 0,
    followingCount: 0,
    error: '',
  })
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)

  const canCallTargetApis = Boolean(state.author?.id)
  const isSelf = Boolean(session?.user?.id && state.author?.id && session.user.id === state.author.id)
  const isMuted = state.author ? mutedUserIds.has(state.author.id) : false

  // Close kebab menu on outside click
  useEffect(() => {
    if (!menuOpen) return undefined
    const close = (e) => {
      if (!menuRef.current?.contains(e.target)) setMenuOpen(false)
    }
    const closeEsc = (e) => {
      if (e.key === 'Escape') setMenuOpen(false)
    }
    document.addEventListener('mousedown', close)
    document.addEventListener('keydown', closeEsc)
    return () => {
      document.removeEventListener('mousedown', close)
      document.removeEventListener('keydown', closeEsc)
    }
  }, [menuOpen])

  useEffect(() => {
    let active = true
    async function load() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const author = await getPublicUserByUsername(username)
        const articlePage = await listPublishedArticles({ authorId: author.id, page, size: 12 })
        if (active) {
          setState({ loading: false, author, articles: articlePage.items, error: '' })
          setTotalPages(articlePage.totalPages ?? 0)
          setFollowState((current) => ({
            ...current,
            error: '',
          }))
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, author: null, articles: [], error: error.message })
          setTotalPages(0)
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [page, username])

  useEffect(() => {
    let active = true
    async function loadFollowSummary() {
      if (!session || !state.author?.id) {
        return
      }
      setFollowState((current) => ({ ...current, loading: true }))
      try {
        const [counts, status, blockStatus] = await requestWithAuth((token) => Promise.all([
          getFollowCounts(state.author.id, token),
          session.user.id === state.author.id ? null : getFollowStatus(state.author.id, token),
          session.user.id === state.author.id ? null : getBlockStatus(state.author.id, token),
        ]))
        if (active) {
          setFollowState((current) => ({
            ...current,
            loading: false,
            following: status?.following ?? false,
            blocked: blockStatus?.blocked ?? false,
            mutualFollow: status?.mutualFollow ?? false,
            pending: status?.pending ?? false,
            followers: counts.followers,
            followingCount: counts.following,
            error: '',
          }))
        }
      } catch (error) {
        if (active) {
          setFollowState((current) => ({ ...current, loading: false, error: error.message }))
        }
      }
    }
    loadFollowSummary()
    return () => {
      active = false
    }
  }, [requestWithAuth, session, state.author?.id])

  const toggleFollow = async () => {
    if (!session) {
      navigate('/login')
      return
    }
    if (!author?.id || followState.busy || followState.blocked) {
      return
    }
    setFollowState((current) => ({ ...current, busy: true, error: '' }))
    try {
      const relation = await requestWithAuth((token) => (
        (followState.following || followState.pending) ? unfollowUser(author.id, token) : followUser(author.id, token)
      ))
      const counts = await requestWithAuth((token) => getFollowCounts(author.id, token)).catch(() => null)
      const status = await requestWithAuth((token) => getFollowStatus(author.id, token)).catch(() => null)
      setFollowState((current) => ({
        ...current,
        busy: false,
        following: relation.following,
        pending: relation.pending,
        mutualFollow: status?.mutualFollow ?? false,
        followers: counts?.followers ?? current.followers + (relation.following ? 1 : -1),
        followingCount: counts?.following ?? current.followingCount,
      }))
    } catch (error) {
      setFollowState((current) => ({ ...current, busy: false, error: error.message }))
      notify?.(error.message, { title: 'Follow failed' })
    }
  }

  const doBlock = async () => {
    if (!session || !author?.id) return
    setMenuOpen(false)
    setFollowState((current) => ({ ...current, busy: true, error: '' }))
    try {
      await requestWithAuth((token) => blockUser(author.id, token))
      const counts = await requestWithAuth((token) => getFollowCounts(author.id, token)).catch(() => null)
      setFollowState((current) => ({
        ...current,
        busy: false,
        blocked: true,
        following: false,
        pending: false,
        mutualFollow: false,
        followers: counts?.followers ?? Math.max(0, current.followers - 1),
        followingCount: counts?.following ?? current.followingCount,
      }))
      notify?.('User has been blocked', { title: 'Blocked', type: 'success' })
    } catch (error) {
      setFollowState((current) => ({ ...current, busy: false, error: error.message }))
      notify?.(error.message, { title: 'Block failed' })
    }
  }

  const toggleMute = async () => {
    if (!session || !author?.id) return
    setMenuOpen(false)
    setFollowState((current) => ({ ...current, busy: true, error: '' }))
    try {
      const { muteUser, unmuteUser } = await import('../services/follows')
      if (isMuted) {
        await requestWithAuth((token) => unmuteUser(author.id, token))
        onMuteToggle?.(author.id, false)
        notify?.(`Unmuted @${author.username}`, { title: 'Unmuted', type: 'success' })
      } else {
        await requestWithAuth((token) => muteUser(author.id, token))
        onMuteToggle?.(author.id, true)
        notify?.(`Muted @${author.username}`, { title: 'Muted', type: 'success' })
      }
      setFollowState((current) => ({ ...current, busy: false }))
    } catch (error) {
      setFollowState((current) => ({ ...current, busy: false, error: error.message }))
      notify?.(error.message, { title: 'Mute failed' })
    }
  }

  const author = state.author
  const isOwnProfile = Boolean(session?.user?.id && author?.id && session.user.id === author.id)
  const isLocked = author?.isPrivate && !followState.following && !isOwnProfile

  return (
    <main>
      <section className="author-hero">
        <div className="page-container author-inner">
          <div style={{ position: 'relative', display: 'inline-block' }}>
            <img alt="" src={author?.avatarUrl || authors.sarah.avatar} />
          </div>
          <div>
            <div className="author-title-row">
              <h1>{author?.displayName || (state.loading ? 'Loading author' : 'Author not found')}</h1>
              {author && !isOwnProfile && session && (
                <div className="author-kebab-menu" ref={menuRef}>
                  <button
                    aria-expanded={menuOpen}
                    aria-label="More actions"
                    className="icon-button kebab-button"
                    type="button"
                    onClick={() => setMenuOpen((c) => !c)}
                  >
                    ⋯
                  </button>
                  {menuOpen && (
                    <div className="kebab-dropdown">
                      {followState.blocked ? (
                        <p className="kebab-info">This user is blocked. Manage blocked users in your <a href="/profile" onClick={(e) => { e.preventDefault(); setMenuOpen(false); navigate('/profile') }}>Profile settings</a>.</p>
                      ) : (
                        <>
                          <button type="button" className="kebab-danger" onClick={doBlock} disabled={followState.busy}>
                            Block @{author?.username}
                          </button>
                          <button type="button" className="kebab-item" onClick={toggleMute} disabled={followState.busy}>
                            {isMuted ? `Unmute @${author?.username}` : `Mute @${author?.username}`}
                          </button>
                        </>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
            <p>{author?.bio || 'An editorial voice writing across design, culture, technology, and lifestyle.'}</p>
            {author && (
              <div className="follow-summary" aria-label="Follow counts">
                <span><strong>{followState.followers}</strong> followers</span>
                <span><strong>{followState.followingCount}</strong> following</span>
                {followState.mutualFollow && (
                  <span className="mutual-badge">Follows you</span>
                )}
              </div>
            )}
            {author && !isOwnProfile && (
              <div className="follow-actions">
                {followState.blocked ? (
                  <span className="blocked-label">Blocked</span>
                ) : (
                  <button
                    className={followState.following || followState.pending ? 'outline-pill compact' : 'submit-button compact'}
                    disabled={followState.loading || followState.busy}
                    type="button"
                    onClick={toggleFollow}
                  >
                    {followState.busy ? 'Updating...' : followState.following ? 'Following' : followState.pending ? 'Requested' : 'Follow'}
                  </button>
                )}
                {followState.error && <span>{followState.error}</span>}
              </div>
            )}
            <div className="author-socials">
              <SocialIcon label="Profile" />
              <span>{author?.username ? `@${author.username}` : username}</span>
              <SocialIcon label="Website" />
              <SocialIcon label="Email" />
            </div>
          </div>
        </div>
      </section>

      <section className="author-articles page-container">
        <h2>Articles by {author?.displayName || ''}</h2>
        {state.loading && <div className="loading-state">Loading author stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load author.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && (
          isLocked ? (
            <div className="empty-state" style={{ padding: '60px 20px', border: '1px dashed var(--border)', borderRadius: '8px', textAlign: 'center' }}>
              <div style={{ fontSize: '48px', marginBottom: '16px' }}>🔒</div>
              <h2>This Account is Private</h2>
              <p>Follow @{author?.username} to see their articles.</p>
            </div>
          ) : (
            <>
              <ArticleList articles={state.articles} emptyText="This author has not published an article yet." navigate={navigate} />
              <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </>
          )
        )}
      </section>

      <SiteFooter />
    </main>
  )
}
