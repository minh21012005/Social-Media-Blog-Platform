import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { clearAuth, isAccessTokenExpiring, loadAuth, refreshAuth, saveAuth } from './services/auth'
import { apiRequest } from './services/api'
import { useRoute } from './hooks/useRoute'
import { SiteHeader } from './components/SiteHeader'
import { ToastStack } from './components/ToastStack'
import { HomePage } from './pages/HomePage'
import { CategoryPage } from './pages/CategoryPage'
import { AuthorPage } from './pages/AuthorPage'
import { AuthPage } from './pages/AuthPage'
import { ArticleDetailPage } from './pages/ArticleDetailPage'
import { EditArticlePage } from './pages/EditArticlePage'
import { MyArticlesPage } from './pages/MyArticlesPage'
import { ProfilePage } from './pages/ProfilePage'
import { SearchPage } from './pages/SearchPage'
import { WritePage } from './pages/WritePage'
import './App.css'

function isProtectedRoute(route) {
  return route === '/write'
    || route === '/articles/me'
    || route === '/profile'
    || /^\/articles\/[^/]+\/edit$/.test(route)
}

function App() {
  const [route, navigate] = useRoute()
  const routeUrl = useMemo(() => new URL(route, window.location.origin), [route])
  const pathname = routeUrl.pathname
  const [auth, setAuth] = useState(loadAuth)
  const [authChecking, setAuthChecking] = useState(() => Boolean(loadAuth()?.accessToken || isProtectedRoute(window.location.pathname)))
  const [toasts, setToasts] = useState([])
  const authRestoreInFlight = useRef(null)

  const session = useMemo(() => {
    if (!auth?.accessToken || !auth?.user) {
      return null
    }
    return auth
  }, [auth])

  const restoreSessionFromRefresh = useCallback(async () => {
    if (!authRestoreInFlight.current) {
      authRestoreInFlight.current = refreshAuth()
        .then((refreshed) => {
          setAuth(refreshed)
          return refreshed
        })
        .catch((error) => {
          clearAuth()
          setAuth(null)
          throw error
        })
        .finally(() => {
          authRestoreInFlight.current = null
        })
    }
    return authRestoreInFlight.current
  }, [])

  useEffect(() => {
    let active = true

    async function verifySession() {
      const storedAuth = loadAuth()
      if (!storedAuth?.accessToken) {
        if (isProtectedRoute(window.location.pathname) && !authRestoreInFlight.current) {
          await restoreSessionFromRefresh().catch(() => null)
        }
        if (active) {
          setAuthChecking(false)
        }
        return
      }

      try {
        const user = await apiRequest('/api/v1/users/me', { token: storedAuth.accessToken })
        if (!active) {
          return
        }
        const verifiedAuth = saveAuth({ ...storedAuth, user })
        setAuth(verifiedAuth)
      } catch {
        try {
          await restoreSessionFromRefresh()
        } catch {
          if (!active) {
            return
          }
          clearAuth()
          setAuth(null)
        }
      } finally {
        if (active) {
          setAuthChecking(false)
        }
      }
    }

    verifySession()

    return () => {
      active = false
    }
  }, [restoreSessionFromRefresh])

  useEffect(() => {
    if (!isProtectedRoute(route) || session || authChecking || authRestoreInFlight.current) {
      return
    }

    let active = true
    setAuthChecking(true)

    restoreSessionFromRefresh()
      .catch(() => null)
      .finally(() => {
        if (active) {
          setAuthChecking(false)
        }
      })

    return () => {
      active = false
    }
  }, [authChecking, restoreSessionFromRefresh, route, session])

  const handleAuthenticated = (authResponse) => {
    const savedAuth = saveAuth(authResponse)
    setAuth(savedAuth)
    navigate('/')
  }

  const notify = useCallback((message, options = {}) => {
    const id = crypto.randomUUID()
    setToasts((current) => [
      ...current,
      {
        id,
        message,
        title: options.title,
        type: options.type || 'error',
      },
    ])
    window.setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, options.duration || 5200)
  }, [])

  const dismissToast = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const requestWithAuth = useCallback(async (request) => {
    let activeAuth = auth

    if (!activeAuth?.accessToken) {
      try {
        activeAuth = await restoreSessionFromRefresh()
      } catch (refreshError) {
        navigate('/login')
        throw refreshError
      }
    }

    if (isAccessTokenExpiring(activeAuth)) {
      try {
        activeAuth = await restoreSessionFromRefresh()
      } catch (refreshError) {
        navigate('/login')
        throw refreshError
      }
    }

    try {
      return await request(activeAuth.accessToken)
    } catch (error) {
      if (error.status !== 401) {
        throw error
      }
      try {
        const refreshed = await restoreSessionFromRefresh()
        return request(refreshed.accessToken)
      } catch (refreshError) {
        navigate('/login')
        throw refreshError
      }
    }
  }, [auth, navigate, restoreSessionFromRefresh])

  const handleLogout = async () => {
    await apiRequest('/api/v1/auth/logout', {
      method: 'POST',
      token: auth?.accessToken,
      credentials: 'include',
    }).catch(() => null)

    clearAuth()
    setAuth(null)
    navigate('/')
  }

  const handleProfileUpdated = (user) => {
    const updatedAuth = saveAuth({ ...auth, user })
    setAuth(updatedAuth)
  }

  const protectedPage = (element) => {
    if (authChecking) {
      return <div className="loading-state page-container">Restoring your session...</div>
    }
    if (!session) {
      return <AuthPage mode="login" onDone={handleAuthenticated} navigate={navigate} />
    }
    return element
  }

  if (route === '/login') {
    return <AuthPage mode="login" onDone={handleAuthenticated} navigate={navigate} />
  }

  if (route === '/register') {
    return <AuthPage mode="register" onDone={handleAuthenticated} navigate={navigate} />
  }

  const renderPage = () => {
    if (pathname === '/write') {
      return protectedPage(<WritePage requestWithAuth={requestWithAuth} navigate={navigate} notify={notify} />)
    }

    if (pathname === '/articles/me') {
      return protectedPage(<MyArticlesPage requestWithAuth={requestWithAuth} navigate={navigate} notify={notify} />)
    }

    if (pathname === '/profile') {
      return protectedPage(<ProfilePage session={session} requestWithAuth={requestWithAuth} onProfileUpdated={handleProfileUpdated} notify={notify} />)
    }

    const editMatch = pathname.match(/^\/articles\/([^/]+)\/edit$/)
    if (editMatch) {
      return protectedPage(
        <EditArticlePage
          articleId={editMatch[1]}
          requestWithAuth={requestWithAuth}
          navigate={navigate}
          notify={notify}
        />
      )
    }

    const articleMatch = pathname.match(/^\/articles\/([^/]+)$/)
    if (articleMatch) {
      return <ArticleDetailPage slug={articleMatch[1]} navigate={navigate} />
    }

    if (pathname.startsWith('/author/')) {
      const username = pathname.replace('/author/', '')
      return <AuthorPage key={username} username={username} navigate={navigate} />
    }

    if (pathname.startsWith('/category/')) {
      const categorySlug = pathname.replace('/category/', '')
      return <CategoryPage key={categorySlug} slug={categorySlug} navigate={navigate} />
    }

    if (pathname === '/search') {
      const query = routeUrl.searchParams.get('q')?.trim() || ''
      return <SearchPage key={query} query={query} navigate={navigate} />
    }

    return <HomePage navigate={navigate} />
  }

  return (
    <>
      <SiteHeader session={session} navigate={navigate} onLogout={handleLogout} />
      <ToastStack onDismiss={dismissToast} toasts={toasts} />
      {renderPage()}
    </>
  )
}

export default App
