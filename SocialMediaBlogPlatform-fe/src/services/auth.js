import { apiRequest } from './api'

const AUTH_STORAGE_KEY = 'social-blog-auth'
const refreshSkewMs = 30_000
let refreshInFlight = null

export function loadAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? normalizeAuth(JSON.parse(raw)) : null
  } catch {
    return null
  }
}

export function saveAuth(auth) {
  const normalized = normalizeAuth(auth)
  if (!normalized) {
    clearAuth()
    return null
  }
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({
    accessToken: normalized.accessToken,
    tokenType: normalized.tokenType,
    expiresInSeconds: normalized.expiresInSeconds,
    accessTokenExpiresAt: normalized.accessTokenExpiresAt,
    user: normalized.user,
  }))
  return normalized
}

export function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function refreshAuth() {
  if (!refreshInFlight) {
    refreshInFlight = apiRequest('/api/v1/auth/refresh', {
      method: 'POST',
      credentials: 'include',
    }).then((auth) => {
      const normalized = saveAuth(auth)
      if (!normalized) {
        throw new Error('Could not refresh session')
      }
      return normalized
    }).finally(() => {
      refreshInFlight = null
    })
  }
  return refreshInFlight
}

export function isAccessTokenExpiring(auth, skewMs = refreshSkewMs) {
  const expiresAt = getAccessTokenExpiresAt(auth)
  if (!expiresAt) {
    return true
  }
  return expiresAt <= Date.now() + skewMs
}

function normalizeAuth(auth) {
  if (!auth?.accessToken || !auth?.user) {
    return null
  }

  return {
    accessToken: auth.accessToken,
    tokenType: auth.tokenType || 'Bearer',
    expiresInSeconds: auth.expiresInSeconds,
    accessTokenExpiresAt: getAccessTokenExpiresAt(auth),
    user: auth.user,
  }
}

function getAccessTokenExpiresAt(auth) {
  if (!auth?.accessToken) {
    return null
  }
  const jwtExpiresAt = getJwtExpiresAt(auth.accessToken)
  if (jwtExpiresAt) {
    return jwtExpiresAt
  }
  if (Number.isFinite(auth.accessTokenExpiresAt)) {
    return auth.accessTokenExpiresAt
  }
  return null
}

function getJwtExpiresAt(token) {
  try {
    const [, payload] = token.split('.')
    if (!payload) {
      return null
    }
    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = JSON.parse(atob(normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=')))
    return Number.isFinite(json.exp) ? json.exp * 1000 : null
  } catch {
    return null
  }
}
