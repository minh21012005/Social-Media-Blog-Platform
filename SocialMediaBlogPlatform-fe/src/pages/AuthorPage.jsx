import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { SocialIcon } from '../components/icons'
import { authors } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'
import { followUser, getFollowCounts, getFollowStatus, unfollowUser } from '../services/follows'
import { getPublicUserByUsername } from '../services/users'
import { getPresenceStatus } from '../services/presence'

export function AuthorPage({ username, navigate, session, requestWithAuth, notify }) {
  const [state, setState] = useState({ loading: true, author: null, articles: [], error: '' })
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [followState, setFollowState] = useState({
    loading: false,
    busy: false,
    following: false,
    followers: 0,
    followingCount: 0,
    error: '',
  })

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
        const [counts, status] = await requestWithAuth((token) => Promise.all([
          getFollowCounts(state.author.id, token),
          session.user.id === state.author.id ? null : getFollowStatus(state.author.id, token),
        ]))
        if (active) {
          setFollowState((current) => ({
            ...current,
            loading: false,
            following: status?.following ?? false,
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
    if (!author?.id || followState.busy) {
      return
    }
    setFollowState((current) => ({ ...current, busy: true, error: '' }))
    try {
      const relation = await requestWithAuth((token) => (
        followState.following ? unfollowUser(author.id, token) : followUser(author.id, token)
      ))
      const counts = await requestWithAuth((token) => getFollowCounts(author.id, token)).catch(() => null)
      setFollowState((current) => ({
        ...current,
        busy: false,
        following: relation.following,
        followers: counts?.followers ?? current.followers + (relation.following ? 1 : -1),
        followingCount: counts?.following ?? current.followingCount,
      }))
    } catch (error) {
      setFollowState((current) => ({ ...current, busy: false, error: error.message }))
      notify?.(error.message, { title: 'Follow failed' })
    }
  }

  const author = state.author
  const isOwnProfile = Boolean(session?.user?.id && author?.id && session.user.id === author.id)

  return (
    <main>
      <section className="author-hero">
        <div className="page-container author-inner">
          <div style={{ position: 'relative', display: 'inline-block' }}>
            <img alt="" src={author?.avatarUrl || authors.sarah.avatar} />
          </div>
          <div>
            <h1>{author?.displayName || (state.loading ? 'Loading author' : 'Author not found')}</h1>
            <p>{author?.bio || 'An editorial voice writing across design, culture, technology, and lifestyle.'}</p>
            {author && (
              <div className="follow-summary" aria-label="Follow counts">
                <span><strong>{followState.followers}</strong> followers</span>
                <span><strong>{followState.followingCount}</strong> following</span>
              </div>
            )}
            {author && !isOwnProfile && (
              <div className="follow-actions">
                <button className={followState.following ? 'outline-pill compact' : 'submit-button compact'} disabled={followState.loading || followState.busy} type="button" onClick={toggleFollow}>
                  {followState.busy ? 'Updating...' : followState.following ? 'Following' : 'Follow'}
                </button>
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
          <ArticleList articles={state.articles} emptyText="This author has not published an article yet." navigate={navigate} />
        )}
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </section>

      <SiteFooter />
    </main>
  )
}
