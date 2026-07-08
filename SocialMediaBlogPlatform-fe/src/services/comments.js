import { apiRequest } from './api'

function normalizeCommentCollection(payload) {
  if (Array.isArray(payload)) {
    return payload
  }

  if (!payload || typeof payload !== 'object') {
    return []
  }

  const nested = payload.data && typeof payload.data === 'object' ? payload.data : null
  const candidates = [
    payload.items,
    payload.content,
    payload.results,
    payload.comments,
    nested?.items,
    nested?.content,
    nested?.results,
    nested?.comments,
  ]

  const collection = candidates.find((value) => Array.isArray(value))
  return collection || []
}

async function withTransientRetry(requestFn, maxRetries = 1) {
  let attempt = 0
  while (true) {
    try {
      return await requestFn()
    } catch (error) {
      const retriableStatus = error?.status === 502 || error?.status === 503 || error?.status === 504
      if (!retriableStatus || attempt >= maxRetries) {
        throw error
      }
      attempt += 1
      await new Promise((resolve) => window.setTimeout(resolve, 300 * attempt))
    }
  }
}

export function listArticleComments(articleId, token, page = 0, size = 10, sort = 'NEWEST') {
  const query = new URLSearchParams({ page, size, sort }).toString()
  return withTransientRetry(() => apiRequest(`/api/v1/articles/${articleId}/comments?${query}`, { token }))
    .then(normalizeCommentCollection)
}

export function listCommentReplies(commentId, token, page = 0, size = 10) {
  const query = new URLSearchParams({ page, size }).toString()
  return withTransientRetry(() => apiRequest(`/api/v1/comments/${commentId}/replies?${query}`, { token }))
    .then(normalizeCommentCollection)
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
