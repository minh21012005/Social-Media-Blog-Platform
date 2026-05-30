export function Newsletter() {
  return (
    <section className="newsletter-section" aria-labelledby="newsletter-title">
      <h2 id="newsletter-title">The Weekly Brief</h2>
      <p>Get our best stories delivered to your inbox every Sunday morning.</p>
      <form>
        <input aria-label="Email address" placeholder="Your email address" type="email" />
        <button type="submit">Subscribe</button>
      </form>
      <small>No spam. Unsubscribe anytime.</small>
    </section>
  )
}
