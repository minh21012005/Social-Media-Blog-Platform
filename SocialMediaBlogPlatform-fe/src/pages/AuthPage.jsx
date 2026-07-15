import { useEffect, useRef, useState } from 'react'
import { apiRequest } from '../services/api'

const GOOGLE_SCRIPT_ID = 'google-identity-services'
const GOOGLE_SCRIPT_URL = 'https://accounts.google.com/gsi/client'

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
  const [googleUnavailable, setGoogleUnavailable] = useState(false)
  const googleButtonRef = useRef(null)

  useEffect(() => {
    if (isRegister) {
      return undefined
    }

    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    if (!clientId) {
      setGoogleUnavailable(true)
      return undefined
    }

    let cancelled = false

    const initializeGoogleButton = () => {
      if (cancelled || !window.google?.accounts?.id || !googleButtonRef.current) {
        return
      }

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: async ({ credential }) => {
          if (!credential) {
            setError('Google did not return a sign-in credential.')
            return
          }

          setError('')
          setLoading(true)
          try {
            const data = await apiRequest('/api/v1/auth/google', {
              method: 'POST',
              credentials: 'include',
              body: { credential },
            })
            onDone(data)
          } catch (requestError) {
            setError(requestError.message || 'Google sign-in could not be completed.')
          } finally {
            setLoading(false)
          }
        },
      })

      googleButtonRef.current.replaceChildren()
      const width = Math.min(400, Math.max(240, Math.floor(googleButtonRef.current.clientWidth)))
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        type: 'standard',
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        shape: 'rectangular',
        logo_alignment: 'left',
        width,
      })
    }

    const existingScript = document.getElementById(GOOGLE_SCRIPT_ID)
    const handleScriptError = () => setGoogleUnavailable(true)

    if (window.google?.accounts?.id) {
      initializeGoogleButton()
    } else if (existingScript) {
      existingScript.addEventListener('load', initializeGoogleButton, { once: true })
      existingScript.addEventListener('error', handleScriptError, { once: true })
    } else {
      const script = document.createElement('script')
      script.id = GOOGLE_SCRIPT_ID
      script.src = GOOGLE_SCRIPT_URL
      script.async = true
      script.defer = true
      script.addEventListener('load', initializeGoogleButton, { once: true })
      script.addEventListener('error', handleScriptError, { once: true })
      document.head.appendChild(script)
    }

    return () => {
      cancelled = true
      existingScript?.removeEventListener('load', initializeGoogleButton)
      existingScript?.removeEventListener('error', handleScriptError)
    }
  }, [isRegister, onDone])

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
        {!isRegister && (
          <>
            <div
              aria-label="Continue with Google"
              className={`google-login-slot${loading ? ' is-loading' : ''}`}
              ref={googleButtonRef}
            >
              {googleUnavailable && <span>Google sign-in is not configured</span>}
            </div>
            <div className="auth-divider"><span>or continue with email</span></div>
          </>
        )}
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
