import { useState } from 'react'
import { categories } from '../data/editorial'
import { SearchIcon } from './icons'

export function SiteHeader({ session, navigate, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false)

  const open = (path) => (event) => {
    event.preventDefault()
    setMenuOpen(false)
    navigate(path)
  }

  const displayName = session?.user?.displayName || session?.user?.username || 'Profile'
  const initial = displayName.charAt(0).toUpperCase()

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
            <button className="header-link-button" type="button" onClick={() => navigate('/articles/me')}>My articles</button>
            <button className="header-write-button" type="button" onClick={() => navigate('/write')}>Write</button>
            <div className="header-user-menu">
              <button
                aria-expanded={menuOpen}
                className="header-user-button"
                type="button"
                onClick={() => setMenuOpen((current) => !current)}
              >
                <span>{initial}</span>
                {displayName}
              </button>
              {menuOpen && (
                <div className="header-dropdown">
                  <button type="button" onClick={() => {
                    setMenuOpen(false)
                    navigate('/profile')
                  }}>
                    Profile
                  </button>
                  <button type="button" onClick={() => {
                    setMenuOpen(false)
                    onLogout()
                  }}>
                    Log out
                  </button>
                </div>
              )}
            </div>
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
