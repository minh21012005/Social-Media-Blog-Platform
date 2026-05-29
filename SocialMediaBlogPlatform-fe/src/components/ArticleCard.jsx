export function ArticleCard({ article, variant = 'grid' }) {
  return (
    <article className={`article-card article-card-${variant}`}>
      <img alt="" className="article-image" src={article.image} />
      <div className="article-body">
        <span className="article-category">{article.category}</span>
        <h3>{article.title}</h3>
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
      <span aria-hidden="true">•</span>
      <span>{article.date}</span>
      {withReadTime && (
        <>
          <span aria-hidden="true">•</span>
          <span>{article.readTime}</span>
        </>
      )}
    </div>
  )
}
