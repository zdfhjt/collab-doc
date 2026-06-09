<template>
  <div class="version-panel" v-if="visible">
    <div class="version-header">
      <span class="version-title">Version History</span>
      <el-button size="small" type="primary" text @click="$emit('save')">
        Save Version
      </el-button>
      <el-icon class="version-close" @click="$emit('close')"><Close /></el-icon>
    </div>
    <div class="version-list" v-if="versions.length > 0">
      <div
        v-for="v in versions"
        :key="v.id"
        class="version-item"
        :class="{ active: selectedVersion === v.version }"
        @click="handleSelect(v)"
      >
        <div class="version-meta">
          <span class="version-num">v{{ v.version }}</span>
          <span class="version-time">{{ formatTime(v.createdAt) }}</span>
        </div>
        <div class="version-preview">{{ v.content?.substring(0, 80) || 'No content' }}...</div>
      </div>
    </div>
    <div v-else class="version-empty">No versions saved yet</div>

    <!-- Restore button when version is selected -->
    <div class="restore-bar" v-if="selectedVersion">
      <el-button type="primary" size="small" @click="$emit('restore', selectedVersion)">
        Restore v{{ selectedVersion }}
      </el-button>
    </div>

    <!-- Diff view -->
    <div class="diff-panel" v-if="diffVisible">
      <div class="diff-header">
        <span>Version {{ selectedVersion }} vs Latest</span>
        <el-icon class="diff-close" @click="diffVisible = false"><Close /></el-icon>
      </div>
      <div class="diff-content">
        <div v-for="(line, i) in diffLines" :key="i" class="diff-line" :class="line.type">
          <span class="diff-prefix">{{ line.prefix }}</span>
          <span>{{ line.text }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getVersions } from '@/api/search'
import type { DocumentSnapshot } from '@/types/search'

const props = defineProps<{
  visible: boolean
  docId: number
}>()

const emit = defineEmits<{
  close: []
  save: []
  restore: [version: number]
}>()

const versions = ref<DocumentSnapshot[]>([])
const selectedVersion = ref<number | null>(null)
const diffVisible = ref(false)
const diffLines = ref<{ text: string; type: string; prefix: string }[]>([])

watch(() => props.visible, (val) => {
  if (val && props.docId) loadVersions()
})

async function loadVersions() {
  try {
    const { data: response } = await getVersions(props.docId)
    versions.value = response.data
  } catch {
    versions.value = []
  }
}

async function handleSelect(v: DocumentSnapshot) {
  selectedVersion.value = v.version
  if (!v.content) return
  const oldLines = v.content.split('\n')
  const latest = versions.value[0]
  const newLines = latest?.content?.split('\n') || []
  diffLines.value = computeDiff(oldLines, newLines)
  diffVisible.value = true
}

function computeDiff(oldLines: string[], newLines: string[]) {
  const result: { text: string; type: string; prefix: string }[] = []
  const maxLen = Math.max(oldLines.length, newLines.length)
  for (let i = 0; i < maxLen; i++) {
    const o = oldLines[i]
    const n = newLines[i]
    if (o === undefined) result.push({ text: n, type: 'added', prefix: '+' })
    else if (n === undefined) result.push({ text: o, type: 'removed', prefix: '-' })
    else if (o !== n) { result.push({ text: o, type: 'removed', prefix: '-' }); result.push({ text: n, type: 'added', prefix: '+' }) }
    else result.push({ text: o, type: 'unchanged', prefix: ' ' })
  }
  return result
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleString()
}

defineExpose({ loadVersions })
</script>

<style scoped>
.version-panel {
  position: fixed;
  top: 60px;
  right: 24px;
  width: 380px;
  max-height: calc(100dvh - 100px);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-overlay);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-light);
}
.version-title { font-weight: 600; font-size: 14px; }
.version-close { cursor: pointer; color: var(--color-text-muted); }
.version-close:hover { color: var(--color-text); }
.version-list { flex: 1; overflow-y: auto; padding: 8px; }
.version-item {
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s;
}
.version-item:hover { background: var(--color-bg-hover); }
.version-item.active { border-color: var(--color-primary); background: var(--color-primary-light); }
.version-meta { display: flex; justify-content: space-between; margin-bottom: 4px; }
.version-num { font-weight: 600; font-size: 13px; color: var(--color-primary); }
.version-time { font-size: 11px; color: var(--color-text-muted); }
.version-preview { font-size: 12px; color: var(--color-text-secondary); line-height: 1.3; }
.version-empty { padding: 32px; text-align: center; color: var(--color-text-muted); font-size: 13px; }
.restore-bar {
  padding: 10px 16px;
  border-top: 1px solid var(--color-border-light);
  display: flex;
  justify-content: center;
}
.diff-panel { border-top: 1px solid var(--color-border); max-height: 300px; overflow-y: auto; }
.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 500;
  background: var(--color-bg-secondary);
}
.diff-close { cursor: pointer; color: var(--color-text-muted); }
.diff-content { padding: 8px 12px; font-family: monospace; font-size: 12px; }
.diff-line { padding: 1px 4px; white-space: pre-wrap; }
.diff-line.added { background: rgba(90, 154, 111, 0.1); color: var(--color-success); }
.diff-line.removed { background: rgba(199, 80, 80, 0.1); color: var(--color-danger); }
.diff-line.unchanged { color: var(--color-text-muted); }
.diff-prefix { margin-right: 8px; user-select: none; }
</style>
