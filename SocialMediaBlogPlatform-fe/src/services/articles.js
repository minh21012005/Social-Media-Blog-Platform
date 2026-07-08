import { apiRequest } from './api'
import { articles as fallbackArticles, authors, categories } from '../data/editorial'
import { getPublicUsers } from './users'

const categoryMap = new Map(categories.map((category) => [category.slug, category.label]))
const fallbackImages = new Map(fallbackArticles.map((article) => [article.categorySlug, article.image]))

export function categoryLabel(slug) {
  return categoryMap.get(slug) ?? slug
}

export function formatDate(value) {
  if (!value) {
    return 'Unpublished'
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value))
}

export function estimateReadTime(content) {
  const words = (content || '').trim().split(/\s+/).filter(Boolean).length
  return `${Math.max(1, Math.ceil(words / 220))} min read`
}

export function formatCount(value) {
  const count = Number(value || 0)
  if (count >= 1000) {
    return `${(count / 1000).toFixed(count >= 10000 ? 0 : 1)}k`
  }
  return String(count)
}

export function articlePath(article) {
  return article?.slug ? `/articles/${article.slug}` : '/'
}

export function normalizeArticle(article, author) {
  const displayAuthor = author || {
    id: article.authorId,
    username: 'unknown',
    displayName: 'Chronicle Writer',
    avatarUrl: authors.sarah.avatar,
    bio: '',
  }

  return {
    ...article,
    image: article.coverImageUrl || fallbackImages.get(article.category) || fallbackArticles[0].image,
    category: categoryLabel(article.category),
    categorySlug: article.category,
    date: formatDate(article.publishedAt || article.createdAt),
    readTime: estimateReadTime(article.content),
    path: articlePath(article),
    author: {
      id: displayAuthor.id,
      name: displayAuthor.displayName || displayAuthor.username,
      username: displayAuthor.username,
      slug: displayAuthor.username,
      avatar: displayAuthor.avatarUrl || authors.sarah.avatar,
      bio: displayAuthor.bio || '',
    },
  }
}

export async function enrichArticles(articles) {
  const authorMap = await getPublicUsers(articles.map((article) => article.authorId)).catch(() => new Map())
  return articles.map((article) => normalizeArticle(article, authorMap.get(article.authorId)))
}

export async function listPublishedArticles(params = {}) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value)
    }
  })

  const page = await apiRequest(`/api/v1/articles?${searchParams.toString()}`)
  return {
    ...page,
    items: await enrichArticles(page?.items || []),
  }
}

export async function listFeaturedArticles({ size = 1 } = {}) {
  return enrichArticles(await apiRequest(`/api/v1/articles/featured?size=${size}`))
}

export async function listEditorPicks({ size = 2 } = {}) {
  return enrichArticles(await apiRequest(`/api/v1/articles/editor-picks?size=${size}`))
}

export async function getArticleBySlug(slug) {
  const article = await apiRequest(`/api/v1/articles/slug/${slug}`)
  const [enriched] = await enrichArticles([article])
  return enriched
}

export async function getArticleById(id) {
  const article = await apiRequest(`/api/v1/articles/id/${id}`)
  const [enriched] = await enrichArticles([article])
  return enriched
}

export async function listMyArticles({ status, page = 0, size = 20 } = {}, token) {
  const searchParams = new URLSearchParams({ page, size })
  if (status) {
    searchParams.set('status', status)
  }
  const result = await apiRequest(`/api/v1/articles/me?${searchParams.toString()}`, { token })
  return {
    ...result,
    items: (result?.items || []).map((article) => normalizeArticle(article, null)),
  }
}

export function createArticle(payload, token) {
  return apiRequest('/api/v1/articles', {
    method: 'POST',
    body: payload,
    token,
  })
}

export function updateArticle(articleId, payload, token) {
  return apiRequest(`/api/v1/articles/${articleId}`, {
    method: 'PUT',
    body: payload,
    token,
  })
}

export function publishArticle(articleId, token) {
  return apiRequest(`/api/v1/articles/${articleId}/publish`, {
    method: 'POST',
    token,
  })
}

export function archiveArticle(articleId, token) {
  return apiRequest(`/api/v1/articles/${articleId}/archive`, {
    method: 'POST',
    token,
  })
}

export function deleteArticle(articleId, token) {
  return apiRequest(`/api/v1/articles/${articleId}`, {
    method: 'DELETE',
    token,
  })
}

export function recordArticleView(articleId, payload = {}) {
  return apiRequest(`/api/v1/articles/${articleId}/views`, {
    method: 'POST',
    body: payload,
  })
}

export function uploadArticleMedia(file, token) {
  const formData = new FormData()
  formData.append('file', file)
  return apiRequest('/api/v1/articles/media', {
    method: 'POST',
    body: formData,
    token,
  })
}

export function curateArticle(articleId, payload, token) {
  return apiRequest(`/api/v1/articles/${articleId}/curation`, {
    method: 'PATCH',
    body: payload,
    token,
  })
}

export async function listTrendingArticles({ size = 6 } = {}) {
  return enrichArticles(await apiRequest(`/api/v1/articles/trending?size=${size}`))
}
