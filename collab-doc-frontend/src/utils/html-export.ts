import type { Node, Mark } from 'prosemirror-model'

function textToHtml(text: string, marks: readonly Mark[]): string {
  const styles: string[] = []

  for (const mark of marks) {
    if (mark.type.name === 'textStyle') {
      if (mark.attrs?.color) styles.push(`color: ${mark.attrs.color}`)
      if (mark.attrs?.fontSize) styles.push(`font-size: ${mark.attrs.fontSize}`)
      if (mark.attrs?.fontFamily) styles.push(`font-family: ${mark.attrs.fontFamily}`)
    }
  }

  for (const mark of marks) {
    if (mark.type.name === 'bold') text = `<strong>${text}</strong>`
    if (mark.type.name === 'italic') text = `<em>${text}</em>`
    if (mark.type.name === 'code') text = `<code>${text}</code>`
    if (mark.type.name === 'strike') text = `<s>${text}</s>`
    if (mark.type.name === 'highlight') text = `<mark>${text}</mark>`
    if (mark.type.name === 'link') text = `<a href="${mark.attrs?.href || ''}">${text}</a>`
  }

  if (styles.length > 0) {
    text = `<span style="${styles.join('; ')}">${text}</span>`
  }

  return text
}

function nodeToHtml(node: Node): string {
  if (node.isText) {
    return textToHtml(node.text || '', node.marks || [])
  }

  // Handle inline image nodes
  if (node.type.name === 'image') {
    const src = node.attrs.src || ''
    const alt = node.attrs.alt || 'image'
    return `<img src="${src}" alt="${alt}" />`
  }

  if (!node.isBlock) {
    return node.children.map((c: Node) => nodeToHtml(c)).join('')
  }

  const children = node.children.map((c: Node) => nodeToHtml(c)).join('')

  switch (node.type.name) {
    case 'heading': {
      const level = node.attrs.level || 1
      return `<h${level}>${children}</h${level}>`
    }
    case 'paragraph':
      return `<p>${children}</p>`
    case 'bulletList':
      return `<ul>${children}</ul>`
    case 'orderedList':
      return `<ol>${children}</ol>`
    case 'listItem':
      return `<li>${children}</li>`
    case 'taskList':
      return node.children.map((child: Node) => {
        const checked = child.attrs.checked ? 'checked' : ''
        const content = child.children.map((c: Node) => {
          if (c.type.name === 'taskItem') {
            return c.children.map((cc: Node) => nodeToHtml(cc)).join('')
          }
          return nodeToHtml(c)
        }).join('')
        return `<div><input type="checkbox" ${checked} disabled />${content}</div>`
      }).join('')
    case 'codeBlock': {
      const lang = node.attrs.language || ''
      return `<pre><code class="lang-${lang}">${node.textContent}</code></pre>`
    }
    case 'blockquote':
      return `<blockquote>${children}</blockquote>`
    case 'horizontalRule':
      return '<hr />'
    case 'table': {
      const rows = node.children.map((row: Node, rowIdx: number) => {
        const cells = row.children.map((cell: Node) => {
          const cellContent = cell.children.map((c: Node) => nodeToHtml(c)).join('')
          const tag = rowIdx === 0 ? 'th' : 'td'
          return `<${tag}>${cellContent}</${tag}>`
        }).join('')
        return `<tr>${cells}</tr>`
      }).join('')
      return `<table>${rows}</table>`
    }
    default:
      return children
  }
}

export function prosemirrorToHtml(doc: Node, title: string): string {
  const bodyHtml = nodeToHtml(doc)

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${title}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; color: #2c2825; background: #faf8f5; line-height: 1.7; max-width: 800px; margin: 0 auto; padding: 48px 32px; }
    h1 { font-size: 32px; font-weight: 700; margin: 24px 0 12px; }
    h2 { font-size: 24px; font-weight: 600; margin: 20px 0 10px; }
    h3 { font-size: 19px; font-weight: 600; margin: 16px 0 8px; }
    p { margin: 8px 0; }
    img { max-width: 100%; border-radius: 6px; margin: 12px 0; }
    code { background: #f0ece6; padding: 2px 6px; border-radius: 4px; }
    pre { background: #f6f3ee; border-radius: 8px; padding: 16px 20px; overflow-x: auto; margin: 12px 0; }
    pre code { background: none; }
    blockquote { border-left: 3px solid #d4cfc7; padding-left: 16px; color: #6b6560; margin: 12px 0; }
    mark { background: #fde68a; }
    hr { border: none; border-top: 1px solid #e8e3db; margin: 24px 0; }
    ul, ol { padding-left: 24px; margin: 8px 0; }
    table { border-collapse: collapse; width: 100%; margin: 12px 0; }
    th, td { border: 1px solid #e8e3db; padding: 8px 12px; }
    th { background: #f2efea; }
    a { color: #c4622d; }
  </style>
</head>
<body>
${bodyHtml}
</body>
</html>`
}
