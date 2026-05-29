import { articles, categories } from '../data/editorial'
import { ArrowRightIcon } from '../components/icons'
import { ArticleCard } from '../components/ArticleCard'
import { Newsletter } from '../components/Newsletter'
import { SiteFooter } from '../components/SiteFooter'

export function HomePage({ navigate }) {
  const [featured, culturePick, technologyPick, coffeeStory, typographyStory] = articles

  const open = (path) => (event) => {
    event.preventDefault()
    navigate(path)
  }

  return (
    <main>
      <section className="hero-section page-container">
        <img alt="" className="hero-image" src={featured.image} />
        <article className="hero-copy">
          <div className="eyebrow-row">
            <span>{featured.category}</span>
            <span aria-hidden="true">•</span>
            <span>{featured.date}</span>
          </div>
          <h1>{featured.title}</h1>
          <p>{featured.summary}</p>
          <div className="author-line">
            <img alt="" src={featured.author.avatar} />
            <div>
              <strong>{featured.author.name}</strong>
              <span>{featured.readTime}</span>
            </div>
          </div>
        </article>
      </section>

      <section className="section-band">
        <div className="page-container">
          <div className="section-heading">
            <h2>Editor&apos;s Picks</h2>
            <a href="/category/design" onClick={open('/category/design')}>
              View all
              <ArrowRightIcon />
            </a>
          </div>
          <div className="editor-grid">
            <ArticleCard article={culturePick} />
            <ArticleCard article={technologyPick} />
          </div>
        </div>
      </section>

      <section className="latest-section page-container">
        <h2>Latest Stories</h2>
        <div className="story-list">
          <ArticleCard article={coffeeStory} variant="horizontal" />
          <ArticleCard article={typographyStory} variant="horizontal" />
        </div>
        <button className="outline-pill" type="button">Load More Stories</button>
      </section>

      <Newsletter />

      <section className="topics-section page-container">
        <h2>Popular Topics</h2>
        <div className="topic-list">
          {categories.map((category) => (
            <a href={`/category/${category.slug}`} key={category.slug} onClick={open(`/category/${category.slug}`)}>
              {category.label}
            </a>
          ))}
        </div>
      </section>

      <SiteFooter />
    </main>
  )
}
