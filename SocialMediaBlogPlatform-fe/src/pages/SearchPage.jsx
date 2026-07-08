import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'
import { listPublishedArticles } from '../services/articles'
import { searchUsers } from '../services/users'
import { getFollowStatus, followUser, unfollowUser } from '../services/follows'
import { authors } from '../data/editorial'

export function SearchPage({ query, navigate, session, requestWithAuth, notify, mutedUserIds = new Set() }) {
  const [page, setPage] = useState(0)
  const [state, setState] = useState({ loading: true, articles: [], error: '', page: 0, totalPages: 0, totalItems: 0 })
  
  // User search state
  const [searchMode, setSearchMode] = useState('stories') // 'stories' or 'people'
  const [users, setUsers] = useState([])
  const [usersLoading, setUsersLoading] = useState(false)
  const [followStatuses, setFollowStatuses] = useState({})
  const [busyUsers, setBusyUsers] = useState({})

  // Load articles
  useEffect(() => {
    let active = true
    async function loadArticles() {
      setState((current) => ({ ...current, loading: true, error: '' }))
      try {
        const result = query
          ? await listPublishedArticles({ q: query, page, size: 12, sort: 'latest' })
          : { items: [], page: 0, totalPages: 0, totalItems: 0 }
        if (active) {
          setState({
            loading: false,
            articles: result.items,
            error: '',
            page: result.page || 0,
            totalPages: result.totalPages || 0,
            totalItems: result.totalItems || 0,
          })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, articles: [], error: error.message, page: 0, totalPages: 0, totalItems: 0 })
        }
      }
    }
    loadArticles()
    return () => {
      active = false
    }
  }, [page, query])

  // Load users and follow statuses
  useEffect(() => {
    let active = true
    async function loadUsers() {
      if (!query) {
        setUsers([])
        return
      }
      setUsersLoading(true)
      try {
        const matchingUsers = await searchUsers(query)
        if (!active) return

        setUsers(matchingUsers)

        if (session && matchingUsers.length > 0) {
          const statuses = await Promise.all(
            matchingUsers.map(async (u) => {
              if (session.user.id === u.id) {
                return [u.id, { following: false, pending: false }]
              }
              try {
                const status = await requestWithAuth((token) => getFollowStatus(u.id, token))
                return [u.id, status]
              } catch {
                return [u.id, { following: false, pending: false }]
              }
            })
          )
          if (active) {
            setFollowStatuses(Object.fromEntries(statuses))
          }
        }
      } catch (error) {
        console.error('Failed to search users:', error)
      } finally {
        if (active) {
          setUsersLoading(false)
        }
      }
    }

    loadUsers()
    return () => {
      active = false
    }
  }, [query, session, requestWithAuth])

  const handleFollowToggle = async (user) => {
    if (!session) {
      navigate('/login')
      return
    }
    const userId = user.id
    if (busyUsers[userId]) return

    setBusyUsers((prev) => ({ ...prev, [userId]: true }))
    const status = followStatuses[userId]
    const isFollowingOrPending = status?.following || status?.pending

    try {
      const relation = await requestWithAuth((token) =>
        isFollowingOrPending ? unfollowUser(userId, token) : followUser(userId, token)
      )
      setFollowStatuses((prev) => ({
        ...prev,
        [userId]: {
          ...prev[userId],
          following: relation.following,
          pending: relation.pending,
        },
      }))
      notify?.(
        relation.following
          ? `You are now following ${user.displayName}.`
          : relation.pending
          ? `Follow request sent to ${user.displayName}.`
          : `You unfollowed ${user.displayName}.`,
        { title: 'Success', type: 'success' }
      )
    } catch (error) {
      notify?.(error.message, { title: 'Follow failed' })
    } finally {
      setBusyUsers((prev) => ({ ...prev, [userId]: false }))
    }
  }

  const filteredArticles = state.articles.filter((article) => article.author && !mutedUserIds.has(article.author.id))

  return (
    <main>
      <section className="search-hero page-container" style={{ borderBottom: query ? 'none' : undefined, paddingBottom: query ? 0 : undefined }}>
        <span className="form-eyebrow">Search</span>
        <h1>{query ? `Results for "${query}"` : 'Search Chronicle.'}</h1>
        <p>
          {query
            ? `${state.totalItems} ${state.totalItems === 1 ? 'story' : 'stories'} and ${users.length} ${users.length === 1 ? 'person' : 'people'} found.`
            : 'Use the search icon above to find stories or people.'}
        </p>

        {/* Tabs */}
        {query && (
          <div className="profile-tabs" style={{ marginTop: '32px' }}>
            <button
              className={`profile-tab${searchMode === 'stories' ? ' profile-tab--active' : ''}`}
              type="button"
              onClick={() => setSearchMode('stories')}
            >
              Stories <span className="tab-count" style={{ marginLeft: '4px', fontSize: '12px', opacity: 0.7 }}>({state.totalItems})</span>
            </button>
            <button
              className={`profile-tab${searchMode === 'people' ? ' profile-tab--active' : ''}`}
              type="button"
              onClick={() => setSearchMode('people')}
            >
              People <span className="tab-count" style={{ marginLeft: '4px', fontSize: '12px', opacity: 0.7 }}>({users.length})</span>
            </button>
          </div>
        )}
      </section>

      <section className="page-container" style={{ minHeight: '300px' }}>
        {searchMode === 'stories' ? (
          <>
            {state.loading && <div className="loading-state">Searching stories...</div>}
            {state.error && <div className="empty-state"><h2>Search failed.</h2><p>{state.error}</p></div>}
            {!state.loading && !state.error && (
              <ArticleList
                articles={filteredArticles}
                emptyText="No published stories matched this search."
                navigate={navigate}
              />
            )}
            <Pagination page={state.page} totalPages={state.totalPages} onPageChange={setPage} />
          </>
        ) : (
          <>
            {usersLoading && <div className="loading-state">Searching people...</div>}
            {!usersLoading && users.length === 0 && (
              <div className="empty-state">
                <h2>No people found.</h2>
                <p>No users matched "{query}".</p>
              </div>
            )}
            {!usersLoading && users.length > 0 && (
              <div className="follow-user-list">
                {users.map((user) => {
                  const status = followStatuses[user.id]
                  const isBusy = busyUsers[user.id]
                  const isSelf = session?.user?.id === user.id

                  let followBtnText = 'Follow'
                  let followBtnClass = 'submit-button compact'

                  if (status?.following) {
                    followBtnText = 'Following'
                    followBtnClass = 'outline-pill compact'
                  } else if (status?.pending) {
                    followBtnText = 'Requested'
                    followBtnClass = 'outline-pill compact'
                  }

                  return (
                    <div className="follow-user-row" key={user.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 0', borderBottom: '1px solid var(--border)' }}>
                      <a
                        className="follow-user-info"
                        href={`/author/${user.username}`}
                        onClick={(e) => {
                          e.preventDefault()
                          navigate(`/author/${user.username}`)
                        }}
                        style={{ display: 'flex', gap: '16px', alignItems: 'center', textDecoration: 'none' }}
                      >
                        <img
                          alt=""
                          className="follow-user-avatar"
                          src={user.avatarUrl || authors.sarah.avatar}
                          style={{ width: '48px', height: '48px', borderRadius: '50%', objectFit: 'cover' }}
                        />
                        <div className="follow-user-text" style={{ display: 'flex', flexDirection: 'column' }}>
                          <strong style={{ color: 'var(--ink)' }}>{user.displayName}</strong>
                          <span style={{ color: 'var(--slate)', fontSize: '14px' }}>@{user.username}</span>
                          {user.bio && <span style={{ color: 'var(--slate)', fontSize: '13px', marginTop: '4px' }}>{user.bio}</span>}
                        </div>
                      </a>
                      
                      {!isSelf && (
                        <button
                          className={followBtnClass}
                          style={{ margin: 0 }}
                          disabled={isBusy}
                          type="button"
                          onClick={() => handleFollowToggle(user)}
                        >
                          {isBusy ? 'Loading...' : followBtnText}
                        </button>
                      )}
                    </div>
                  )
                })}
              </div>
            )}
          </>
        )}
      </section>

      <SiteFooter />
    </main>
  )
}
