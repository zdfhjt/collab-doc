/**
 * Convert markdown text to Tiptap/ProseMirror JSON.
 * Uses a character-by-character state machine for reliable inline parsing.
 * Handles: headings, paragraphs, bold, italic, code, images, lists, code blocks,
 *          blockquotes, horizontal rules, and HTML inline tags.
 */

export function markdownToTiptapJson(markdown: string): object {
  const lines = markdown.split('\n')
  const content: any[] = []
  let i = 0

  while (i < lines.length) {
    const line = lines[i]

    if (line.startsWith('```')) {
      const lang = line.slice(3).trim()
      const codeLines: string[] = []
      i++
      while (i < lines.length && !lines[i].startsWith('```')) {
        codeLines.push(lines[i])
        i++
      }
      i++
      content.push({ type: 'codeBlock', attrs: { language: lang || null }, content: [{ type: 'text', text: codeLines.join('\n') }] })
      continue
    }

    const headingMatch = line.match(/^(#{1,6})\s+(.*)/)
    if (headingMatch) {
      content.push({ type: 'heading', attrs: { level: headingMatch[1].length }, content: parseInlineSM(headingMatch[2]) })
      i++; continue
    }

    if (/^(-{3,}|\*{3,}|_{3,})$/.test(line.trim())) {
      content.push({ type: 'horizontalRule' }); i++; continue
    }

    if (line.startsWith('> ')) {
      const ql: string[] = []
      while (i < lines.length && lines[i].startsWith('> ')) { ql.push(lines[i].slice(2)); i++ }
      content.push({ type: 'blockquote', content: [{ type: 'paragraph', content: parseInlineSM(ql.join(' ')) }] })
      continue
    }

    if (/^[\-\*\+]\s+/.test(line)) {
      const items: any[] = []
      while (i < lines.length && /^[\-\*\+]\s+/.test(lines[i])) {
        items.push({ type: 'listItem', content: [{ type: 'paragraph', content: parseInlineSM(lines[i].replace(/^[\-\*\+]\s+/, '')) }] })
        i++
      }
      content.push({ type: 'bulletList', content: items }); continue
    }

    if (/^\d+\.\s+/.test(line)) {
      const items: any[] = []
      while (i < lines.length && /^\d+\.\s+/.test(lines[i])) {
        items.push({ type: 'listItem', content: [{ type: 'paragraph', content: parseInlineSM(lines[i].replace(/^\d+\.\s+/, '')) }] })
        i++
      }
      content.push({ type: 'orderedList', content: items }); continue
    }

    if (line.trim() === '') { i++; continue }

    const imgLine = line.match(/^!\[([^\]]*)\]\(([^)]+)\)$/)
    if (imgLine) {
      content.push({ type: 'paragraph', content: [{ type: 'image', attrs: { src: imgLine[2], alt: imgLine[1] || '' } }] })
      i++; continue
    }

    content.push({ type: 'paragraph', content: parseInlineSM(line) })
    i++
  }

  // Validate: filter out any nodes with undefined type
  const validContent = content.filter(node => node && node.type)

  return { type: 'doc', content: validContent.length > 0 ? validContent : [{ type: 'paragraph', content: [{ type: 'text', text: '' }] }] }
}

