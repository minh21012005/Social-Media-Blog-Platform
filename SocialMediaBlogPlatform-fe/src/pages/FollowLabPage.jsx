import { useMemo, useState } from 'react'
import { SiteFooter } from '../components/SiteFooter'
import { followUser, getFollowCounts, getFollowStatus, listFollowers, listFollowing, unfollowUser } from '../services/follows'
import { getPublicUser, getPublicUserByUsername, getPublicUsers } from '../services/users'

const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function FollowLabPage({ session, requestWithAuth, notify }) {
  const [lookup, setLookup] = useState('')
  const [target, setTarget] = useState(null)
  const [busy, setBusy] = useState('')
  const [error, setError] = useState('')
  const [status, setStatus] = useState(null)
  const [counts, setCounts] = useState(null)
  const [followers, setFollowers] = useState(null)
  const [following, setFollowing] = useState(null)
  const [raw, setRaw] = useState(null)

  const canCallTargetApis = Boolean(target?.id)
  const isSelf = Boolean(session?.user?.id && target?.id && session.user.id === target.id)

  const targetLabel = useMemo(() => {
    if (!target) {
      return 'No target loaded'
    }
    return `${target.displayName || target.username} @${target.username}`
  }, [target])

  const run = async (label, action) => {
    setBusy(label)
    setError('')
    try {
      const result = await action()
      setRaw({ action: label, result })
      return result
    } catch (requestError) {
      setError(requestError.message)
      notify?.(requestError.message, { title: label })
      return null
    } finally {
      setBusy('')
    }
  }

  const loadTarget = async (event) => {
    event.preventDefault()
    const value = lookup.trim()
    if (!value) {
      setError('Enter a username or user id.')
      return
    }

    const user = await run('Load target', () => (
      uuidPattern.test(value) ? getPublicUser(value) : getPublicUserByUsername(value.replace(/^@/, ''))
    ))
    if (!user) {
      return
    }
    setTarget(user)
    setStatus(null)
    setCounts(null)
    setFollowers(null)
    setFollowing(null)
  }

  const refreshStatus = () => run('Get follow status', async () => {
    const result = await requestWithAuth((token) => getFollowStatus(target.id, token))
    setStatus(result)
    return result
  })

  const refreshCounts = () => run('Get follow counts', async () => {
    const result = await getFollowCounts(target.id)
    setCounts(result)
    return result
  })

  const loadFollowers = () => run('List followers', async () => {
    const page = await listFollowers(target.id, { page: 0, size: 20 })
    const profiles = await getProfilesForPage(page)
    const result = { ...page, profiles }
    setFollowers(result)
    return result
  })

  const loadFollowing = () => run('List following', async () => {
    const page = await listFollowing(target.id, { page: 0, size: 20 })
    const profiles = await getProfilesForPage(page)
    const result = { ...page, profiles }
    setFollowing(result)
    return result
  })

  const doFollow = () => run('Follow user', async () => {
    const result = await requestWithAuth((token) => followUser(target.id, token))
    setStatus({ viewerId: session.user.id, targetUserId: target.id, following: result.following })
    await refreshCounts()
    return result
  })

  const doUnfollow = () => run('Unfollow user', async () => {
    const result = await requestWithAuth((token) => unfollowUser(target.id, token))
    setStatus({ viewerId: session.user.id, targetUserId: target.id, following: result.following })
    await refreshCounts()
    return result
  })

  return (
    <main>
      <section className="writer-hero page-container">
        <span className="form-eyebrow">Follow API Lab</span>
        <h1>Test follow service APIs</h1>
        <p>Load a public user by username or UUID, then call follow, unfollow, status, counts, followers, and following through the gateway.</p>
      </section>

      <section className="follow-lab page-container">
        <form className="follow-lab-panel" onSubmit={loadTarget}>
          <label>
            Target username or user id
            <input placeholder="username or UUID" value={lookup} onChange={(event) => setLookup(event.target.value)} />
          </label>
          <button className="submit-button" disabled={busy === 'Load target'} type="submit">
            {busy === 'Load target' ? 'Loading...' : 'Load target'}
          </button>
        </form>

        <div className="follow-lab-grid">
          <section className="follow-lab-panel">
            <span className="form-eyebrow">Target</span>
            <h2>{targetLabel}</h2>
            {target && (
              <>
                <p>{target.id}</p>
                <div className="follow-summary">
                  <span><strong>{counts?.followers ?? '-'}</strong> followers</span>
                  <span><strong>{counts?.following ?? '-'}</strong> following</span>
                  <span><strong>{status ? (status.following ? 'Yes' : 'No') : '-'}</strong> followed by me</span>
                </div>
              </>
            )}
            {error && <p className="form-error">{error}</p>}
          </section>

          <section className="follow-lab-panel">
            <span className="form-eyebrow">Actions</span>
            <div className="follow-test-actions">
              <button className="submit-button compact" disabled={!canCallTargetApis || isSelf || Boolean(busy)} type="button" onClick={doFollow}>Follow</button>
              <button className="outline-pill compact" disabled={!canCallTargetApis || isSelf || Boolean(busy)} type="button" onClick={doUnfollow}>Unfollow</button>
              <button className="outline-pill compact" disabled={!canCallTargetApis || Boolean(busy)} type="button" onClick={refreshStatus}>Status</button>
              <button className="outline-pill compact" disabled={!canCallTargetApis || Boolean(busy)} type="button" onClick={refreshCounts}>Counts</button>
              <button className="outline-pill compact" disabled={!canCallTargetApis || Boolean(busy)} type="button" onClick={loadFollowers}>Followers</button>
              <button className="outline-pill compact" disabled={!canCallTargetApis || Boolean(busy)} type="button" onClick={loadFollowing}>Following</button>
            </div>
            {isSelf && <p className="field-error">Self-follow is blocked by the API, so follow/unfollow buttons are disabled for your own profile.</p>}
            {busy && busy !== 'Load target' && <p className="dashboard-stat">{busy}...</p>}
          </section>
        </div>

        <section className="follow-lab-grid">
          <FollowList title="Followers" page={followers} />
          <FollowList title="Following" page={following} />
        </section>

        <section className="follow-lab-panel">
          <span className="form-eyebrow">Last response</span>
          <pre className="api-response-preview">{raw ? JSON.stringify(raw, null, 2) : 'No API response yet.'}</pre>
        </section>
      </section>

      <SiteFooter />
    </main>
  )
}

function FollowList({ title, page }) {
  return (
    <section className="follow-lab-panel">
      <span className="form-eyebrow">{title}</span>
      <h2>{page ? `${page.total} users` : 'Not loaded'}</h2>
      <div className="follow-user-list">
        {(page?.users || []).map((item) => {
          const profile = page.profiles?.get(item.userId)
          return (
            <div className="follow-user-row" key={item.userId}>
              <div>
                <strong>{profile?.displayName || profile?.username || item.userId}</strong>
                <span>{profile?.username ? `@${profile.username}` : item.userId}</span>
              </div>
              <time>{item.followedAt ? new Date(item.followedAt).toLocaleString() : '-'}</time>
            </div>
          )
        })}
        {page && page.users.length === 0 && <p className="dashboard-stat">No users returned.</p>}
      </div>
    </section>
  )
}

async function getProfilesForPage(page) {
  const ids = (page?.users || []).map((item) => item.userId)
  try {
    return await getPublicUsers(ids)
  } catch {
    return new Map()
  }
}
