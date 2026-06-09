<template>
  <div class="tiptap-editor">
    <EditorToolbar v-if="editor" :editor="editor" @insert-image="showImageDialog = true" @ai="showAiPanel = !showAiPanel" />
    <div class="connection-status" :class="connectionStatus">
      <span class="status-dot"></span>
      <span>{{ statusText }}</span>
      <span v-if="onlineUsers > 0" class="online-count">{{ onlineUsers }} online</span>
    </div>
    <div class="editor-content-wrapper">
      <EditorContent :editor="editor" class="editor-content" @click="editor?.commands.focus()" />
      <RemoteCursors v-if="editor && wsProvider" :editor="editor" :provider="wsProvider" />
    </div>
    <SlashMenu
      :show="slashMenu.show"
      :items="slashMenuItems"
      :position="slashMenu.position"
      @close="handleSlashClose"
      @select="handleSlashSelect"
    />
    <ImageUploadDialog
      v-if="editor"
      v-model="showImageDialog"
      :editor="editor"
      :workspace-id="workspaceId"
    />
    <AiAssistant
      :visible="showAiPanel"
      :selected-text="selectedText"
      @close="showAiPanel = false"
      @insert="handleAiInsert"
      @replace="handleAiReplace"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'
import Highlight from '@tiptap/extension-highlight'
import Underline from '@tiptap/extension-underline'
import { TextStyle } from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import { FontSize } from './extensions/font-size'
import { FontFamily } from './extensions/font-family'
import { PasteImage } from './extensions/paste-image'
import { CursorPlugin } from './extensions/cursor-plugin'
import SlashMenu from './SlashMenu.vue'
import RemoteCursors from './RemoteCursors.vue'
import { marked } from 'marked'
import { useDocumentStore } from '@/stores/document'

// Post-process marked HTML to add Tiptap TaskList compatible attributes
function parseMarkdownToHtml(md: string): string {
  let html = marked.parse(md) as string
  // Split merged lists: when a <ul> contains both checkbox and non-checkbox items,
  // separate them into distinct <ul> blocks so Tiptap can distinguish taskList from bulletList
  html = html.replace(/<ul>([\s\S]*?)<\/ul>/g, (match, body: string) => {
    const hasCheckbox = /<input[^>]*type="checkbox"/.test(body)
    if (!hasCheckbox) return match
    // Split items: group consecutive checkbox items as taskList, others as bulletList
    const items = body.match(/<li[\s\S]*?<\/li>/g) || []
    let result = ''
    let currentGroup: string[] = []
    let currentIsTask = false
    for (const item of items) {
      const isTask = /<input[^>]*type="checkbox"/.test(item)
      if (currentGroup.length > 0 && isTask !== currentIsTask) {
        result += buildList(currentGroup, currentIsTask)
        currentGroup = []
      }
      currentIsTask = isTask
      currentGroup.push(item)
    }
    if (currentGroup.length > 0) {
      result += buildList(currentGroup, currentIsTask)
    }
    return result
  })
  return html
}

function buildList(items: string[], isTask: boolean): string {
  const tag = isTask ? 'ul data-type="taskList"' : 'ul'
  const processed = items.map(item => {
    if (isTask) {
      // Ensure data-type="taskItem" on <li>, handle both <li>text and <li><p>text
      return item.replace(/^<li>/, '<li data-type="taskItem">')
    }
    return item
  })
  return `<${tag}>\n${processed.join('\n')}\n</ul>\n`
}
import type { SlashMenuItem } from './SlashMenu.vue'
import { uploadFile } from '@/api/file'
import { prosemirrorToMarkdown } from '@/utils/markdown'
import { prosemirrorToHtml } from '@/utils/html-export'
import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import Collaboration from '@tiptap/extension-collaboration'
import CollaborationCursor from '@tiptap/extension-collaboration-cursor'
import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import EditorToolbar from './EditorToolbar.vue'
import ImageUploadDialog from './ImageUploadDialog.vue'
import AiAssistant from './AiAssistant.vue'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  documentId: number
  workspaceId: number
}>()

const emit = defineEmits<{
  statusChange: [status: string]
}>()

const authStore = useAuthStore()
const docStore = useDocumentStore()
const showImageDialog = ref(false)
const showAiPanel = ref(false)

