<template>
  <div v-if="visible" class="ai-panel">
    <div class="ai-header">
      <span class="ai-title">AI Assistant</span>
      <el-icon class="ai-close" @click="$emit('close')"><Close /></el-icon>
    </div>

    <div class="ai-actions" v-if="!streaming && !result">
      <div class="ai-selected-text" v-if="selectedText">
        <span class="ai-label">Selected text:</span>
        <div class="ai-text-preview">{{ selectedText.substring(0, 100) }}{{ selectedText.length > 100 ? '...' : '' }}</div>
      </div>
      <div class="ai-btn-grid">
        <button class="ai-btn" @click="handleAction('expand')">
          <span class="ai-btn-icon">📝</span>
          <span>Expand</span>
        </button>
        <button class="ai-btn" @click="handleAction('polish')">
          <span class="ai-btn-icon">✨</span>
          <span>Polish</span>
        </button>
        <button class="ai-btn" @click="handleAction('summarize')">
          <span class="ai-btn-icon">📋</span>
          <span>Summarize</span>
        </button>
        <button class="ai-btn" @click="showTranslate = !showTranslate">
          <span class="ai-btn-icon">🌐</span>
          <span>Translate</span>
        </button>
      </div>
      <div v-if="showTranslate" class="ai-translate">
        <el-select v-model="targetLang" placeholder="Target language" size="small" style="width: 100%">
          <el-option label="English" value="English" />
          <el-option label="中文" value="中文" />
          <el-option label="日本語" value="日本語" />
          <el-option label="한국어" value="한국어" />
          <el-option label="Español" value="Español" />
          <el-option label="Français" value="Français" />
          <el-option label="Deutsch" value="Deutsch" />
        </el-select>
        <el-button type="primary" size="small" style="margin-top: 8px; width: 100%" @click="handleAction('translate')">
          Translate
        </el-button>
      </div>
    </div>

    <div class="ai-result" v-if="streaming || result">
      <div class="ai-result-header">
        <span class="ai-result-action">{{ actionLabel }}</span>
        <span v-if="streaming" class="ai-streaming">Generating...</span>
      </div>
      <div class="ai-result-text" ref="resultRef">{{ result }}</div>
      <div class="ai-result-actions" v-if="!streaming && result">
        <el-button size="small" type="primary" @click="handleInsert">
          Insert
        </el-button>
        <el-button size="small" @click="handleReplace">
          Replace
        </el-button>
        <el-button size="small" @click="handleCopy">
          Copy
        </el-button>
        <el-button size="small" text @click="handleReset">
          New
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { streamAiResponse, type AiRequest } from '@/api/ai'

const props = defineProps<{
  visible: boolean
  selectedText: string
}>()

const emit = defineEmits<{
  close: []
  insert: [text: string]
  replace: [text: string]
}>()

const streaming = ref(false)
const result = ref('')
const showTranslate = ref(false)
const targetLang = ref('English')
const resultRef = ref<HTMLDivElement>()

const actionLabels: Record<string, string> = {
  expand: 'Expanding...',
  polish: 'Polishing...',
  summarize: 'Summarizing...',
  translate: 'Translating...',
}
const actionLabel = ref('')

function handleAction(action: string) {
  if (!props.selectedText) {
    ElMessage.warning('Please select text first')
    return
  }

  streaming.value = true
  result.value = ''
  actionLabel.value = actionLabels[action] || 'Processing...'

  const request: AiRequest = {
    action: action as any,
    text: props.selectedText,
  }
  if (action === 'translate') {
    request.targetLang = targetLang.value
  }

  const safetyTimer = setTimeout(() => {
    streaming.value = false
    if (!result.value) ElMessage.warning('Request timed out')
  }, 60000)

  streamAiResponse(
    request,
    (chunk) => {
      result.value += chunk
      nextTick(() => {
        if (resultRef.value) {
          resultRef.value.scrollTop = resultRef.value.scrollHeight
        }
      })
    },
    () => {
      clearTimeout(safetyTimer)
      streaming.value = false
    },
    (err) => {
      clearTimeout(safetyTimer)
      streaming.value = false
      ElMessage.error(err)
    }
  )
}

function handleInsert() {
  emit('insert', result.value)
  handleReset()
}

function handleReplace() {
  emit('replace', result.value)
  handleReset()
}

function handleCopy() {
  navigator.clipboard.writeText(result.value)
  ElMessage.success('Copied to clipboard')
}

function handleReset() {
  result.value = ''
  showTranslate.value = false
}
</script>

<style scoped>
.ai-panel {
  position: fixed;
  top: 80px;
  right: 24px;
  width: 340px;
  z-index: var(--z-overlay);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  max-height: calc(100dvh - 120px);
}
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border-light);
}
.ai-title {
  font-weight: 600;
  font-size: 14px;
}
.ai-close {
  cursor: pointer;
  color: var(--color-text-muted);
}
.ai-close:hover { color: var(--color-text); }

.ai-actions { padding: 12px; }
.ai-selected-text { margin-bottom: 12px; }
.ai-label { font-size: 12px; color: var(--color-text-muted); }
.ai-text-preview {
  font-size: 13px;
  color: var(--color-text);
  background: var(--color-bg-secondary);
  padding: 8px;
  border-radius: var(--radius-sm);
  margin-top: 4px;
  max-height: 60px;
  overflow-y: auto;
}
.ai-btn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.ai-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font-ui);
  transition: all 0.15s;
}
.ai-btn:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}
.ai-btn-icon { font-size: 16px; }
.ai-translate { margin-top: 8px; }

.ai-result { padding: 12px; flex: 1; display: flex; flex-direction: column; min-height: 0; }
.ai-result-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.ai-result-action { font-size: 13px; font-weight: 500; color: var(--color-primary); }
.ai-streaming { font-size: 12px; color: var(--color-text-muted); animation: blink 1.2s infinite; }
@keyframes blink { 50% { opacity: 0.4; } }

.ai-result-text {
  flex: 1;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
  padding: 12px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  white-space: pre-wrap;
  min-height: 100px;
  max-height: 300px;
}
.ai-result-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
