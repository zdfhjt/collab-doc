<template>
  <div class="remote-cursors" v-if="editor">
    <div
      v-for="(cursor, id) in remoteCursors"
      :key="id"
      class="remote-cursor"
      :style="cursor.style"
    >
      <div class="cursor-line" :style="{ background: cursor.color }"></div>
      <div class="cursor-label" :style="{ background: cursor.color }">{{ cursor.name }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, type Ref } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import { WebsocketProvider } from 'y-websocket'

interface CursorInfo {
  name: string
  color: string
  style: Record<string, string>
}

const props = defineProps<{
  editor: Editor
  provider: WebsocketProvider
}>()

const remoteCursors = ref<Record<string, CursorInfo>>({})
let updateTimer: ReturnType<typeof setInterval> | null = null

function updateCursors() {
  if (!props.provider || !props.editor) return

  const states = props.provider.awareness.getStates()
  const myId = props.provider.awareness.clientID
  const newCursors: Record<string, CursorInfo> = {}

  states.forEach((state, clientId) => {
    if (clientId === myId) return
    const user = state.user
    if (!user) return

    // Try to get cursor position from awareness state
    const cursor = state.cursor
    if (!cursor || !cursor.anchor) return

    try {
      const { anchor } = cursor
      const view = props.editor.view
      const coords = view.coordsAtPos(anchor)

      newCursors[clientId] = {
        name: user.name || 'Anonymous',
        color: user.color || '#999',
        style: {
          left: `${coords.left - view.dom.getBoundingClientRect().left}px`,
          top: `${coords.top - view.dom.getBoundingClientRect().top}px`,
          height: `${coords.bottom - coords.top}px`,
        },
      }
    } catch {
      // Position not valid, skip
    }
  })

  remoteCursors.value = newCursors
}

onMounted(() => {
  // Update cursors periodically
  updateTimer = setInterval(updateCursors, 200)

  // Also update on awareness changes
  props.provider.awareness.on('change', () => {
    setTimeout(updateCursors, 50)
  })
})

onBeforeUnmount(() => {
  if (updateTimer) clearInterval(updateTimer)
})
</script>

<style scoped>
.remote-cursors {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 10;
}
.remote-cursor {
  position: absolute;
  pointer-events: none;
  transition: left 0.1s, top 0.1s;
}
.cursor-line {
  width: 2px;
  height: 100%;
  border-radius: 1px;
}
.cursor-label {
  position: absolute;
  top: -20px;
  left: 0;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  line-height: 1.4;
}
</style>
