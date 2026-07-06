import { useEffect, useState } from 'react'
import { MediaUploader } from '../components/MediaUploader'
import { SiteFooter } from '../components/SiteFooter'
import { authors } from '../data/editorial'
import { updateProfile, uploadAvatar } from '../services/users'
import { getFollowCounts } from '../services/follows'

export function ProfilePage({ session, requestWithAuth, onProfileUpdated, notify }) {
  const [form, setForm] = useState({
    displayName: session.user.displayName || '',
    bio: session.user.bio || '',
  })
  const [uploading, setUploading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [followCounts, setFollowCounts] = useState({ followers: 0, following: 0 })

  useEffect(() => {
    let active = true
    async function loadFollowSummary() {
      if (!session?.user?.id) return
      try {
        const counts = await requestWithAuth((token) => getFollowCounts(session.user.id, token))
        if (active) {
          setFollowCounts({ followers: counts.followers, following: counts.following })
        }
      } catch (err) {
        // silently ignore error on counts
      }
    }
    loadFollowSummary()
    return () => {
      active = false
    }
  }, [session?.user?.id, requestWithAuth])

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

  return (
    <main>
      <section className="profile-page">
        <div className="page-container profile-shell">
          <aside className="profile-card">
            <span className="form-eyebrow">Public profile</span>
            <img alt="" className="profile-avatar" src={session.user.avatarUrl || authors.sarah.avatar} />
            <h1>{session.user.displayName}</h1>
            <p>{session.user.bio || 'Add a short bio so readers can recognize your voice across Chronicle.'}</p>
            <div className="follow-summary" aria-label="Follow counts" style={{ marginBottom: '24px' }}>
              <span><strong>{followCounts.followers}</strong> followers</span>
              <span><strong>{followCounts.following}</strong> following</span>
            </div>
            <MediaUploader busy={uploading} label={session.user.avatarUrl ? 'Change avatar' : 'Upload avatar'} onUpload={upload} />
          </aside>

          <section className="profile-panel" aria-labelledby="profile-settings-title">
            <div className="profile-panel-header">
              <div>
                <span className="form-eyebrow">Account settings</span>
              </div>
            </div>

            <form className="profile-form" onSubmit={save}>
              <label>
                Display name
                <input maxLength="80" required value={form.displayName} onChange={update('displayName')} />
              </label>
              <label>
                Bio
                <textarea maxLength="500" rows="5" value={form.bio} onChange={update('bio')} />
              </label>
              {error && <p className="form-error">{error}</p>}
              <div className="profile-actions">
                <button className="submit-button" disabled={saving} type="submit">
                  {saving ? 'Saving...' : 'Save profile'}
                </button>
              </div>
            </form>
          </section>
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
