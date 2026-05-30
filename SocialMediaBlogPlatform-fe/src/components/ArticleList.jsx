import { ArticleCard } from './ArticleCard'

export function ArticleList({ articles, emptyTitle = 'No stories yet.', emptyText, navigate }) {
  if (!articles?.length) {
    return (
      <div className="empty-state">
        <h2>{emptyTitle}</h2>
        {emptyText && <p>{emptyText}</p>}
      </div>
    )
  }

  return (
    <div className="category-grid">
      {articles.map((article) => (
        <ArticleCard article={article} key={article.id} navigate={navigate} />
      ))}
    </div>
  )
}
