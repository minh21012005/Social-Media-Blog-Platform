import { useEffect, useState } from 'react'
import { MediaUploader } from '../components/MediaUploader'
import { SiteFooter } from '../components/SiteFooter'
import { authors } from '../data/editorial'
import { updateProfile, uploadAvatar, getPublicUsers } from '../services/users'
import { getFollowCounts, listFollowers, listFollowing, listBlockedUsers, followUser, unfollowUser, unblockUser, listPendingFollowRequests, acceptFollowRequest, rejectFollowRequest, listMutedUsers, unmuteUser } from '../services/follows'

export function ProfilePage({ session, requestWithAuth, onProfileUpdated, notify, mutedUserIds = new Set(), onMutedUsersChanged }) {
  const [form, setForm] = useState({
    displayName: session.user.displayName || '',
    bio: session.user.bio || '',
    isPrivate: session.user.isPrivate || false,
  })
  const [uploading, setUploading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  // Follow tabs
  const [activeTab, setActiveTab] = useState('about')
  const [counts, setCounts] = useState({ followers: 0, following: 0 })
  const [tabData, setTabData] = useState({ users: [], profiles: new Map(), page: 0, total: 0, loading: false })
  const [tabBusy, setTabBusy] = useState({})

  const update = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }))
    setError('')
  }

  const upload = async (file) => {
    setError('')
    setUploading(true)
    try {
      const response = await requestWithAuth((token) => uploadAvatar(file, token))
      onProfileUpdated(response.user)
      notify?.('Your avatar has been updated.', { title: 'Profile saved', type: 'success' })
    } catch (requestError) {
      const message = requestError.message || 'Could not upload avatar.'
      setError(message)
      notify?.(message, { title: 'Upload failed' })
    } finally {
      setUploading(false)
    }
  }

  const save = async (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      const user = await requestWithAuth((token) => updateProfile({
        displayName: form.displayName,
        bio: form.bio,
        avatarUrl: session.user.avatarUrl,
        isPrivate: form.isPrivate,
      }, token))
      onProfileUpdated(user)
      notify?.('Your public profile has been updated.', { title: 'Profile saved', type: 'success' })
    } catch (requestError) {
      const message = requestError.message || 'Could not update profile.'
      setError(message)
      notify?.(message, { title: 'Save failed' })
    } finally {
      setSaving(false)
    }
  }

  // Load counts
  useEffect(() => {
    let active = true
    async function loadCounts() {
      try {
        const result = await requestWithAuth((token) => getFollowCounts(session.user.id, token))
        if (active) setCounts({ followers: result.followers, following: result.following })
      } catch {
        // ignore
      }
    }
    loadCounts()
    return () => { active = false }
  }, [requestWithAuth, session.user.id])

  // Load tab data
  useEffect(() => {
    if (['about', 'settings'].includes(activeTab)) {
      return
    }
    let active = true
    async function loadTab() {
      setTabData((prev) => ({ ...prev, loading: true }))
      try {
        let page
        if (activeTab === 'followers') {
          page = await requestWithAuth((token) => listFollowers(session.user.id, { page: 0, size: 50 }, token))
        } else if (activeTab === 'following') {
          page = await requestWithAuth((token) => listFollowing(session.user.id, { page: 0, size: 50 }, token))
        } else if (activeTab === 'requests') {
          page = await requestWithAuth((token) => listPendingFollowRequests({ page: 0, size: 50 }, token))
        } else if (activeTab === 'muted') {
          page = await requestWithAuth((token) => listMutedUsers({ page: 0, size: 50 }, token))
        } else {
          page = await requestWithAuth((token) => listBlockedUsers({ page: 0, size: 50 }, token))
        }
        const ids = (page?.users || []).map((u) => u.userId)
        let profiles = new Map()
        if (ids.length > 0) {
          try {
            profiles = await getPublicUsers(ids)
          } catch {
            // ignore
          }
        }
        if (active) {
          setTabData({ users: page?.users || [], profiles, page: 0, total: page?.total || 0, loading: false })
        }
      } catch {
        if (active) setTabData({ users: [], profiles: new Map(), page: 0, total: 0, loading: false })
      }
    }
    loadTab()
    return () => { active = false }
  }, [activeTab, requestWithAuth, session.user.id])

  const handleFollowToggle = async (userId, isFollowing) => {
    setTabBusy((prev) => ({ ...prev, [userId]: true }))
    try {
      if (isFollowing) {
        await requestWithAuth((token) => unfollowUser(userId, token))
      } else {
        await requestWithAuth((token) => followUser(userId, token))
      }
      // Refresh tab
      setActiveTab((t) => t) // force re-render
      const newCounts = await requestWithAuth((token) => getFollowCounts(session.user.id, token)).catch(() => null)
      if (newCounts) setCounts({ followers: newCounts.followers, following: newCounts.following })

      // Reload current tab data
      let page
      if (activeTab === 'followers') {
        page = await requestWithAuth((token) => listFollowers(session.user.id, { page: 0, size: 50 }, token))
      } else {
        page = await requestWithAuth((token) => listFollowing(session.user.id, { page: 0, size: 50 }, token))
      }
      const ids = (page?.users || []).map((u) => u.userId)
      let profiles = new Map()
      if (ids.length > 0) {
        try { profiles = await getPublicUsers(ids) } catch { /* ignore */ }
      }
      setTabData({ users: page?.users || [], profiles, page: 0, total: page?.total || 0, loading: false })
    } catch (err) {
      notify?.(err.message, { title: 'Action failed' })
    } finally {
      setTabBusy((prev) => ({ ...prev, [userId]: false }))
    }
  }

  const handleUnblock = async (userId) => {
    setTabBusy((prev) => ({ ...prev, [userId]: true }))
    try {
      await requestWithAuth((token) => unblockUser(userId, token))
      notify?.('User has been unblocked', { title: 'Unblocked', type: 'success' })
      // Reload blocked tab
      const page = await requestWithAuth((token) => listBlockedUsers({ page: 0, size: 50 }, token))
      const ids = (page?.users || []).map((u) => u.userId)
      let profiles = new Map()
      if (ids.length > 0) {
        try { profiles = await getPublicUsers(ids) } catch { /* ignore */ }
      }
      setTabData({ users: page?.users || [], profiles, page: 0, total: page?.total || 0, loading: false })
    } catch (err) {
      notify?.(err.message, { title: 'Unblock failed' })
    } finally {
      setTabBusy((prev) => ({ ...prev, [userId]: false }))
    }
  }

  const handleAcceptRequest = async (followerId) => {
    setTabBusy((prev) => ({ ...prev, [followerId]: true }))
    try {
      await requestWithAuth((token) => acceptFollowRequest(followerId, token))
      notify?.('Follow request accepted', { title: 'Accepted', type: 'success' })
      
      // Update follow counts
      const newCounts = await requestWithAuth((token) => getFollowCounts(session.user.id, token)).catch(() => null)
      if (newCounts) setCounts({ followers: newCounts.followers, following: newCounts.following })

      // Reload requests tab
      const page = await requestWithAuth((token) => listPendingFollowRequests({ page: 0, size: 50 }, token))
      const ids = (page?.users || []).map((u) => u.userId)
      let profiles = new Map()
      if (ids.length > 0) {
        try { profiles = await getPublicUsers(ids) } catch { /* ignore */ }
      }
      setTabData({ users: page?.users || [], profiles, page: 0, total: page?.total || 0, loading: false })
    } catch (err) {
      notify?.(err.message, { title: 'Action failed' })
    } finally {
      setTabBusy((prev) => ({ ...prev, [followerId]: false }))
    }
  }

  const handleRejectRequest = async (followerId) => {
    setTabBusy((prev) => ({ ...prev, [followerId]: true }))
    try {
      await requestWithAuth((token) => rejectFollowRequest(followerId, token))
      notify?.('Follow request rejected', { title: 'Rejected', type: 'success' })

      // Reload requests tab
      const page = await requestWithAuth((token) => listPendingFollowRequests({ page: 0, size: 50 }, token))
      const ids = (page?.users || []).map((u) => u.userId)
      let profiles = new Map()
      if (ids.length > 0) {
        try { profiles = await getPublicUsers(ids) } catch { /* ignore */ }
      }
      setTabData({ users: page?.users || [], profiles, page: 0, total: page?.total || 0, loading: false })
    } catch (err) {
      notify?.(err.message, { title: 'Action failed' })
    } finally {
      setTabBusy((prev) => ({ ...prev, [followerId]: false }))
    }
  }

  const handleUnmute = async (userId) => {
    setTabBusy((prev) => ({ ...prev, [userId]: true }))
    try {
      await requestWithAuth((token) => unmuteUser(userId, token))
      notify?.('User has been unmuted', { title: 'Unmuted', type: 'success' })

      // Update global mutedUserIds state
      const nextMuted = new Set(mutedUserIds)
      nextMuted.delete(userId)
      onMutedUsersChanged?.(nextMuted)

      // Reload muted tab
      const page = await requestWithAuth((token) => listMutedUsers({ page: 0, size: 50 }, token))
      const ids = (page?.users || []).map((u) => u.userId)
      let profiles = new Map()
      if (ids.length > 0) {
        try { profiles = await getPublicUsers(ids) } catch { /* ignore */ }
      }
      setTabData({ users: page?.users || [], profiles, page: 0, total: page?.total || 0, loading: false })
    } catch (err) {
      notify?.(err.message, { title: 'Unmute failed' })
    } finally {
      setTabBusy((prev) => ({ ...prev, [userId]: false }))
    }
  }

  return (
    <main>
      <section className="profile-page">
        <div className="profile-header-section page-container">
          <div className="profile-header-main">
            <div className="profile-header-info">
              <span className="form-eyebrow" style={{ display: 'inline-block', marginBottom: '8px' }}>
                {session.user.isPrivate ? 'Private profile' : 'Public profile'}
              </span>
              <h1>{session.user.displayName}</h1>
              {session.user.username && (
                <span className="profile-username" style={{ color: 'var(--muted)', fontSize: '15px', fontWeight: '400', display: 'block', marginTop: '4px' }}>
                  @{session.user.username}
                </span>
              )}
              <p className="profile-bio-snippet" style={{ color: 'var(--muted)', fontSize: '16px', lineHeight: '1.5', margin: '14px 0 16px', maxWidth: '600px' }}>
                {session.user.bio || 'Add a short bio so readers can recognize your voice across Chronicle.'}
              </p>
              <div className="profile-header-stats" style={{ display: 'flex', gap: '8px', fontSize: '14px', color: 'var(--ink)' }}>
                <button
                  type="button"
                  onClick={() => setActiveTab('followers')}
                  style={{ background: 'none', border: 'none', padding: 0, font: 'inherit', cursor: 'pointer', color: 'inherit' }}
                >
                  <strong style={{ fontWeight: '700' }}>{counts.followers}</strong> followers
                </button>
                <span className="dot-separator" style={{ color: 'var(--border)' }}>·</span>
                <button
                  type="button"
                  onClick={() => setActiveTab('following')}
                  style={{ background: 'none', border: 'none', padding: 0, font: 'inherit', cursor: 'pointer', color: 'inherit' }}
                >
                  <strong style={{ fontWeight: '700' }}>{counts.following}</strong> following
                </button>
              </div>
            </div>
            <div className="profile-header-avatar-container" style={{ flexShrink: 0 }}>
              <img alt="" className="profile-avatar-large" src={session.user.avatarUrl || authors.sarah.avatar} style={{ width: '120px', height: '120px', borderRadius: '50%', objectFit: 'cover', border: '1px solid var(--border)' }} />
            </div>
          </div>

          <div className="profile-tabs">
            {[
              { id: 'about', label: 'About' },
              { id: 'settings', label: 'Edit Profile' },
              { id: 'followers', label: `Followers (${counts.followers})` },
              { id: 'following', label: `Following (${counts.following})` },
              ...(session.user.isPrivate ? [{ id: 'requests', label: 'Requests' }] : []),
              { id: 'muted', label: 'Muted' },
              { id: 'blocked', label: 'Blocked' }
            ].map((tab) => (
              <button
                key={tab.id}
                className={`profile-tab${activeTab === tab.id ? ' profile-tab--active' : ''}`}
                type="button"
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        <section className="profile-body-section page-container" style={{ minHeight: '300px', paddingBottom: '80px', marginTop: '32px' }}>
          {activeTab === 'about' && (
            <div className="profile-about-tab" style={{ maxWidth: '680px' }}>
              <div className="profile-about-card" style={{ background: 'rgba(255,255,255,0.7)', border: '1px solid var(--border)', padding: '24px', borderRadius: '8px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: '800', marginBottom: '12px', color: 'var(--ink)' }}>Bio</h3>
                <p style={{ fontSize: '15px', color: 'var(--ink)', lineHeight: '1.6', whiteSpace: 'pre-wrap' }}>
                  {session.user.bio || 'No bio written yet. Click on "Edit Profile" tab to add one!'}
                </p>
              </div>
            </div>
          )}

          {activeTab === 'settings' && (
            <div className="profile-settings-tab">
              <div className="profile-settings-grid">
                <div className="profile-settings-form-wrapper">
                  <form className="profile-form" onSubmit={save}>
                    <label>
                      Display name
                      <input maxLength="80" required value={form.displayName} onChange={update('displayName')} />
                    </label>
                    <label>
                      Bio
                      <textarea maxLength="500" rows="5" value={form.bio} onChange={update('bio')} />
                    </label>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer', margin: '10px 0 16px', fontWeight: '500', color: 'var(--ink)' }}>
                      <input
                        type="checkbox"
                        style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--copper)' }}
                        checked={form.isPrivate}
                        onChange={(e) => setForm((c) => ({ ...c, isPrivate: e.target.checked }))}
                      />
                      Private account (requires approval for new followers)
                    </label>
                    {error && <p className="form-error">{error}</p>}
                    <div className="profile-actions">
                      <button className="submit-button" disabled={saving} type="submit">
                        {saving ? 'Saving...' : 'Save profile'}
                      </button>
                    </div>
                  </form>
                </div>
                <div className="profile-settings-avatar-wrapper">
                  <span className="editor-field-label" style={{ display: 'block', fontWeight: '780', fontSize: '15px', marginBottom: '12px' }}>Profile photo</span>
                  <img alt="" className="profile-avatar-medium" src={session.user.avatarUrl || authors.sarah.avatar} />
                  <div style={{ marginTop: '16px' }}>
                    <MediaUploader busy={uploading} label={session.user.avatarUrl ? 'Change photo' : 'Upload photo'} onUpload={upload} />
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* List Content */}
          {!['about', 'settings'].includes(activeTab) && (
            <div className="profile-list-tab">
              {tabData.loading ? (
                <div className="loading-state">Loading...</div>
              ) : tabData.users.length === 0 ? (
                <div className="empty-state">
                  <p>{activeTab === 'followers' ? 'No followers yet.' : activeTab === 'following' ? 'Not following anyone yet.' : activeTab === 'requests' ? 'No pending follow requests.' : activeTab === 'muted' ? 'No muted users.' : 'No blocked users.'}</p>
                </div>
              ) : (
                <div className="follow-user-list">
                  {tabData.users.map((item) => {
                    const profile = tabData.profiles?.get(item.userId)
                    const isBusy = tabBusy[item.userId]
                    return (
                      <div className="follow-user-row" key={item.userId}>
                        <a className="follow-user-info" href={profile?.username ? `/author/${profile.username}` : '#'} onClick={(e) => { e.preventDefault(); if (profile?.username) window.history.pushState({}, '', `/author/${profile.username}`); window.dispatchEvent(new PopStateEvent('popstate')) }}>
                          <img alt="" className="follow-user-avatar" src={profile?.avatarUrl || authors.sarah.avatar} />
                          <div className="follow-user-text" style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                            <strong>{profile?.displayName || profile?.username || 'Unknown'}</strong>
                            <span>{profile?.username ? `@${profile.username}` : ''}</span>
                          </div>
                        </a>
                        <div className="follow-user-actions">
                          {activeTab === 'blocked' ? (
                            <button
                              className="outline-pill compact"
                              disabled={isBusy}
                              type="button"
                              onClick={() => handleUnblock(item.userId)}
                            >
                              {isBusy ? 'Unblocking...' : 'Unblock'}
                            </button>
                          ) : activeTab === 'following' ? (
                            <button
                              className="outline-pill compact"
                              disabled={isBusy}
                              type="button"
                              onClick={() => handleFollowToggle(item.userId, true)}
                            >
                              {isBusy ? 'Updating...' : 'Unfollow'}
                            </button>
                          ) : activeTab === 'requests' ? (
                            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                              <button
                                className="submit-button compact"
                                style={{ margin: 0, padding: '4px 12px', fontSize: '12px' }}
                                disabled={isBusy}
                                type="button"
                                onClick={() => handleAcceptRequest(item.userId)}
                              >
                                {isBusy ? 'Accepting...' : 'Accept'}
                              </button>
                              <button
                                className="outline-pill compact"
                                disabled={isBusy}
                                type="button"
                                onClick={() => handleRejectRequest(item.userId)}
                              >
                                {isBusy ? 'Rejecting...' : 'Reject'}
                              </button>
                            </div>
                          ) : activeTab === 'muted' ? (
                            <button
                              className="outline-pill compact"
                              disabled={isBusy}
                              type="button"
                              onClick={() => handleUnmute(item.userId)}
                            >
                              {isBusy ? 'Unmuting...' : 'Unmute'}
                            </button>
                          ) : null}
                          <time>{item.followedAt ? new Date(item.followedAt).toLocaleDateString() : ''}</time>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          )}
        </section>
      </section>

      <SiteFooter />
    </main>
  )
}
