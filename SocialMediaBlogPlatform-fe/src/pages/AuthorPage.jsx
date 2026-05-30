import { useEffect, useState } from 'react'
import { ArticleList } from '../components/ArticleList'
import { SiteFooter } from '../components/SiteFooter'
import { SocialIcon } from '../components/icons'
import { authors } from '../data/editorial'
import { listPublishedArticles } from '../services/articles'
import { getPublicUserByUsername } from '../services/users'

export function AuthorPage({ username, navigate }) {
  const [state, setState] = useState({ loading: true, author: null, articles: [], error: '' })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const author = await getPublicUserByUsername(username)
        const page = await listPublishedArticles({ authorId: author.id, size: 12 })
        if (active) {
          setState({ loading: false, author, articles: page.items, error: '' })
        }
      } catch (error) {
        if (active) {
          setState({ loading: false, author: null, articles: [], error: error.message })
        }
      }
    }
    load()
    return () => {
      active = false
    }
  }, [username])

  const author = state.author

  return (
    <main>
      <section className="author-hero">
        <div className="page-container author-inner">
          <img alt="" src={author?.avatarUrl || authors.sarah.avatar} />
          <div>
            <h1>{author?.displayName || (state.loading ? 'Loading author' : 'Author not found')}</h1>
            <p>{author?.bio || 'An editorial voice writing across design, culture, technology, and lifestyle.'}</p>
            <div className="author-socials">
              <SocialIcon label="Profile" />
              <span>{author?.username ? `@${author.username}` : username}</span>
              <SocialIcon label="Website" />
              <SocialIcon label="Email" />
            </div>
          </div>
        </div>
      </section>

      <section className="author-articles page-container">
        <h2>Articles by {author?.displayName || ''}</h2>
        {state.loading && <div className="loading-state">Loading author stories...</div>}
        {state.error && <div className="empty-state"><h2>Could not load author.</h2><p>{state.error}</p></div>}
        {!state.loading && !state.error && (
          <ArticleList articles={state.articles} emptyText="This author has not published an article yet." navigate={navigate} />
        )}
      </section>

      <SiteFooter />
    </main>
  )
}