const selectedText = computed(() => {
  if (!editor.value) return ''
  const { from, to } = editor.value.state.selection
  return editor.value.state.doc.textBetween(from, to)
})

function handleAiInsert(text: string) {
  if (!editor.value) return
  const { to } = editor.value.state.selection
  editor.value.chain().focus().insertContentAt(to, text).run()
  showAiPanel.value = false
}

function handleAiReplace(text: string) {
  if (!editor.value) return
  const { from, to } = editor.value.state.selection
  editor.value.chain().focus().deleteRange({ from, to }).insertContentAt(from, text).run()
  showAiPanel.value = false
}
const connectionStatus = ref<'connecting' | 'connected' | 'disconnected'>('connecting')
const onlineUsers = ref(0)

// Slash menu
const slashMenu = ref({ show: false, position: { x: 0, y: 0 } })

const slashMenuItems: SlashMenuItem[] = [
  {
    label: 'Text',
    description: 'Plain text paragraph',
    icon: 'T',
    action: () => editor.value?.chain().focus().setParagraph().run(),
  },
  {
    label: 'Heading 1',
    description: 'Large section heading',
    icon: 'H1',
    action: () => editor.value?.chain().focus().toggleHeading({ level: 1 }).run(),
  },
  {
    label: 'Heading 2',
    description: 'Medium section heading',
    icon: 'H2',
    action: () => editor.value?.chain().focus().toggleHeading({ level: 2 }).run(),
  },
  {
    label: 'Heading 3',
    description: 'Small section heading',
    icon: 'H3',
    action: () => editor.value?.chain().focus().toggleHeading({ level: 3 }).run(),
  },
  {
    label: 'Bullet List',
    description: 'Unordered list',
    icon: '•',
    action: () => editor.value?.chain().focus().toggleBulletList().run(),
  },
  {
    label: 'Numbered List',
    description: 'Ordered list',
    icon: '1.',
    action: () => editor.value?.chain().focus().toggleOrderedList().run(),
  },
  {
    label: 'Task List',
    description: 'Checkbox task list',
    icon: '☑',
    action: () => editor.value?.chain().focus().toggleTaskList().run(),
  },
  {
    label: 'Code Block',
    description: 'Syntax highlighted code',
    icon: '</>',
    action: () => editor.value?.chain().focus().toggleCodeBlock().run(),
  },
  {
    label: 'Blockquote',
    description: 'Quoted text',
    icon: '“',
    action: () => editor.value?.chain().focus().toggleBlockquote().run(),
  },
  {
    label: 'Divider',
    description: 'Horizontal line',
    icon: '—',
    action: () => editor.value?.chain().focus().setHorizontalRule().run(),
  },
  {
    label: 'Image',
    description: 'Upload or embed image',
    icon: '🖼',
    action: () => { showImageDialog.value = true },
  },
]

const statusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connecting': return 'Connecting...'
    case 'connected': return 'Connected'
    case 'disconnected': return 'Offline'
  }
})

// Yjs setup
const docName = `doc-${props.documentId}`
const ydoc = new Y.Doc()
let wsProvider: WebsocketProvider | null = null
let indexeddbProvider: IndexeddbPersistence | null = null

// IndexedDB for offline persistence
indexeddbProvider = new IndexeddbPersistence(docName, ydoc)

// WebSocket for real-time sync
wsProvider = new WebsocketProvider('ws://localhost:1234', docName, ydoc)

// Connection status tracking
wsProvider.on('status', (event: { status: string }) => {
  connectionStatus.value = event.status as any
  emit('statusChange', event.status)
})

wsProvider.on('connection-close', () => {
  connectionStatus.value = 'disconnected'
})

wsProvider.on('connection-error', () => {
  connectionStatus.value = 'disconnected'
})

// Awareness — set current user info
const randomColor = '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0')
wsProvider.awareness.setLocalStateField('user', {
  name: authStore.displayName,
  color: randomColor,
})

// Track online users (debounced + nextTick to avoid re-render affecting editor)
let awarenessTimer: ReturnType<typeof setTimeout> | null = null
wsProvider.awareness.on('change', () => {
  if (awarenessTimer) clearTimeout(awarenessTimer)
  awarenessTimer = setTimeout(() => {
    const states = wsProvider!.awareness.getStates()
    nextTick(() => {
      onlineUsers.value = states.size
    })
  }, 500)
})

