const ws = require('ws')
const Y = require('yjs')
const { setupWSConnection, getYDoc } = require('y-websocket/bin/utils')

const PORT = process.env.PORT || 1234
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080'

const wss = new ws.Server({ port: PORT })
const trackedDocs = new Map()

wss.on('connection', (socket, req) => {
  const docName = req.url?.slice(1)?.split('?')[0] || 'default'
  setupWSConnection(socket, req, { docName, gc: true })

  // Track doc for periodic snapshot saving
  if (!trackedDocs.has(docName)) {
    const doc = getYDoc(docName, { gc: true })
    trackedDocs.set(docName, doc)
    console.log(`Tracking doc: ${docName}`)
  }

  console.log(`Client connected to doc: ${docName}`)
})

console.log(`Collaboration server running on ws://localhost:${PORT}`)

// Save snapshots every 30 seconds
setInterval(() => {
  for (const [name, doc] of trackedDocs) {
    try {
      const docId = name.replace('doc-', '')
      if (!docId || isNaN(Number(docId))) continue

      const state = Y.encodeStateAsUpdate(doc)
      const base64 = Buffer.from(state).toString('base64')
      const content = JSON.stringify(doc.getXmlFragment('content').toJSON())

      fetch(`${BACKEND_URL}/api/documents/${docId}/snapshot`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ yjsState: base64, content }),
      }).then(res => {
        if (res.ok) console.log(`Snapshot saved for doc ${docId}`)
      }).catch(err => {
        console.warn(`Snapshot error for doc ${docId}:`, err.message)
      })
    } catch (err) {
      console.warn(`Snapshot error for doc ${name}:`, err.message)
    }
  }
}, 30000)
