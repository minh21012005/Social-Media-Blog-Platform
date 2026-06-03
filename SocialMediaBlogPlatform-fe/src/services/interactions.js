import { apiRequest } from './api'

export function bookmarkArticle(articleId, token) {
  return apiRequest('/api/v1/interactions/bookmarks', {
    method: 'POST',
    body: { articleId },
    token,
  })
}

export function removeBookmark(articleId, token) {
  return apiRequest(`/api/v1/interactions/bookmarks/${articleId}`, {
    method: 'DELETE',
    token,
  })
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

export async function getArticleLikes(articleId) {
  return apiRequest(`/api/v1/interactions/${articleId}/likes`)
}

export async function isArticleLiked(articleId, token) {
  return apiRequest(`/api/v1/interactions/${articleId}/liked`, {
    token,
  })
}