const yxmlFragment = ydoc.getXmlFragment('content')

const editor = useEditor({
  extensions: [
    StarterKit.configure({
      history: false, // Disable Tiptap history, use Yjs undo manager
    }),
    Image.configure({
      inline: true,
      allowBase64: true,
    }),
    Placeholder.configure({
      placeholder: 'Start writing...',
    }),
    Highlight,
    Underline,
    TextStyle,
    Color,
    FontSize,
    FontFamily,
    TaskList,
    TaskItem.configure({
      nested: true,
    }),
    Collaboration.configure({
      document: ydoc,
      field: 'content',
    }),
    // CollaborationCursor — temporarily disabled to test content shift issue
    // CollaborationCursor.configure({
    //   provider: wsProvider,
    //   user: {
    //     name: authStore.displayName,
    //     color: randomColor,
    //   },
    // }),
    PasteImage.configure({
      onImagePaste: async (file: File) => {
        const { uploadFile: uploadFn } = await import('@/api/file')
        try {
          const { data: response } = await uploadFn(file, props.workspaceId)
          editor.value?.chain().focus().setImage({ src: response.data.url }).run()
        } catch {}
      },
    }),
    CursorPlugin.configure({
      provider: wsProvider,
    }),
  ],
  onUpdate: () => {},
})

// Handle imported markdown content
watch(() => props.documentId, (docId) => {
  if (!docId) return
  const importData = docStore.consumePendingImport(docId)
  if (!importData) return

  // Import data is markdown text — parse to HTML and set directly
  const applyImport = () => {
    const ed = editor.value
    if (!ed) {
      setTimeout(applyImport, 200)
      return
    }
    const html = parseMarkdownToHtml(importData)
    ed.commands.setContent(html)
  }

  setTimeout(applyImport, 500)
}, { immediate: true })

// Detect "/" via keydown — only fires for local keyboard input
watch(editor, (e) => {
  if (!e) return
  e.view.dom.addEventListener('keydown', (ev: KeyboardEvent) => {
    if (ev.key === '/' && !slashMenu.value.show) {
      requestAnimationFrame(() => {
        const ed = editor.value
        if (!ed) return
        const { state } = ed
        const { from } = state.selection
        const textBefore = state.doc.textBetween(Math.max(0, from - 1), from)
        if (textBefore === '/') {
          const coords = ed.view.coordsAtPos(from)
          slashMenu.value = {
            show: true,
            position: { x: coords.left, y: coords.bottom + 8 },
          }
        }
      })
    } else if (slashMenu.value.show && ev.key !== 'Escape' && ev.key !== 'ArrowUp' && ev.key !== 'ArrowDown' && ev.key !== 'Enter' && ev.key !== 'Backspace') {
      // Close slash menu when typing non-slash character
      // (Backspace is handled by the slash menu filter)
      requestAnimationFrame(() => {
        const ed = editor.value
        if (!ed) return
        const { state } = ed
        const { from } = state.selection
        const lineText = state.doc.textBetween(Math.max(0, from - 20), from)
        if (!lineText.includes('/')) {
          slashMenu.value.show = false
        }
      })
    }
  })
})

function handleSlashSelect(item: SlashMenuItem) {
  // First delete the "/" and filter text
  handleSlashClose()
  // Then execute the command
  item.action()
}

function handleSlashClose() {
  slashMenu.value.show = false
  // Delete the "/" and any filter text typed after it
  const e = editor.value
  if (!e) return
  const { state } = e
  const { from } = state.selection
  const lineText = state.doc.textBetween(Math.max(0, from - 30), from)
  const lastSlash = lineText.lastIndexOf('/')
  if (lastSlash >= 0) {
    const deleteFrom = from - (lineText.length - lastSlash)
    e.chain().focus().deleteRange({ from: deleteFrom, to: from }).run()
  }
}

onBeforeUnmount(() => {
  if (awarenessTimer) clearTimeout(awarenessTimer)
  editor.value?.destroy()
  wsProvider?.destroy()
  indexeddbProvider?.destroy()
  ydoc.destroy()
})

