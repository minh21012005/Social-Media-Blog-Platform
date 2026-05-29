import { categories } from '../data/editorial'
import { SearchIcon } from './icons'

export function SiteHeader({ session, navigate, onLogout }) {
  const open = (path) => (event) => {
    event.preventDefault()
    navigate(path)
  }

  return (
    <header className="site-header">
      <a className="brand-wordmark" href="/" onClick={open('/')}>Chronicle</a>
      <nav aria-label="Primary navigation" className="site-nav">
        {categories.map((category) => (
          <a
            href={`/category/${category.slug}`}
            key={category.slug}
            onClick={open(`/category/${category.slug}`)}
          >
            {category.label}
          </a>
        ))}
      </nav>
      <div className="site-actions">
        <button aria-label="Search" className="icon-button" type="button">
          <SearchIcon />
        </button>
        {session ? (
          <>
            <button className="pill-button" type="button" onClick={() => navigate('/author/sarah-jenkins')}>
              {session.user.displayName}
            </button>
            <button className="text-button" type="button" onClick={onLogout}>Log out</button>
          </>
        ) : (
          <button className="pill-button" type="button" onClick={() => navigate('/register')}>
            Subscribe
          </button>
        )}
      </div>
    </header>
  )
}
