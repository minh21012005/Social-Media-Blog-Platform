import { useEffect, useRef, useState } from 'react'
import { categories } from '../data/editorial'
import { BellIcon, SearchIcon } from './icons'

export function SiteHeader({ session, navigate, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const [searchOpen, setSearchOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const menuRef = useRef(null)
  const searchRef = useRef(null)

  useEffect(() => {
    if (!menuOpen) {
      return undefined
    }

    const closeOnOutsideInteraction = (event) => {
      if (!menuRef.current?.contains(event.target)) {
        setMenuOpen(false)
      }
    }

    const closeOnEscape = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false)
      }
    }

    document.addEventListener('mousedown', closeOnOutsideInteraction)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeOnOutsideInteraction)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [menuOpen])

  useEffect(() => {
    if (!searchOpen) {
      return undefined
    }

    const closeOnOutsideInteraction = (event) => {
      if (!searchRef.current?.contains(event.target)) {
        setSearchOpen(false)
      }
    }

    const closeOnEscape = (event) => {
      if (event.key === 'Escape') {
        setSearchOpen(false)
      }
    }

    document.addEventListener('mousedown', closeOnOutsideInteraction)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeOnOutsideInteraction)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [searchOpen])

  const open = (path) => (event) => {
    event.preventDefault()
    setMenuOpen(false)
    navigate(path)
  }

  const submitSearch = (event) => {
    event.preventDefault()
    const normalized = searchTerm.trim()
    if (!normalized) {
      return
    }
    setSearchOpen(false)
    navigate(`/search?q=${encodeURIComponent(normalized)}`)
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
        <div className="header-search" ref={searchRef}>
          <button
            aria-expanded={searchOpen}
            aria-label="Search"
            className="icon-button"
            type="button"
            onClick={() => setSearchOpen((current) => !current)}
          >
            <SearchIcon />
          </button>
          {searchOpen && (
            <form className="header-search-panel" onSubmit={submitSearch}>
              <input
                autoFocus
                aria-label="Search stories"
                placeholder="Search stories"
                type="search"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
              />
              <button type="submit">Search</button>
            </form>
          )}
        </div>
        <button aria-label="Notifications" className="icon-button" type="button">
          <BellIcon />
        </button>
        {session ? (
          <>
            <button className="header-write-button" type="button" onClick={() => navigate('/write')}>Write</button>
            <div className="header-user-menu" ref={menuRef}>
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
                    navigate('/articles/me')
                  }}>
                    My articles
                  </button>
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