defineExpose({
  exportMarkdown: () => {
    if (!editor.value) return ''
    return prosemirrorToMarkdown(editor.value.state.doc)
  },
  exportHTML: (title: string) => {
    if (!editor.value) return ''
    return prosemirrorToHtml(editor.value.state.doc, title)
  },
  setContent: (html: string) => {
    if (!editor.value) return
    editor.value.commands.setContent(html)
  },
  getCurrentHtml: () => {
    if (!editor.value) return ''
    return editor.value.getHTML()
  },
})
</script>

<style scoped>
.tiptap-editor {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 12px;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-border-light);
  font-weight: 500;
  letter-spacing: 0.01em;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-text-muted);
  transition: all 0.3s;
}

.connection-status.connected .status-dot {
  background: var(--color-success);
  box-shadow: 0 0 8px rgba(90, 154, 111, 0.5);
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 8px rgba(90, 154, 111, 0.5); }
  50% { box-shadow: 0 0 14px rgba(90, 154, 111, 0.7); }
}
.connection-status.connecting .status-dot {
  background: var(--color-warning);
  animation: blink 1s ease-in-out infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.connection-status.disconnected .status-dot {
  background: var(--color-danger);
}

.online-count {
  margin-left: auto;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.editor-content-wrapper {
  position: relative;
  flex: 1;
  overflow-y: auto;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
}

.editor-content :deep(.tiptap) {
  outline: none;
  min-height: 400px;
  padding: 12px 0;
  cursor: text;
  line-height: 1.7;
  font-size: 16px;
}

.editor-content :deep(.tiptap p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: var(--color-text-muted);
  pointer-events: none;
  height: 0;
  font-style: italic;
}

.editor-content :deep(.tiptap h1) {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 600;
  margin: var(--space-lg) 0 var(--space-sm);
  letter-spacing: -0.02em;
  line-height: 1.2;
  padding-bottom: var(--space-sm);
  border-bottom: 1px solid var(--color-border-light);
}
.editor-content :deep(.tiptap h2) {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  margin: var(--space-lg) 0 var(--space-sm);
  letter-spacing: -0.01em;
  line-height: 1.3;
}
.editor-content :deep(.tiptap h3) {
  font-family: var(--font-display);
  font-size: 19px;
  font-weight: 600;
  margin: var(--space-md) 0 var(--space-xs);
  line-height: 1.4;
}

.editor-content :deep(.tiptap ul),
.editor-content :deep(.tiptap ol) {
  padding-left: 24px;
}

.editor-content :deep(.tiptap blockquote) {
  border-left: 3px solid var(--color-primary);
  padding: var(--space-sm) var(--space-md);
  color: var(--color-text-secondary);
  margin: var(--space-sm) 0;
  background: var(--color-primary-light);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.editor-content :deep(.tiptap blockquote p) {
  margin: 0;
}

.editor-content :deep(.tiptap blockquote p + p) {
  margin-top: var(--space-xs);
}

.editor-content :deep(.tiptap pre) {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-md) var(--space-lg);
  font-family: 'SFMono-Regular', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  tab-size: 2;
}

.editor-content :deep(.tiptap img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--space-sm) 0;
  box-shadow: var(--shadow-sm);
}

.editor-content :deep(.tiptap ul[data-type="taskList"]) {
  list-style: none;
  padding-left: 0;
}
.editor-content :deep(.tiptap ul[data-type="taskList"] li) {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.editor-content :deep(.tiptap ul[data-type="taskList"] li label) {
  margin-top: 2px;
}

.editor-content :deep(.tiptap ul[data-type="taskList"] li input[type="checkbox"]) {
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.editor-content :deep(.tiptap code) {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  padding: 2px 6px;
  font-family: 'SFMono-Regular', 'Cascadia Code', Consolas, monospace;
  font-size: 0.9em;
  color: var(--color-primary);
}

.editor-content :deep(.tiptap hr) {
  border: none;
  height: 1px;
  background: var(--color-border);
  margin: var(--space-lg) 0;
}

/* Collaboration cursor styles */
.editor-content :deep(.collaboration-cursor__caret) {
  border-left: 1px solid;
  margin-left: -1px;
  margin-right: -1px;
  pointer-events: none;
  position: relative;
  word-break: normal;
}

.editor-content :deep(.collaboration-cursor__label) {
  border-radius: 3px;
  color: #fff;
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
  left: -1px;
  line-height: normal;
  padding: 1px 4px;
  position: absolute;
  top: -1.4em;
  user-select: none;
  white-space: nowrap;
}
</style>
