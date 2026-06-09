<template>
  <div class="search-panel" v-if="visible">
    <div class="search-header">
      <span class="search-title">Search</span>
      <el-icon class="search-close" @click="$emit('close')"><Close /></el-icon>
    </div>
    <div class="search-input">
      <el-input
        v-model="keyword"
        placeholder="Search documents..."
        size="small"
        clearable
        @keyup.enter="handleSearch"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    <div class="search-results" v-if="results.length > 0">
      <div
        v-for="result in results"
        :key="result.id"
        class="search-result-item"
        @click="$emit('select', Number(result.id))"
      >
        <div class="result-title">{{ result.title }}</div>
        <div class="result-snippet">{{ result.content.substring(0, 120) }}...</div>
      </div>
    </div>
    <div v-else-if="keyword && !loading" class="search-empty">
      No results found
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { searchDocuments } from '@/api/search'
import type { DocumentIndex } from '@/types/search'

const props = defineProps<{
  visible: boolean
  workspaceId: number
}>()

defineEmits<{
  close: []
  select: [docId: number]
}>()

const keyword = ref('')
const results = ref<DocumentIndex[]>([])
const loading = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

function handleSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!keyword.value.trim()) {
    results.value = []
    return
  }

  searchTimer = setTimeout(async () => {
    loading.value = true
    try {
      const { data: response } = await searchDocuments(props.workspaceId, keyword.value.trim())
      results.value = response.data
    } catch {
      results.value = []
    } finally {
      loading.value = false
    }
  }, 300)
}
</script>

<style scoped>
.search-panel {
  position: fixed;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  width: 480px;
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
.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-light);
}
.search-title { font-weight: 600; font-size: 14px; }
.search-close { cursor: pointer; color: var(--color-text-muted); }
.search-close:hover { color: var(--color-text); }
.search-input { padding: 12px 16px; }
.search-results { flex: 1; overflow-y: auto; padding: 0 8px 8px; }
.search-result-item {
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s;
}
.search-result-item:hover { background: var(--color-bg-hover); }
.result-title { font-weight: 500; font-size: 14px; margin-bottom: 4px; }
.result-snippet { font-size: 12px; color: var(--color-text-secondary); line-height: 1.4; }
.search-empty { padding: 32px; text-align: center; color: var(--color-text-muted); font-size: 13px; }
</style>
