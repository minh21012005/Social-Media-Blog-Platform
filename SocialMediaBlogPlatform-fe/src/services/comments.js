import { apiRequest } from './api'

export function listArticleComments(articleId, token, page = 0, size = 10, sort = 'NEWEST') {
  const query = new URLSearchParams({ page, size, sort }).toString()
  return apiRequest(`/api/v1/articles/${articleId}/comments?${query}`, { token })
}

export function listCommentReplies(commentId, token, page = 0, size = 10) {
  const query = new URLSearchParams({ page, size }).toString()
  return apiRequest(`/api/v1/comments/${commentId}/replies?${query}`, { token })
}

export function createCommentReply(commentId, payload, token) {
  return apiRequest(`/api/v1/comments/${commentId}/replies`, {
    method: 'POST',
    body: payload,
    token,
  })
}

export function createComment(articleId, payload, token) {
  return apiRequest(`/api/v1/articles/${articleId}/comments`, {
    method: 'POST',
    body: payload,
    token,
  })
}

export function editComment(commentId, payload, token) {
  return apiRequest(`/api/v1/comments/${commentId}`, {
    method: 'PATCH',
    body: payload,
    token,
  })
}

export function deleteComment(commentId, token) {
  return apiRequest(`/api/v1/comments/${commentId}`, {
    method: 'DELETE',
    token,
  })
}

export function getArticleCommentCount(articleId) {
  return apiRequest(`/api/v1/articles/${articleId}/comments/count`)
}

export function pinComment(commentId, token) {
  return apiRequest(`/api/v1/comments/${commentId}/pin`, {
    method: 'POST',
    token,
  })
}

export function unpinComment(commentId, token) {
  return apiRequest(`/api/v1/comments/${commentId}/unpin`, {
    method: 'POST',
    token,
  })
}

export function clapComment(commentId, token) {
  return apiRequest(`/api/v1/comments/${commentId}/clap`, {
    method: 'POST',
    token,
  })
}

export function undoClapComment(commentId, token) {
  return apiRequest(`/api/v1/comments/${commentId}/clap`, {
    method: 'DELETE',
    token,
  })
}
