export function MarkdownPreview({ content }) {
  const lines = (content || '').split('\n')

  if (!content?.trim()) {
    return <p className="preview-placeholder">Preview will appear here as you write.</p>
  }

  return (
    <div className="markdown-preview">
      {lines.map((line, index) => renderLine(line, index))}
    </div>
  )
}

function renderLine(line, index) {
  const trimmed = line.trim()

  if (!trimmed) {
    return <br key={index} />
  }

  if (trimmed.startsWith('### ')) {
    return <h3 key={index}>{renderInlineMarkdown(trimmed.slice(4), index)}</h3>
  }

  if (trimmed.startsWith('## ')) {
    return <h2 key={index}>{renderInlineMarkdown(trimmed.slice(3), index)}</h2>
  }

  if (trimmed.startsWith('# ')) {
    return <h1 key={index}>{renderInlineMarkdown(trimmed.slice(2), index)}</h1>
  }

  if (trimmed.startsWith('- ')) {
    return <p className="preview-list-item" key={index}>{renderInlineMarkdown(trimmed.slice(2), index)}</p>
  }

  if (isImageOnlyLine(trimmed)) {
    const image = parseImage(trimmed)
    return (
      <figure className="markdown-image" key={index}>
        <img alt={image.alt} loading="lazy" src={image.src} />
      </figure>
    )
  }

  return <p key={index}>{renderInlineMarkdown(line, index)}</p>
}

function renderInlineMarkdown(text, lineIndex) {
  const tokens = []
  const pattern = /(!?)\[([^\]]+)]\(([^)\s]+)\)/g
  let cursor = 0
  let match

  while ((match = pattern.exec(text)) !== null) {
    const [raw, imageMarker, label, href] = match
    if (match.index > cursor) {
      tokens.push(text.slice(cursor, match.index))
    }

    if (imageMarker) {
      tokens.push(
        <img
          alt={label}
          className="markdown-inline-image"
          key={`${lineIndex}-image-${match.index}`}
          loading="lazy"
          src={href}
        />,
      )
    } else {
      tokens.push(
        <a href={href} key={`${lineIndex}-link-${match.index}`} rel="noreferrer" target="_blank">
          {label}
        </a>,
      )
    }

    cursor = match.index + raw.length
  }

  if (cursor < text.length) {
    tokens.push(text.slice(cursor))
  }

  return tokens.length ? tokens : text
}

function isImageOnlyLine(line) {
  return /^!\[[^\]]*]\([^)]+\)$/.test(line)
}

function parseImage(line) {
  const match = /^!\[([^\]]*)]\(([^)]+)\)$/.exec(line)
  return {
    alt: match?.[1]?.trim() || '',
    src: match?.[2] || '',
  }
}
