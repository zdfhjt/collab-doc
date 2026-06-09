import type { Node, Mark } from 'prosemirror-model'

function wrapInline(text: string, marks: readonly Mark[]): string {
  const styles: string[] = []

  for (const mark of marks) {
    if (mark.type.name === 'textStyle') {
      if (mark.attrs?.color) styles.push(`color: ${mark.attrs.color}`)
      if (mark.attrs?.fontSize) styles.push(`font-size: ${mark.attrs.fontSize}`)
      if (mark.attrs?.fontFamily) styles.push(`font-family: ${mark.attrs.fontFamily}`)
    }
  }

  for (const mark of marks) {
    if (mark.type.name === 'bold') text = `**${text}**`
    if (mark.type.name === 'italic') text = `*${text}*`
    if (mark.type.name === 'code') text = `\`${text}\``
    if (mark.type.name === 'strike') text = `~~${text}~~`
    if (mark.type.name === 'highlight') text = `<mark>${text}</mark>`
    if (mark.type.name === 'link') text = `[${text}](${mark.attrs?.href || ''})`
  }

  if (styles.length > 0) {
    text = `<span style="${styles.join('; ')}">${text}</span>`
  }

  return text
}

function renderInline(node: Node): string {
  if (node.isText) {
    return wrapInline(node.text || '', node.marks || [])
  }
  if (node.type.name === 'image') {
    const src = node.attrs.src || ''
    const alt = node.attrs.alt || 'image'
    return `![${alt}](${src})`
  }
  if (node.children) {
    return node.children.map((c: Node) => renderInline(c)).join('')
  }
  return ''
}

function renderBlock(node: Node): string[] {
  if (!node.isBlock) {
    return [renderInline(node)]
  }

  switch (node.type.name) {
    case 'heading': {
      const level = node.attrs.level || 1
      const prefix = '#'.repeat(level)
      const content = node.children.map((c: Node) => renderInline(c)).join('')
      return [`${prefix} ${content}`, '']
    }
    case 'paragraph': {
      const content = node.children.map((c: Node) => renderInline(c)).join('')
      return [content, '']
    }
    case 'bulletList': {
      const lines: string[] = []
      node.children.forEach((child: Node) => {
        lines.push(...renderListItem(child, 'bullet', 0))
      })
      lines.push('')
      return lines
    }
    case 'orderedList': {
      const lines: string[] = []
      node.children.forEach((child: Node) => {
        lines.push(...renderListItem(child, 'ordered', 0))
      })
      lines.push('')
      return lines
    }
    case 'taskList': {
      const lines: string[] = []
      node.children.forEach((child: Node) => {
        const checked = child.attrs.checked
        const checkbox = checked ? '[x]' : '[ ]'
        const content = child.children.map((c: Node) => {
          if (c.type.name === 'taskItem') {
            return c.children.map((cc: Node) => renderInline(cc)).join('')
          }
          return renderInline(c)
        }).join('')
        lines.push(`- ${checkbox} ${content}`)
      })
      lines.push('')
      return lines
    }
    case 'codeBlock': {
      const lang = node.attrs.language || ''
      return [`\`\`\`${lang}`, node.textContent, '```', '']
    }
    case 'blockquote': {
      const innerLines: string[] = []
      node.children.forEach((child: Node) => {
        innerLines.push(...renderBlock(child))
      })
      // Remove trailing empty line from child blocks, then prefix with >
      while (innerLines.length > 0 && innerLines[innerLines.length - 1] === '') {
        innerLines.pop()
      }
      return [...innerLines.map(line => `> ${line}`), '']
    }
    case 'horizontalRule': {
      return ['---', '']
    }
    case 'table': {
      const lines: string[] = []
      node.children.forEach((row: Node, rowIdx: number) => {
        const cells = row.children.map((cell: Node) => {
          return cell.children.map((c: Node) => renderInline(c)).join('')
        })
        lines.push(`| ${cells.join(' | ')} |`)
        if (rowIdx === 0) {
          lines.push(`| ${cells.map(() => '---').join(' | ')} |`)
        }
      })
      lines.push('')
      return lines
    }
    default: {
      // Recurse into unknown block types
      const lines: string[] = []
      node.children.forEach((c: Node) => {
        lines.push(...renderBlock(c))
      })
      return lines
    }
  }
}

function renderListItem(node: Node, listType: 'bullet' | 'ordered', indent: number): string[] {
  const lines: string[] = []
  const bullet = listType === 'ordered' ? '1. ' : '- '
  const prefix = `${'  '.repeat(indent)}${bullet}`

  node.children.forEach((child: Node) => {
    if (child.isBlock && (child.type.name === 'bulletList' || child.type.name === 'orderedList')) {
      // Nested list
      const nestedType = child.type.name === 'bulletList' ? 'bullet' : 'ordered'
      child.children.forEach((nestedItem: Node) => {
        lines.push(...renderListItem(nestedItem, nestedType, indent + 1))
      })
    } else {
      const content = renderInline(child)
      if (lines.length === 0) {
        lines.push(`${prefix}${content}`)
      } else {
        lines.push(`${'  '.repeat(indent + 1)}${content}`)
      }
    }
  })

  return lines
}

export function prosemirrorToMarkdown(doc: Node): string {
  const lines: string[] = []

  doc.children.forEach((child: Node) => {
    lines.push(...renderBlock(child))
  })

  return lines.join('\n').replace(/\n{3,}/g, '\n\n').trim() + '\n'
}
