import { useEffect, useMemo, useState } from 'react'
import { clearAuth, loadAuth, refreshAuth, saveAuth } from './services/auth'
import { apiRequest } from './services/api'
import { useRoute } from './hooks/useRoute'
import { SiteHeader } from './components/SiteHeader'
import { HomePage } from './pages/HomePage'
import { CategoryPage } from './pages/CategoryPage'
import { AuthorPage } from './pages/AuthorPage'
import { AuthPage } from './pages/AuthPage'
import './App.css'

function App() {
  const [route, navigate] = useRoute()
  const [auth, setAuth] = useState(loadAuth)

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

  if (route === '/login') {
    return <AuthPage mode="login" onDone={handleAuthenticated} navigate={navigate} />
  }

  if (route === '/register') {
    return <AuthPage mode="register" onDone={handleAuthenticated} navigate={navigate} />
  }

  const renderPage = () => {
    if (route === '/author/sarah-jenkins') {
      return <AuthorPage />
    }

    if (route.startsWith('/category/')) {
      return <CategoryPage slug={route.replace('/category/', '')} />
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
