import { useState } from 'react'
import { MediaUploader } from '../components/MediaUploader'
import { SiteFooter } from '../components/SiteFooter'
import { authors } from '../data/editorial'
import { updateProfile, uploadAvatar } from '../services/users'

export function ProfilePage({ session, requestWithAuth, onProfileUpdated }) {
  const [form, setForm] = useState({
    displayName: session.user.displayName || '',
    bio: session.user.bio || '',
  })
  const [uploading, setUploading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const update = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }))
  }

  const upload = async (file) => {
    setError('')
    setMessage('')
    setUploading(true)
    try {
      const response = await requestWithAuth((token) => uploadAvatar(file, token))
      onProfileUpdated(response.user)
      setMessage('Avatar updated.')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploading(false)
    }
  }

  const save = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')
    setSaving(true)
    try {
      const user = await requestWithAuth((token) => updateProfile({
        displayName: form.displayName,
        bio: form.bio,
        avatarUrl: session.user.avatarUrl,
      }, token))
      onProfileUpdated(user)
      setMessage('Profile updated.')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <main>
      <section className="author-hero">
        <div className="page-container author-inner">
          <img alt="" src={session.user.avatarUrl || authors.sarah.avatar} />
          <div>
            <span className="form-eyebrow">Profile</span>
            <h1>{session.user.displayName}</h1>
            <p>{session.user.bio || 'Add a distinct editorial identity through your avatar and profile metadata.'}</p>
            <MediaUploader busy={uploading} label="Upload avatar" onUpload={upload} />
            <form className="profile-form" onSubmit={save}>
              <label>
                Display name
                <input maxLength="80" required value={form.displayName} onChange={update('displayName')} />
              </label>
              <label>
                Bio
                <textarea maxLength="500" rows="4" value={form.bio} onChange={update('bio')} />
              </label>
              <button className="submit-button" disabled={saving} type="submit">
                {saving ? 'Saving...' : 'Save profile'}
              </button>
            </form>
            {error && <p className="form-error">{error}</p>}
            {message && <p className="form-success">{message}</p>}
          </div>
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
