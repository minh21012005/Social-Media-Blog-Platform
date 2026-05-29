import { categories } from '../data/editorial'

export function SiteFooter() {
  return (
    <footer className="site-footer">
      <div className="footer-grid">
        <section className="footer-brand">
          <h2>Chronicle</h2>
          <p>An editorial platform exploring the intersection of design, culture, and technology in the modern world.</p>
          <form className="footer-subscribe">
            <input aria-label="Email address" placeholder="Your email address" type="email" />
            <button type="submit">Subscribe</button>
          </form>
        </section>
        <section>
          <h3>Categories</h3>
          {categories.map((category) => (
            <a href={`/category/${category.slug}`} key={category.slug}>{category.label}</a>
          ))}
        </section>
        <section>
          <h3>Company</h3>
          <a href="/">About Us</a>
          <a href="/">Contact</a>
          <a href="/">Careers</a>
          <a href="/">Privacy Policy</a>
        </section>
      </div>
      <div className="footer-bottom">
        <span>© 2026 Chronicle Media. All rights reserved.</span>
        <div>
          <a href="/">Twitter</a>
          <a href="/">Instagram</a>
          <a href="/">LinkedIn</a>
        </div>
      </div>
    </footer>
  )
}
