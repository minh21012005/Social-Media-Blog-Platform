import { apiRequest } from './api'

export function followUser(userId, token) {
  return apiRequest(`/api/v1/follows/${userId}`, {
    method: 'POST',
    token,
  })
}

export function unfollowUser(userId, token) {
  return apiRequest(`/api/v1/follows/${userId}`, {
    method: 'DELETE',
    token,
  })
}

export function getFollowStatus(userId, token) {
  return apiRequest(`/api/v1/follows/${userId}/status`, { token })
}

export function getFollowCounts(userId, token) {
  return apiRequest(`/api/v1/follows/${userId}/counts`, { token })
}

export function listFollowers(userId, { page = 0, size = 20 } = {}, token) {
  return apiRequest(`/api/v1/follows/${userId}/followers?page=${page}&size=${size}`, { token })
}

export function listFollowing(userId, { page = 0, size = 20 } = {}, token) {
  return apiRequest(`/api/v1/follows/${userId}/following?page=${page}&size=${size}`, { token })
}
