export function Pagination({ page = 0, totalPages = 0, onPageChange }) {
  if (totalPages <= 1) {
    return null
  }

  const pages = visiblePages(page, totalPages)

  return (
    <nav aria-label="Pagination" className="pagination">
      <button
        aria-label="Previous page"
        disabled={page <= 0}
        type="button"
        onClick={() => onPageChange(page - 1)}
      >
        Prev
      </button>
      {pages.map((item, index) => item === 'gap' ? (
        <span aria-hidden="true" key={`gap-${index}`}>...</span>
      ) : (
        <button
          aria-current={item === page ? 'page' : undefined}
          key={item}
          type="button"
          onClick={() => onPageChange(item)}
        >
          {item + 1}
        </button>
      ))}
      <button
        aria-label="Next page"
        disabled={page >= totalPages - 1}
        type="button"
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </nav>
  )
}

function visiblePages(currentPage, totalPages) {
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, index) => index)
  }

  const pages = new Set([0, totalPages - 1, currentPage])
  if (currentPage > 0) {
    pages.add(currentPage - 1)
  }
  if (currentPage < totalPages - 1) {
    pages.add(currentPage + 1)
  }

  const sorted = [...pages].sort((left, right) => left - right)
  return sorted.flatMap((item, index) => {
    const previous = sorted[index - 1]
    if (index > 0 && item - previous > 1) {
      return ['gap', item]
    }
    return [item]
  })
}
