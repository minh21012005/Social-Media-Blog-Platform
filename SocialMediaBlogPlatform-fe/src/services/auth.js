import { apiRequest } from './api'

const AUTH_STORAGE_KEY = 'social-blog-auth'

export function loadAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function saveAuth(auth) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({
    accessToken: auth.accessToken,
    tokenType: auth.tokenType,
    expiresInSeconds: auth.expiresInSeconds,
    user: auth.user,
  }))
}

export function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function refreshAuth() {
  return apiRequest('/api/v1/auth/refresh', {
    method: 'POST',
    credentials: 'include',
  })
}
