import { apiRequest } from './api'

export function listArticleComments(articleId, token) {
  return apiRequest(`/api/v1/articles/${articleId}/comments`, { token })
}

export function listCommentReplies(commentId, token) {
  return apiRequest(`/api/v1/comments/${commentId}/replies`, { token })
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
