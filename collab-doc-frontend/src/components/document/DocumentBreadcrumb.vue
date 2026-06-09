<template>
  <div class="breadcrumb" v-if="path.length > 0">
    <span class="breadcrumb-item" @click="$emit('navigate', null)">
      {{ workspaceName }}
    </span>
    <template v-for="(item, index) in path" :key="item.id">
      <span class="breadcrumb-separator">/</span>
      <span
        class="breadcrumb-item"
        :class="{ active: index === path.length - 1 }"
        @click="$emit('navigate', item.id)"
      >
        {{ item.title }}
      </span>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { DocumentTreeNode } from '@/types/document'

const props = defineProps<{
  tree: DocumentTreeNode[]
  currentDocumentId: number | null
  workspaceName: string
}>()

defineEmits<{
  navigate: [documentId: number | null]
}>()

function findPath(nodes: DocumentTreeNode[], targetId: number): DocumentTreeNode[] | null {
  for (const node of nodes) {
    if (node.id === targetId) return [node]
    const childPath = findPath(node.children, targetId)
    if (childPath) return [node, ...childPath]
  }
  return null
}

const path = computed(() => {
  if (!props.currentDocumentId) return []
  return findPath(props.tree, props.currentDocumentId) || []
})

import { computed } from 'vue'
</script>

<style scoped>
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: 13px;
  color: var(--color-text-muted);
  padding: var(--space-sm) 0;
}
.breadcrumb-item {
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 4px;
  transition: all 0.15s;
}
.breadcrumb-item:hover {
  background: var(--color-bg-hover);
  color: var(--color-text);
}
.breadcrumb-item.active {
  color: var(--color-text);
  font-weight: 500;
  cursor: default;
}
.breadcrumb-item.active:hover {
  background: none;
}
.breadcrumb-separator {
  color: var(--color-text-muted);
  user-select: none;
}
</style>
