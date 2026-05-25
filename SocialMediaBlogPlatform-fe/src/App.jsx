import { useEffect, useMemo, useState } from 'react'
import './App.css'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const AUTH_STORAGE_KEY = 'social-blog-auth'

function loadAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function saveAuth(auth) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
}

function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

async function apiRequest(path, { method = 'GET', body, token } = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.success === false) {
    throw new Error(payload?.message ?? 'Request failed')
  }
  return payload?.data
}

function useRoute() {
  const [route, setRoute] = useState(window.location.pathname)

  useEffect(() => {
    const onPopState = () => setRoute(window.location.pathname)
    window.addEventListener('popstate', onPopState)
    return () => window.removeEventListener('popstate', onPopState)
  }, [])

  const navigate = (path) => {
    window.history.pushState({}, '', path)
    setRoute(path)
  }

  return [route, navigate]
}

function App() {
  const [route, navigate] = useRoute()
  const [auth, setAuth] = useState(loadAuth)

  const session = useMemo(() => {
    if (!auth?.accessToken || !auth?.user) {
      return null
    }
    return auth
  }, [auth])

  const handleAuthenticated = (authResponse) => {
    saveAuth(authResponse)
    setAuth(authResponse)
    navigate('/')
  }

  const handleLogout = () => {
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

  return <HomePage session={session} onLogout={handleLogout} navigate={navigate} />
}

function AppHeader({ session, navigate, onLogout }) {
  const open = (path) => (event) => {
    event.preventDefault()
    navigate(path)
  }

  return (
    <header className="app-header">
      <a href="/" className="brand" onClick={open('/')}>
        <span className="brand-mark">S</span>
        Social Blog
      </a>
      <nav className="top-nav" aria-label="Main navigation">
        <a href="/" onClick={open('/')}>Home</a>
        <a href="/register" onClick={open('/register')}>Start writing</a>
      </nav>
      <div className="header-actions">
        {session ? (
          <>
            <span className="mini-profile">{session.user.displayName}</span>
            <button className="ghost-button" type="button" onClick={onLogout}>Log out</button>
          </>
        ) : (
          <>
            <button className="ghost-button" type="button" onClick={() => navigate('/login')}>Log in</button>
            <button className="primary-button" type="button" onClick={() => navigate('/register')}>Join now</button>
          </>
        )}
      </div>
    </header>
  )
}

function HomePage({ session, onLogout, navigate }) {
  return (
    <main>
      <AppHeader session={session} onLogout={onLogout} navigate={navigate} />
      <section className="home-shell">
        <div className="feed-column">
          <div className="story-rail" aria-label="Featured writers">
            {['Lan', 'Minh', 'An', 'Vy', 'Khoa'].map((name, index) => (
              <div className="story" key={name}>
                <span className={`story-ring story-${index}`}>{name.slice(0, 1)}</span>
                <small>{name}</small>
              </div>
            ))}
          </div>

          <article className="feature-post">
            <div className="post-author">
              <span className="avatar">M</span>
              <div>
                <strong>Mai Writer</strong>
                <span>Published in Product Notes</span>
              </div>
            </div>
            <div className="post-visual">
              <div className="post-visual-content">
                <span>New essay</span>
                <h1>Write, publish, and meet readers in one calm social space.</h1>
              </div>
            </div>
            <div className="post-actions">
              <span>12.4k claps</span>
              <span>384 comments</span>
              <span>2 min read</span>
            </div>
            <p>
              A clean base for creators and readers: accounts today, articles,
              comments, interactions, and follows ready to grow from this foundation.
            </p>
          </article>

          <section className="article-grid" aria-label="Article previews">
            {articlePreviews.map((article) => (
              <article className="article-card" key={article.title}>
                <span className="category">{article.category}</span>
                <h2>{article.title}</h2>
                <p>{article.summary}</p>
              </article>
            ))}
          </section>
        </div>

        <aside className="side-panel">
          {session ? (
            <div className="profile-panel">
              <span className="avatar large">{session.user.displayName.slice(0, 1)}</span>
              <h2>Welcome back, {session.user.displayName}</h2>
              <p>Your account is active. The next milestone is publishing your first article.</p>
              <button className="primary-button wide" type="button">Draft article</button>
            </div>
          ) : (
            <div className="profile-panel">
              <h2>Join the first circle of writers.</h2>
              <p>Create an account to save your profile and unlock protected platform routes.</p>
              <button className="primary-button wide" type="button" onClick={() => navigate('/register')}>
                Create account
              </button>
              <button className="ghost-button wide" type="button" onClick={() => navigate('/login')}>
                I already have one
              </button>
            </div>
          )}
          <div className="trend-list">
            <h3>Trending topics</h3>
            <a href="/">Writing Systems</a>
            <a href="/">Creator Economy</a>
            <a href="/">Microservices</a>
          </div>
        </aside>
      </section>
    </main>
  )
}

function AuthPage({ mode, onDone, navigate }) {
  const isRegister = mode === 'register'
  const [form, setForm] = useState({
    username: '',
    displayName: '',
    email: '',
    identifier: '',
    password: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }))
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const data = isRegister
        ? await apiRequest('/api/v1/auth/register', {
            method: 'POST',
            body: {
              username: form.username,
              email: form.email,
              password: form.password,
              displayName: form.displayName || form.username,
            },
          })
        : await apiRequest('/api/v1/auth/login', {
            method: 'POST',
            body: {
              identifier: form.identifier,
              password: form.password,
            },
          })
      onDone(data)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-preview">
        <a className="brand" href="/" onClick={(event) => {
          event.preventDefault()
          navigate('/')
        }}>
          <span className="brand-mark">S</span>
          Social Blog
        </a>
        <div className="phone-frame">
          <div className="mini-feed-card top-card">
            <span className="avatar">A</span>
            <div>
              <strong>Architecture notes</strong>
              <p>Gateway, discovery, JWT, and clean services.</p>
            </div>
          </div>
          <div className="mini-feed-card bottom-card">
            <span className="avatar">R</span>
            <div>
              <strong>Reader profile</strong>
              <p>Follow writers and collect the essays that matter.</p>
            </div>
          </div>
        </div>
      </section>

      <section className="auth-card" aria-labelledby="auth-title">
        <span className="eyebrow">{isRegister ? 'Create account' : 'Welcome back'}</span>
        <h1 id="auth-title">{isRegister ? 'Start your writing profile.' : 'Log in to your profile.'}</h1>
        <form onSubmit={submit}>
          {isRegister && (
            <>
              <label>
                Username
                <input
                  autoComplete="username"
                  onChange={update('username')}
                  pattern="[A-Za-z0-9._]{3,30}"
                  required
                  type="text"
                  value={form.username}
                />
              </label>
              <label>
                Display name
                <input
                  autoComplete="name"
                  maxLength="80"
                  onChange={update('displayName')}
                  type="text"
                  value={form.displayName}
                />
              </label>
              <label>
                Email
                <input
                  autoComplete="email"
                  onChange={update('email')}
                  required
                  type="email"
                  value={form.email}
                />
              </label>
            </>
          )}
          {!isRegister && (
            <label>
              Email or username
              <input
                autoComplete="username"
                onChange={update('identifier')}
                required
                type="text"
                value={form.identifier}
              />
            </label>
          )}
          <label>
            Password
            <input
              autoComplete={isRegister ? 'new-password' : 'current-password'}
              minLength="8"
              maxLength="72"
              onChange={update('password')}
              required
              type="password"
              value={form.password}
            />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button className="primary-button wide" disabled={loading} type="submit">
            {loading ? 'Please wait...' : isRegister ? 'Create account' : 'Log in'}
          </button>
        </form>
        <p className="auth-switch">
          {isRegister ? 'Already joined?' : 'New here?'}
          <button type="button" onClick={() => navigate(isRegister ? '/login' : '/register')}>
            {isRegister ? 'Log in' : 'Create an account'}
          </button>
        </p>
      </section>
    </main>
  )
}

const articlePreviews = [
  {
    category: 'Design',
    title: 'A calm social feed for long-form ideas',
    summary: 'A homepage that feels social without making writing feel noisy.',
  },
  {
    category: 'Engineering',
    title: 'Auth as the first vertical slice',
    summary: 'Register, login, JWT, gateway routing, and profile retrieval are wired end to end.',
  },
  {
    category: 'Community',
    title: 'Profiles before publishing',
    summary: 'The account base is ready for author pages, follows, comments, and claps.',
  },
]

export default App
