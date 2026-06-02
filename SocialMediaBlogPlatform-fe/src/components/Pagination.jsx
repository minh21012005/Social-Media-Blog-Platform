export function Pagination() {
  return (
    <nav aria-label="Pagination" className="pagination">
      <button aria-label="Previous page" disabled type="button">‹</button>
      <a aria-current="page" href="/">1</a>
      <a href="/">2</a>
      <a href="/">3</a>
      <span>...</span>
      <button aria-label="Next page" type="button">›</button>
    </nav>
  )
}
