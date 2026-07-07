import { useState, useEffect } from 'react'

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
        <ArticleMeta article={article} navigate={navigate} />
      </div>
    </article>
  )
}

export function ArticleMeta({ article, withReadTime = false, navigate }) {
  return (
    <div className="article-meta">
      <strong 
        style={{ cursor: 'pointer' }}
        onClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
          if (navigate) navigate(`/author/${article.author.username}`)
        }}
      >
        {article.author.name}
      </strong>
      <span aria-hidden="true">&middot;</span>
      <span>{article.date}</span>
      {withReadTime && (
        <>
          <span aria-hidden="true">&middot;</span>
          <span>{article.readTime}</span>
        </>
      )}
    </div>
  )
}