/** Character-by-character inline parser — no regex alternation issues */
function parseInlineSM(text: string): any[] {
  const nodes: any[] = []
  let pos = 0

  function flushPlain(end: number) {
    if (end > pos) {
      nodes.push({ type: 'text', text: text.slice(pos, end) })
      pos = end
    }
  }

  while (pos < text.length) {
    const ch = text[pos]
    const rest = text.slice(pos)

    // --- HTML tags: <tag attrs>inner</tag> ---
    if (ch === '<') {
      const htmlMatch = rest.match(/^<(\w+)([^>]*)>([\s\S]*?)<\/\1>/)
      if (htmlMatch) {
        flushPlain(pos)
        nodes.push(convertHtmlTag(htmlMatch[1], htmlMatch[2], htmlMatch[3]))
        pos += htmlMatch[0].length
        continue
      }
    }

    // --- Markdown: ~~strikethrough~~ ---
    if (rest.startsWith('~~')) {
      const end = text.indexOf('~~', pos + 2)
      if (end !== -1) {
        flushPlain(pos)
        const inner = parseInlineSM(text.slice(pos + 2, end))
        nodes.push(...applyMarks(inner, [{ type: 'strike' }]))
        pos = end + 2
        continue
      }
    }

    // --- Markdown: ***bold+italic*** ---
    if (rest.startsWith('***')) {
      const end = text.indexOf('***', pos + 3)
      if (end !== -1) {
        flushPlain(pos)
        const inner = parseInlineSM(text.slice(pos + 3, end))
        nodes.push(...applyMarks(inner, [{ type: 'bold' }, { type: 'italic' }]))
        pos = end + 3
        continue
      }
    }

    // --- Markdown: **bold** ---
    if (rest.startsWith('**')) {
      const end = text.indexOf('**', pos + 2)
      if (end !== -1) {
        flushPlain(pos)
        const inner = parseInlineSM(text.slice(pos + 2, end))
        nodes.push(...applyMarks(inner, [{ type: 'bold' }]))
        pos = end + 2
        continue
      }
    }

    // --- Markdown: *italic* ---
    if (ch === '*') {
      const end = text.indexOf('*', pos + 1)
      if (end !== -1 && end > pos + 1) {
        flushPlain(pos)
        const inner = parseInlineSM(text.slice(pos + 1, end))
        nodes.push(...applyMarks(inner, [{ type: 'italic' }]))
        pos = end + 1
        continue
      }
    }

    // --- Markdown: `code` ---
    if (ch === '`') {
      const end = text.indexOf('`', pos + 1)
      if (end !== -1) {
        flushPlain(pos)
        nodes.push({ type: 'text', text: text.slice(pos + 1, end), marks: [{ type: 'code' }] })
        pos = end + 1
        continue
      }
    }

    // --- Markdown: ![alt](url) ---
    if (rest.startsWith('![')) {
      const imgMatch = rest.match(/^!\[([^\]]*)\]\(([^)]+)\)/)
      if (imgMatch) {
        flushPlain(pos)
        nodes.push({ type: 'image', attrs: { src: imgMatch[2], alt: imgMatch[1] || '' } })
        pos += imgMatch[0].length
        continue
      }
    }

    pos++
  }

  flushPlain(text.length)
  // Filter out nodes with undefined type
  const valid = nodes.filter(n => n && n.type)
  return valid.length > 0 ? valid : [{ type: 'text', text: text || '' }]
}

function convertHtmlTag(tag: string, attrs: string, inner: string): any {
  // Recursively parse inner content, then apply outer mark
  const innerNodes = parseInlineSM(inner)

  switch (tag) {
    case 'mark': return applyMarks(innerNodes, [{ type: 'highlight' }])
    case 's': case 'del': return applyMarks(innerNodes, [{ type: 'strike' }])
    case 'em': case 'i': return applyMarks(innerNodes, [{ type: 'italic' }])
    case 'strong': case 'b': return applyMarks(innerNodes, [{ type: 'bold' }])
    case 'a': {
      const href = attrs.match(/href="([^"]+)"/)
      return applyMarks(innerNodes, [{ type: 'link', attrs: { href: href?.[1] || '' } }])
    }
    case 'span': {
      const style = attrs.match(/style="([^"]+)"/)
      if (style) {
        const s = style[1]
        const textStyle: any = {}
        const colorM = s.match(/color:\s*([^;]+)/)
        const sizeM = s.match(/font-size:\s*([^;]+)/)
        const familyM = s.match(/font-family:\s*([^;]+)/)
        if (colorM) textStyle.color = colorM[1].trim()
        if (sizeM) textStyle.fontSize = sizeM[1].trim()
        if (familyM) textStyle.fontFamily = familyM[1].trim()
        if (Object.keys(textStyle).length > 0) {
          return applyMarks(innerNodes, [{ type: 'textStyle', attrs: textStyle }])
        }
      }
      return innerNodes
    }
    case 'img': {
      const src = attrs.match(/src="([^"]+)"/)
      const alt = attrs.match(/alt="([^"]*)"/)
      return [{ type: 'image', attrs: { src: src?.[1] || '', alt: alt?.[1] || '' } }]
    }
    default: return innerNodes
  }
}

/** Apply marks to all text nodes in a node list, preserving existing marks */
function applyMarks(nodes: any[], marks: any[]): any[] {
  return nodes.map(node => {
    if (node.type === 'text') {
      const existingMarks = node.marks || []
      return { ...node, marks: [...existingMarks, ...marks] }
    }
    return node
  })
}
