import { apiRequest } from './api'

export function normalizeProfile(profile) {
  if (!profile) {
    return null
  }

  return {
    id: profile.id,
    username: profile.username,
    email: profile.email,
    displayName: profile.displayName || profile.username,
    bio: profile.bio || '',
    avatarUrl: profile.avatarUrl || '',
    roles: profile.roles || [],
    isPrivate: profile.isPrivate || false,
    createdAt: profile.createdAt,
  }
}

export async function getCurrentProfile(token) {
  return normalizeProfile(await apiRequest('/api/v1/users/me', { token }))
}

export async function getPublicUser(userId) {
  return normalizeProfile(await apiRequest(`/api/v1/users/${userId}`))
}

export async function getPublicUserByUsername(username) {
  return normalizeProfile(await apiRequest(`/api/v1/users/by-username/${username}`))
}

export async function getPublicUsers(userIds) {
  const ids = [...new Set((userIds || []).filter(Boolean))]
  if (ids.length === 0) {
    return new Map()
  }

  const profiles = await apiRequest(`/api/v1/users/public?ids=${ids.join(',')}`)
  return new Map((profiles || []).map((profile) => [profile.id, normalizeProfile(profile)]))
}

export async function uploadAvatar(file, token) {
  const formData = new FormData()
  formData.append('file', file)
  return apiRequest('/api/v1/users/me/avatar', {
    method: 'POST',
    body: formData,
    token,
  })
}

export async function updateProfile(payload, token) {
  return normalizeProfile(await apiRequest('/api/v1/users/me', {
    method: 'PATCH',
    body: payload,
    token,
  }))
}

export async function searchUsers(query) {
  if (!query) {
    return []
  }
  const results = await apiRequest(`/api/v1/users/search?q=${encodeURIComponent(query)}`)
  return (results || []).map(normalizeProfile)
}
