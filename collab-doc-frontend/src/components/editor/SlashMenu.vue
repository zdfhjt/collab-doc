<template>
  <div v-if="show" class="slash-menu" :style="{ top: position.y + 'px', left: position.x + 'px' }">
    <div class="slash-search">
      <input
        ref="searchInput"
        v-model="query"
        placeholder="Filter commands..."
        @keydown.down.prevent="moveDown"
        @keydown.up.prevent="moveUp"
        @keydown.enter.prevent="selectCurrent"
        @keydown.escape="close"
      />
    </div>
    <div class="slash-list">
      <div
        v-for="(item, index) in filteredItems"
        :key="item.label"
        class="slash-item"
        :class="{ active: index === activeIndex }"
        @click="executeItem(item)"
        @mouseenter="activeIndex = index"
      >
        <div class="slash-item-icon">{{ item.icon }}</div>
        <div class="slash-item-info">
          <div class="slash-item-label">{{ item.label }}</div>
          <div class="slash-item-desc">{{ item.description }}</div>
        </div>
      </div>
      <div v-if="filteredItems.length === 0" class="slash-empty">
        No matching commands
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

export interface SlashMenuItem {
  label: string
  description: string
  icon: string
  action: () => void
}

const props = defineProps<{
  items: SlashMenuItem[]
  position: { x: number; y: number }
  show: boolean
}>()

const emit = defineEmits<{
  close: []
  select: [item: SlashMenuItem]
}>()

const query = ref('')
const activeIndex = ref(0)
const searchInput = ref<HTMLInputElement>()

const filteredItems = computed(() => {
  if (!query.value) return props.items
  const q = query.value.toLowerCase()
  return props.items.filter(
    (item) =>
      item.label.toLowerCase().includes(q) ||
      item.description.toLowerCase().includes(q)
  )
})

watch(() => props.show, (val) => {
  if (val) {
    query.value = ''
    activeIndex.value = 0
    nextTick(() => searchInput.value?.focus())
  }
})

watch(query, () => {
  activeIndex.value = 0
})

function moveDown() {
  if (activeIndex.value < filteredItems.value.length - 1) {
    activeIndex.value++
  }
}

function moveUp() {
  if (activeIndex.value > 0) {
    activeIndex.value--
  }
}

function selectCurrent() {
  const item = filteredItems.value[activeIndex.value]
  if (item) executeItem(item)
}

function executeItem(item: SlashMenuItem) {
  emit('select', item)
}

function close() {
  emit('close')
}
</script>

<style scoped>
.slash-menu {
  position: absolute;
  z-index: var(--z-dropdown);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  width: 280px;
  max-height: 320px;
  overflow: hidden;
  animation: slashIn 0.12s var(--ease-out);
}

@keyframes slashIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.slash-search {
  padding: 8px;
  border-bottom: 1px solid var(--color-border-light);
}

.slash-search input {
  width: 100%;
  border: none;
  outline: none;
  padding: 6px 8px;
  font-size: 13px;
  font-family: var(--font-ui);
  color: var(--color-text);
  background: transparent;
}

.slash-search input::placeholder {
  color: var(--color-text-muted);
}

.slash-list {
  max-height: 260px;
  overflow-y: auto;
  padding: 4px;
}

.slash-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.1s;
}

.slash-item.active,
.slash-item:hover {
  background: var(--color-bg-hover);
}

.slash-item.active .slash-item-icon {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.slash-item-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: var(--color-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.slash-item-info {
  flex: 1;
  min-width: 0;
}

.slash-item-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
}

.slash-item-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.slash-empty {
  padding: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted);
}
</style>
