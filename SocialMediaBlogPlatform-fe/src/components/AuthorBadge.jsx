export function AuthorBadge({ author, navigate }) {
  const open = (event) => {
    if (!navigate || !author?.username) {
      return
    }
    event.preventDefault()
    navigate(`/author/${author.username}`)
  }

  return (
    <a className="author-badge" href={author?.username ? `/author/${author.username}` : '#'} onClick={open}>
      <img alt="" src={author?.avatar} />
      <span>{author?.name || 'Chronicle Writer'}</span>
    </a>
  )
}
