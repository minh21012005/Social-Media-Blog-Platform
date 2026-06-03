import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { SocialIcon } from '../components/icons'
import { authors } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'
import { followUser, getFollowCounts, getFollowStatus, unfollowUser } from '../services/follows'
import { getPublicUserByUsername } from '../services/users'

export function AuthorPage({ username, navigate, session, requestWithAuth, notify }) {
  const [state, setState] = useState({ loading: true, author: null, articles: [], error: '' })
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
        const [page, counts] = await Promise.all([
          listPublishedArticles({ authorId: author.id, size: 12 }),
          getFollowCounts(author.id).catch(() => null),
        ])
        if (active) {
          setState({ loading: false, author, articles: page.items, error: '' })
          setFollowState((current) => ({
            ...current,
            followers: counts?.followers ?? 0,
            followingCount: counts?.following ?? 0,
            error: '',
          }))
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, author: null, articles: [], error: error.message, page: 0, totalPages: 0 })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [username])

  useEffect(() => {
    let active = true
    async function loadStatus() {
      if (!session || !state.author?.id || session.user.id === state.author.id) {
        return
      }
      setFollowState((current) => ({ ...current, loading: true }))
      try {
        const status = await requestWithAuth((token) => getFollowStatus(state.author.id, token))
        if (active) {
          setFollowState((current) => ({ ...current, loading: false, following: status.following, error: '' }))
        }
      } catch (error) {
        if (active) {
          setFollowState((current) => ({ ...current, loading: false, error: error.message }))
        }
      }
    }
    loadStatus()
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
      const counts = await getFollowCounts(author.id).catch(() => null)
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
          <img alt="" src={author?.avatarUrl || authors.sarah.avatar} />
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
        <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
      </section>

      <SiteFooter />
    </main>
  )
}
