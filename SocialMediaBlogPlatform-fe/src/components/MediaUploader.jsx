export function MediaUploader({ label = 'Upload image', busy, onUpload }) {
  const upload = async (event) => {
    const [file] = event.target.files || []
    if (!file) {
      return
    }
    await onUpload(file)
    event.target.value = ''
  }

  return (
    <label className="media-uploader">
      <span>{busy ? 'Uploading...' : label}</span>
      <input accept="image/*" disabled={busy} onChange={upload} type="file" />
    </label>
  )
}
