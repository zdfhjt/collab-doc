export interface AiRequest {
  action: 'expand' | 'polish' | 'summarize' | 'translate' | 'chat'
  text: string
  targetLang?: string
}

export interface ChatEvent {
  eventType: number  // 1001=data, 1002=stop
  eventData: string | null
}

export function streamAiResponse(
  request: AiRequest,
  onChunk: (chunk: string) => void,
  onDone: () => void,
  onError: (err: string) => void
): AbortController {
  const controller = new AbortController()

  fetch('/api/ai/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${JSON.parse(localStorage.getItem('auth') || '{}').token || ''}`,
    },
    body: JSON.stringify(request),
    signal: controller.signal,
  })
    .then(async (res) => {
      if (!res.ok) {
        onError(`AI Error: ${res.status}`)
        onDone()
        return
      }

      const reader = res.body?.getReader()
      if (!reader) {
        onDone()
        return
      }

      const decoder = new TextDecoder()
      let buffer = ''

      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            const trimmed = line.trim()
            if (!trimmed.startsWith('data:')) continue

            const jsonStr = trimmed.slice(5).trim()
            if (!jsonStr) continue

            try {
              const event: ChatEvent = JSON.parse(jsonStr)
              if (event.eventType === 1001 && event.eventData) {
                onChunk(event.eventData)
              } else if (event.eventType === 1002) {
                onDone()
                return
              }
            } catch {
              // Not JSON
            }
          }
        }
        onDone()
      } catch {
        onError('Stream read error')
      }
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err.message)
      }
      onDone()
    })

  return controller
}
