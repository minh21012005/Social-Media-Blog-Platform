import { useState, useEffect } from 'react'
import { getArticleCommentCount } from '../services/comments'

export function ArticleCard({ article, variant = 'grid', navigate }) {
  const open = (event) => {
    if (!navigate || !article.path) {
      return
    }
    event.preventDefault()
    navigate(article.path)
  }

  return (
    <article className={`article-card article-card-${variant}`}>
      <a href={article.path || '#'} onClick={open}>
        <img alt="" className="article-image" src={article.image} />
      </a>
      <div className="article-body">
        <span className="article-category">{article.category}</span>
        <h3>
          <a href={article.path || '#'} onClick={open}>{article.title}</a>
        </h3>
        <p>{article.summary}</p>
        <ArticleMeta article={article} />
      </div>
    </article>
  )
}

export function ArticleMeta({ article, withReadTime = false }) {
  return (
    <div className="article-meta">
      <strong>{article.author.name}</strong>
      <span aria-hidden="true">&middot;</span>
      <span>{article.date}</span>
      {withReadTime && (
        <>
          <span aria-hidden="true">&middot;</span>
          <span>{article.readTime}</span>
        </>
      )}
      <span aria-hidden="true">&middot;</span>
      <CommentCountBadge articleId={article.id} />
    </div>
  )
}

function CommentCountBadge({ articleId }) {
  const [count, setCount] = useState(null)

  useEffect(() => {
    let active = true
    getArticleCommentCount(articleId)
      .then(res => {
        if (active && res && typeof res.commentCount === 'number') {
          setCount(res.commentCount)
        }
      })
      .catch(() => {})
    
    return () => {
      active = false
    }
  }, [articleId])

  if (count === null) {
    return null
  }

  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
      💬 {count} {count === 1 ? 'comment' : 'comments'}
    </span>
  )
}
