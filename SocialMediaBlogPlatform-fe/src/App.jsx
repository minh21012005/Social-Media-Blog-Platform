import { useCallback, useEffect, useMemo, useState } from 'react'
import { clearAuth, loadAuth, refreshAuth, saveAuth } from './services/auth'
import { apiRequest } from './services/api'
import { useRoute } from './hooks/useRoute'
import { SiteHeader } from './components/SiteHeader'
import { HomePage } from './pages/HomePage'
import { CategoryPage } from './pages/CategoryPage'
import { AuthorPage } from './pages/AuthorPage'
import { AuthPage } from './pages/AuthPage'
import { ArticleDetailPage } from './pages/ArticleDetailPage'
import { EditArticlePage } from './pages/EditArticlePage'
import { MyArticlesPage } from './pages/MyArticlesPage'
import { ProfilePage } from './pages/ProfilePage'
import { WritePage } from './pages/WritePage'
import './App.css'

function App() {
  const [route, navigate] = useRoute()
  const [auth, setAuth] = useState(loadAuth)
  const [authChecking, setAuthChecking] = useState(() => Boolean(loadAuth()?.accessToken))

  const session = useMemo(() => {
    if (!auth?.accessToken || !auth?.user) {
      return null
    }
    return auth
  }, [auth])

  useEffect(() => {
    const storedAuth = loadAuth()
    if (!storedAuth?.accessToken) {
      return
    }

    let active = true

    async function verifySession() {
      try {
        const user = await apiRequest('/api/v1/users/me', { token: storedAuth.accessToken })
        if (!active) {
          return
        }
        const verifiedAuth = { ...storedAuth, user }
        saveAuth(verifiedAuth)
        setAuth(verifiedAuth)
      } catch {
        try {
          const refreshed = await refreshAuth()
          if (!active) {
            return
          }
          saveAuth(refreshed)
          setAuth(refreshed)
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
  }, [])

  const handleAuthenticated = (authResponse) => {
    saveAuth(authResponse)
    setAuth(authResponse)
    navigate('/')
  }

  const requestWithAuth = useCallback(async (request) => {
    if (!auth?.accessToken) {
      navigate('/login')
      throw new Error('Please log in first')
    }

    try {
      return await request(auth.accessToken)
    } catch (error) {
      if (error.status !== 401) {
        throw error
      }
      try {
        const refreshed = await refreshAuth()
        saveAuth(refreshed)
        setAuth(refreshed)
        return request(refreshed.accessToken)
      } catch (refreshError) {
        clearAuth()
        setAuth(null)
        navigate('/login')
        throw refreshError
      }
    }
  }, [auth, navigate])

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
    const updatedAuth = { ...auth, user }
    saveAuth(updatedAuth)
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
    if (route === '/write') {
      return protectedPage(<WritePage session={session} requestWithAuth={requestWithAuth} navigate={navigate} />)
    }

    if (route === '/articles/me') {
      return protectedPage(<MyArticlesPage requestWithAuth={requestWithAuth} navigate={navigate} />)
    }

    if (route === '/profile') {
      return protectedPage(<ProfilePage session={session} requestWithAuth={requestWithAuth} onProfileUpdated={handleProfileUpdated} />)
    }

    const editMatch = route.match(/^\/articles\/([^/]+)\/edit$/)
    if (editMatch) {
      return protectedPage(
        <EditArticlePage
          articleId={editMatch[1]}
          session={session}
          requestWithAuth={requestWithAuth}
          navigate={navigate}
        />
      )
    }

    const articleMatch = route.match(/^\/articles\/([^/]+)$/)
    if (articleMatch) {
      return <ArticleDetailPage slug={articleMatch[1]} navigate={navigate} />
    }

    if (route.startsWith('/author/')) {
      return <AuthorPage username={route.replace('/author/', '')} navigate={navigate} />
    }

    if (route.startsWith('/category/')) {
      return <CategoryPage slug={route.replace('/category/', '')} navigate={navigate} />
    }

    return <HomePage navigate={navigate} />
  }

  return (
    <>
      <SiteHeader session={session} navigate={navigate} onLogout={handleLogout} />
      {renderPage()}
    </>
  )
}

export default App
