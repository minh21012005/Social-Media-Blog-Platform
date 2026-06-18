import { useEffect, useRef, useState, useCallback } from 'react'
import { categories } from '../data/editorial'
import { BellIcon, SearchIcon } from './icons'
import { getMyNotifications, markNotificationRead } from '../services/notifications'

export function SiteHeader({ session, navigate, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const [searchOpen, setSearchOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [notifOpen, setNotifOpen] = useState(false)
  const [notifications, setNotifications] = useState([])
  const menuRef = useRef(null)
  const searchRef = useRef(null)
  const notifRef = useRef(null)

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

  const fetchNotifications = useCallback(async () => {
    if (!session?.token) return
    try {
      const data = await getMyNotifications(session.token)
      setNotifications(data ?? [])
    } catch {
      // lỗi mạng: giữ nguyên danh sách cũ, không crash UI
    }
  }, [session?.token])

  // Tải notification khi đăng nhập; tự động làm mới mỗi 30 giây
  useEffect(() => {
    fetchNotifications()
    const timer = setInterval(fetchNotifications, 30_000)
    return () => clearInterval(timer)
  }, [fetchNotifications])

  // Đóng notification dropdown khi click bên ngoài
  useEffect(() => {
    if (!notifOpen) return undefined
    const close = (e) => {
      if (!notifRef.current?.contains(e.target)) setNotifOpen(false)
    }
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [notifOpen])

  const handleMarkRead = async (notifId) => {
    if (!session?.token) return
    try {
      await markNotificationRead(notifId, session.token)
      setNotifications((prev) =>
        prev.map((n) => (n.id === notifId ? { ...n, status: 'READ' } : n))
      )
    } catch {
      // bỏ qua lỗi đơn lẻ
    }
  }

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
        <div className="header-notif" ref={notifRef}>
          <button
            aria-expanded={notifOpen}
            aria-label="Notifications"
            className="icon-button notif-bell"
            type="button"
            onClick={() => setNotifOpen((c) => !c)}
          >
            <BellIcon />
            {notifications.filter((n) => n.status === 'UNREAD').length > 0 && (
              <span className="notif-badge">
                {Math.min(notifications.filter((n) => n.status === 'UNREAD').length, 99)}
              </span>
            )}
          </button>
          {notifOpen && (
            <div className="notif-dropdown">
              <div className="notif-dropdown-header">
                <span>Thông báo</span>
                {notifications.filter((n) => n.status === 'UNREAD').length > 0 && (
                  <span className="notif-unread-count">
                    {notifications.filter((n) => n.status === 'UNREAD').length} chưa đọc
                  </span>
                )}
              </div>
              <ul className="notif-list">
                {notifications.length === 0 ? (
                  <li className="notif-empty">Bạn chưa có thông báo nào</li>
                ) : (
                  notifications.map((n) => (
                    <li
                      key={n.id}
                      className={`notif-item${n.status === 'UNREAD' ? ' notif-item--unread' : ''}`}
                      role="button"
                      tabIndex={0}
                      onClick={() => handleMarkRead(n.id)}
                      onKeyDown={(e) => e.key === 'Enter' && handleMarkRead(n.id)}
                    >
                      {n.status === 'UNREAD' && <span className="notif-dot" aria-hidden="true" />}
                      <div className="notif-content">
                        <p className="notif-title">{n.title}</p>
                        {n.body && <p className="notif-body">{n.body}</p>}
                        <time className="notif-time" dateTime={n.createdAt}>
                          {new Date(n.createdAt).toLocaleString('vi-VN', {
                            day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
                          })}
                        </time>
                      </div>
                    </li>
                  ))
                )}
              </ul>
            </div>
          )}
        </div>
        {session ? (
          <>
            <button className="header-link-button" type="button" onClick={() => navigate('/articles/me')}>My articles</button>
            <button className="header-link-button" type="button" onClick={() => navigate('/follow-lab')}>Follow lab</button>
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
