import { apiRequest } from './api'

export function listArticleComments(articleId) {
  return apiRequest(`/api/v1/articles/${articleId}/comments`)
}

export function listCommentReplies(commentId) {
  return apiRequest(`/api/v1/comments/${commentId}/replies`)
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
