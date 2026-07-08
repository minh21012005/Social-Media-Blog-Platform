import { apiRequest } from './api'

function normalizeBookmarkCollection(payload) {
  if (Array.isArray(payload)) {
    return payload
  }

  if (!payload || typeof payload !== 'object') {
    return []
  }

  const nested = payload.data && typeof payload.data === 'object' ? payload.data : null
  const collection = [
    payload.items,
    payload.content,
    payload.results,
    payload.bookmarks,
    nested?.items,
    nested?.content,
    nested?.results,
    nested?.bookmarks,
  ].find((value) => Array.isArray(value))

  return collection || []
}

export function bookmarkArticle(articleId, token) {
  return apiRequest(`/api/v1/interactions/${articleId}/bookmark`, {
    method: 'POST',
    token,
  })
}

export function removeBookmark(articleId, token) {
  return apiRequest(`/api/v1/interactions/${articleId}/bookmark`, {
    method: 'DELETE',
    token,
  })
}

export function listMyBookmarks(token, { silent = false } = {}) {
  return apiRequest('/api/v1/interactions/bookmarks/me', {
    token,
    silent,
  }).then(normalizeBookmarkCollection)
}

export async function likeArticle(articleId, token) {
  return apiRequest(`/api/v1/interactions/${articleId}/like`, {
    method: 'POST',
    token,
  })
}

export async function unlikeArticle(articleId, token) {
  return apiRequest(`/api/v1/interactions/${articleId}/like`, {
    method: 'DELETE',
    token,
  })
}

export async function getArticleLikes(articleId, token, { silent = false } = {}) {
  return apiRequest(`/api/v1/interactions/${articleId}/likes`, {
    token,
    silent,
  })
}

export async function isArticleLiked(articleId, token, { silent = false } = {}) {
  return apiRequest(`/api/v1/interactions/${articleId}/liked`, {
    token,
    silent,
  })
}