import { articles, authors } from '../data/editorial'
import { ArticleCard } from '../components/ArticleCard'
import { SiteFooter } from '../components/SiteFooter'
import { SocialIcon } from '../components/icons'

export function AuthorPage() {
  const author = authors.sarah
  const authorArticles = articles.filter((article) => article.author.slug === author.slug)

  return (
    <main>
      <section className="author-hero">
        <div className="page-container author-inner">
          <img alt={author.name} src={author.avatar} />
          <div>
            <h1>{author.name}</h1>
            <p>{author.title}</p>
            <div className="author-socials">
              <SocialIcon label="Twitter" />
              <span>{author.handle}</span>
              <SocialIcon label="Website" />
              <SocialIcon label="Email" />
            </div>
          </div>
        </div>
      </section>

      <section className="author-articles page-container">
        <h2>Articles by</h2>
        <div className="category-grid">
          {authorArticles.map((article) => (
            <ArticleCard article={article} key={article.id} />
          ))}
        </div>
      </section>

      <SiteFooter />
    </main>
  )
}
