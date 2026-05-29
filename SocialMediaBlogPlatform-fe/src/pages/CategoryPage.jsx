import { articles, categories } from '../data/editorial'
import { ArticleCard } from '../components/ArticleCard'
import { Pagination } from '../components/Pagination'
import { SiteFooter } from '../components/SiteFooter'

export function CategoryPage({ slug }) {
  const category = categories.find((item) => item.slug === slug) ?? categories[0]
  const filtered = articles.filter((article) => article.categorySlug === category.slug)
  const repeated = filtered.length > 1 ? [...filtered, ...filtered] : [...filtered, ...filtered]

  return (
    <main>
      <section className="category-hero">
        <h1>{category.label}</h1>
        <p>Exploring the latest trends, deep dives, and expert perspectives in {category.label.toLowerCase()}.</p>
      </section>

      <section className="category-grid page-container">
        {repeated.map((article, index) => (
          <ArticleCard article={article} key={`${article.id}-${index}`} />
        ))}
      </section>

      <Pagination />
      <SiteFooter />
    </main>
  )
}
