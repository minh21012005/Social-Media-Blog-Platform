import { useState } from 'react'
import { apiRequest } from '../services/api'

export function AuthPage({ mode, onDone, navigate }) {
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
            credentials: 'include',
            body: {
              username: form.username,
              email: form.email,
              password: form.password,
              displayName: form.displayName || form.username,
            },
          })
        : await apiRequest('/api/v1/auth/login', {
            method: 'POST',
            credentials: 'include',
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
    <main className="auth-page">
      <section className="auth-visual">
        <a className="brand-wordmark" href="/" onClick={(event) => {
          event.preventDefault()
          navigate('/')
        }}>
          Chronicle
        </a>
        <div className="auth-editorial-card">
          <span>Member Brief</span>
          <h1>Write with taste. Publish with calm.</h1>
          <p>Join a slower social platform for essays, ideas, and editorial conversations.</p>
        </div>
      </section>

      <section className="auth-panel" aria-labelledby="auth-title">
        <span className="form-eyebrow">{isRegister ? 'Create account' : 'Welcome back'}</span>
        <h1 id="auth-title">{isRegister ? 'Start your Chronicle profile.' : 'Log in to Chronicle.'}</h1>
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
              maxLength="72"
              minLength="8"
              onChange={update('password')}
              required
              type="password"
              value={form.password}
            />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button className="submit-button" disabled={loading} type="submit">
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
