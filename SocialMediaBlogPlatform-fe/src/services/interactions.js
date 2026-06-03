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
