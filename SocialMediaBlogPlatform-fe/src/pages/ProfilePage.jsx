import { useState } from 'react'
import { MediaUploader } from '../components/MediaUploader'
import { SiteFooter } from '../components/SiteFooter'
import { authors } from '../data/editorial'
import { uploadAvatar } from '../services/users'

export function ProfilePage({ session, requestWithAuth, onProfileUpdated }) {
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  const upload = async (file) => {
    setError('')
    setUploading(true)
    try {
      const response = await requestWithAuth((token) => uploadAvatar(file, token))
      onProfileUpdated(response.user)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploading(false)
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
            {error && <p className="form-error">{error}</p>}
          </div>
        </div>
      </section>
      <SiteFooter />
    </main>
  )
}
