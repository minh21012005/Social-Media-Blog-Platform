import { ArticleCard } from './ArticleCard'

export function ArticleList({ articles, emptyTitle = 'No stories yet.', emptyText, navigate, variant = 'grid', className = 'category-grid' }) {
  if (!articles?.length) {
    return (
      <div className="empty-state">
        <h2>{emptyTitle}</h2>
        {emptyText && <p>{emptyText}</p>}
      </div>
    )
  }

  return (
    <div className={className}>
      {articles.map((article) => (
        <ArticleCard article={article} key={article.id} navigate={navigate} variant={variant} />
      ))}
    </div>
  )
}
