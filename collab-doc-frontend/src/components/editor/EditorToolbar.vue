<template>
  <div v-if="editor" class="toolbar">
    <div class="toolbar-group">
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('bold') }"
        @click="editor.chain().focus().toggleBold().run()"
        title="Bold"
      >
        <strong>B</strong>
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('italic') }"
        @click="editor.chain().focus().toggleItalic().run()"
        title="Italic"
      >
        <em>I</em>
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('underline') }"
        @click="editor.chain().focus().toggleUnderline().run()"
        title="Underline"
      >
        <u>U</u>
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('strike') }"
        @click="editor.chain().focus().toggleStrike().run()"
        title="Strikethrough"
      >
        <s>S</s>
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('highlight') }"
        @click="editor.chain().focus().toggleHighlight().run()"
        title="Highlight"
      >
        <span style="background: #ffe066; padding: 0 2px;">H</span>
      </button>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('heading', { level: 1 }) }"
        @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
        title="Heading 1"
      >H1</button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('heading', { level: 2 }) }"
        @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
        title="Heading 2"
      >H2</button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('heading', { level: 3 }) }"
        @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
        title="Heading 3"
      >H3</button>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <select class="font-select font-family-select" :value="currentFontFamily" @change="setFontFamily($event)">
        <option value="">Default</option>
        <option value="serif">Serif</option>
        <option value="SimSun, STSong, 宋体">宋体</option>
        <option value="SimHei, STHeiti, 黑体">黑体</option>
        <option value="KaiTi, STKaiti, 楷体">楷体</option>
        <option value="FangSong, STFangsong, 仿宋">仿宋</option>
        <option value="Arial, sans-serif">Arial</option>
        <option value="Georgia, serif">Georgia</option>
        <option value="Courier New, monospace">Courier New</option>
        <option value="Outfit, sans-serif">Outfit</option>
        <option value="Fraunces, serif">Fraunces</option>
      </select>
      <select class="font-select font-size-select" :value="currentFontSize" @change="setFontSize($event)">
        <option value="">Default</option>
        <option value="12px">12</option>
        <option value="14px">14</option>
        <option value="16px">16</option>
        <option value="18px">18</option>
        <option value="20px">20</option>
        <option value="24px">24</option>
        <option value="28px">28</option>
        <option value="32px">32</option>
        <option value="36px">36</option>
        <option value="48px">48</option>
      </select>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <div class="color-picker-wrapper" title="Text color">
        <button class="toolbar-btn" @click="openColorPicker">
          <span class="color-icon" :style="{ color: currentColor }">A</span>
        </button>
        <input ref="colorInputRef" type="color" class="color-input" :value="currentColor" @input="setColor" />
      </div>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('highlight') }"
        @click="editor.chain().focus().toggleHighlight().run()"
        title="Highlight"
      >
        <span class="color-icon" style="color: #fbbf24;">&#9679;</span>
      </button>
      <button class="toolbar-btn" @click="resetColor" title="Reset color">
        <el-icon><Close /></el-icon>
      </button>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('bulletList') }"
        @click="editor.chain().focus().toggleBulletList().run()"
        title="Bullet List"
      >
        <el-icon><List /></el-icon>
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('orderedList') }"
        @click="editor.chain().focus().toggleOrderedList().run()"
        title="Ordered List"
      >
        1.
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('taskList') }"
        @click="editor.chain().focus().toggleTaskList().run()"
        title="Task List"
      >
        <el-icon><Finished /></el-icon>
      </button>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('codeBlock') }"
        @click="editor.chain().focus().toggleCodeBlock().run()"
        title="Code Block"
      >
        &lt;/&gt;
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: editor.isActive('blockquote') }"
        @click="editor.chain().focus().toggleBlockquote().run()"
        title="Blockquote"
      >
        <el-icon><ChatDotRound /></el-icon>
      </button>
      <button class="toolbar-btn" @click="$emit('insertImage')" title="Insert Image">
        <el-icon><Picture /></el-icon>
      </button>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <button class="toolbar-btn" @click="editor.chain().focus().undo().run()" :disabled="!editor.can().undo()" title="Undo">
        <el-icon><RefreshLeft /></el-icon>
      </button>
      <button class="toolbar-btn" @click="editor.chain().focus().redo().run()" :disabled="!editor.can().redo()" title="Redo">
        <el-icon><RefreshRight /></el-icon>
      </button>
    </div>

    <div class="toolbar-divider"></div>

    <div class="toolbar-group">
      <button class="toolbar-btn ai-btn" @click="$emit('ai')" title="AI Assistant">
        <span style="font-size: 14px;">✦</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Editor } from '@tiptap/vue-3'

const props = defineProps<{
  editor: Editor
}>()

defineEmits<{
  insertImage: []
  ai: []
}>()

const colorInputRef = ref<HTMLInputElement>()
const defaultColor = '#2c2825'

const currentColor = computed(() => {
  return props.editor.getAttributes('textStyle').color || defaultColor
})

function openColorPicker() {
  colorInputRef.value?.click()
}

function setColor(e: Event) {
  const color = (e.target as HTMLInputElement).value
  props.editor.chain().focus().setColor(color).run()
}

function resetColor() {
  props.editor.chain().focus().unsetColor().run()
}

const currentFontSize = computed(() => {
  return props.editor.getAttributes('textStyle').fontSize || ''
})

const currentFontFamily = computed(() => {
  return props.editor.getAttributes('textStyle').fontFamily || ''
})

function setFontFamily(e: Event) {
  const font = (e.target as HTMLSelectElement).value
  if (font) {
    props.editor.chain().focus().setFontFamily(font).run()
  } else {
    props.editor.chain().focus().unsetFontFamily().run()
  }
}

function setFontSize(e: Event) {
  const size = (e.target as HTMLSelectElement).value
  if (size) {
    props.editor.chain().focus().setFontSize(size).run()
  } else {
    props.editor.chain().focus().unsetFontSize().run()
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-bottom: 1px solid var(--color-border-light);
  backdrop-filter: blur(8px);
  background: rgba(250, 248, 245, 0.85);
  flex-wrap: wrap;
  position: sticky;
  top: 0;
  z-index: var(--z-topbar);
}
.toolbar-group {
  display: flex;
  gap: 2px;
}
.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--color-border-light);
  margin: 0 6px;
}
.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text-secondary);
  transition: all 0.15s var(--ease-out);
}
.toolbar-btn:hover {
  background: var(--color-bg-hover);
  color: var(--color-text);
}
.toolbar-btn.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  box-shadow: inset 0 1px 2px rgba(196, 98, 45, 0.1);
}
.toolbar-btn:active:not(:disabled) {
  transform: scale(0.92);
  transition-duration: 0.05s;
}
.toolbar-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.color-picker-wrapper {
  position: relative;
}
.color-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}
.color-icon {
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}
.font-select {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: 4px 8px;
  font-size: 13px;
  font-family: var(--font-ui);
  color: var(--color-text);
  background: var(--color-bg);
  cursor: pointer;
  outline: none;
}
.font-family-select {
  min-width: 90px;
}
.font-size-select {
  min-width: 60px;
}
.font-select:hover {
  border-color: var(--color-text-muted);
}
.font-select:focus {
  border-color: var(--color-primary);
}
.ai-btn {
  color: var(--color-primary) !important;
  font-weight: 600;
}
</style>
