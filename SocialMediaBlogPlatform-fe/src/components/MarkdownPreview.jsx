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
  if (!line.trim()) {
    return <br key={index} />
  }

  if (line.startsWith('### ')) {
    return <h3 key={index}>{line.slice(4)}</h3>
  }

  if (line.startsWith('## ')) {
    return <h2 key={index}>{line.slice(3)}</h2>
  }

  if (line.startsWith('# ')) {
    return <h1 key={index}>{line.slice(2)}</h1>
  }

  if (line.startsWith('- ')) {
    return <p className="preview-list-item" key={index}>{line.slice(2)}</p>
  }

  return <p key={index}>{line}</p>
}
