<template>
  <div class="tree-item" :style="{ paddingLeft: level * 16 + 8 + 'px' }">
    <div
      class="item-row"
      :class="{ 'is-folder': node.docType === 'FOLDER', 'is-renaming': isRenaming, 'drag-over': isDragOver, 'is-selected': selectedId === node.id, 'is-checked': selectMode && checkedIds?.has(node.id) }"
      draggable="true"
      @click="handleClick"
      @contextmenu.prevent="openContextMenu"
      @dragstart="onDragStart"
      @dragover.prevent="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
    >
      <span v-if="selectMode" class="select-checkbox" :class="{ checked: checkedIds?.has(node.id) }" @click.stop="emit('toggleCheck', node.id)">
        <el-icon v-if="checkedIds?.has(node.id)" :size="12"><Check /></el-icon>
      </span>
      <span class="expand-btn" v-if="node.docType === 'FOLDER'" @click.stop="toggleExpand">
        <el-icon :size="12"><ArrowRight v-if="!isExpanded" /><ArrowDown v-else /></el-icon>
      </span>
      <span v-else class="expand-placeholder"></span>

      <span class="item-icon" v-if="node.icon">{{ node.icon }}</span>
      <el-icon v-else-if="node.docType === 'FOLDER'" class="item-icon" :size="14"><Folder /></el-icon>
      <el-icon v-else class="item-icon" :size="14"><Document /></el-icon>

      <input
        v-if="isRenaming"
        ref="renameInputRef"
        class="rename-input"
        :value="node.title"
        @keydown.enter="confirmRename"
        @keydown.escape="cancelRename"
        @blur="confirmRename"
        @click.stop
      />
      <span v-else class="item-title">{{ node.title }}</span>

      <span class="item-actions" v-if="!isRenaming">
        <el-icon :size="14" @click.stop="$emit('createChild', node.id)"><Plus /></el-icon>
      </span>
    </div>

    <!-- Right-click context menu -->
    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click="contextMenu.visible = false"
      >
        <div class="context-menu-item" @click="startRename">
          <el-icon><Edit /></el-icon>
          <span>Rename</span>
        </div>
        <div class="context-menu-item" @click="$emit('createChild', node.id)">
          <el-icon><Plus /></el-icon>
          <span>Add sub-page</span>
        </div>
        <div class="context-menu-divider"></div>
        <div class="context-menu-item danger" @click="$emit('delete', node.id)">
          <el-icon><Delete /></el-icon>
          <span>Delete</span>
        </div>
      </div>
    </Teleport>

    <div v-if="isExpanded && node.children.length > 0" class="children">
      <DocumentTreeItem
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :level="level + 1"
        :selected-id="selectedId"
        :select-mode="selectMode"
        :checked-ids="checkedIds"
        @select="(n) => $emit('select', n)"
        @create-child="(id) => $emit('createChild', id)"
        @delete="(id) => $emit('delete', id)"
        @rename="(id, title) => $emit('rename', id, title)"
        @rename-state="(v) => $emit('renameState', v)"
        @move="(dId, tId, tType) => $emit('move', dId, tId, tType)"
        @toggle-check="(id) => $emit('toggleCheck', id)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import type { DocumentTreeNode } from '@/types/document'

const props = defineProps<{
  node: DocumentTreeNode
  level: number
  selectedId?: number | null
  selectMode?: boolean
  checkedIds?: Set<number>
}>()

const emit = defineEmits<{
  select: [node: DocumentTreeNode]
  createChild: [parentId: number]
  delete: [nodeId: number]
  rename: [nodeId: number, newTitle: string]
  renameState: [renaming: boolean]
  move: [draggedId: number, targetId: number, targetType: string]
  toggleCheck: [nodeId: number]
}>()

const isExpanded = ref(props.node.isExpanded ?? true)
const isRenaming = ref(false)
const isDragOver = ref(false)
const renameInputRef = ref<HTMLInputElement>()

const contextMenu = ref({ visible: false, x: 0, y: 0 })

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

function handleClick() {
  if (props.selectMode) {
    emit('toggleCheck', props.node.id)
    return
  }
  if (!isRenaming.value) {
    emit('select', props.node)
  }
}

function openContextMenu(e: MouseEvent) {
  contextMenu.value = { visible: true, x: e.clientX, y: e.clientY }
}

function closeContextMenu() {
  contextMenu.value.visible = false
}

async function startRename() {
  contextMenu.value.visible = false
  isRenaming.value = true
  emit('renameState', true)
  await nextTick()
  const input = renameInputRef.value
  if (input) {
    input.focus()
    input.select()
  }
}

function confirmRename(e: Event) {
  const target = e.target as HTMLInputElement
  const newTitle = target.value.trim()
  if (newTitle && newTitle !== props.node.title) {
    emit('rename', props.node.id, newTitle)
  }
  isRenaming.value = false
  emit('renameState', false)
}

function cancelRename() {
  isRenaming.value = false
  emit('renameState', false)
}

function handleGlobalClick(e: MouseEvent) {
  if (contextMenu.value.visible) {
    const menu = document.querySelector('.context-menu')
    if (menu && !menu.contains(e.target as Node)) {
      closeContextMenu()
    }
  }
}

// Drag and drop
function onDragStart(e: DragEvent) {
  if (isRenaming.value) return
  e.dataTransfer!.effectAllowed = 'move'
  e.dataTransfer!.setData('text/plain', String(props.node.id))
  e.dataTransfer!.setData('application/x-node-type', props.node.docType)
}

function onDragOver(e: DragEvent) {
  e.dataTransfer!.dropEffect = 'move'
  isDragOver.value = true
}

function onDragLeave() {
  isDragOver.value = false
}

function onDrop(e: DragEvent) {
  isDragOver.value = false
  const draggedId = Number(e.dataTransfer!.getData('text/plain'))
  if (draggedId === props.node.id) return
  emit('move', draggedId, props.node.id, props.node.docType)
}

onMounted(() => document.addEventListener('click', handleGlobalClick))
onBeforeUnmount(() => document.removeEventListener('click', handleGlobalClick))
</script>

<style scoped>
.tree-item {
  user-select: none;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text);
  transition: background 0.15s;
  position: relative;
}
.item-row:hover {
  background: var(--color-bg-hover);
}
.item-row.is-selected {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 500;
}
.item-row.is-checked {
  background: rgba(196, 98, 45, 0.08);
}
.select-checkbox {
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--color-border);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s var(--ease-out);
  cursor: pointer;
  color: #fff;
}
.select-checkbox.checked {
  background: var(--color-primary);
  border-color: var(--color-primary);
}
.item-row:hover .item-actions {
  opacity: 1;
}
.item-row.drag-over {
  background: var(--color-primary-light);
  outline: 2px dashed var(--color-primary);
  outline-offset: -2px;
}
.item-row.is-renaming {
  background: var(--color-bg-card);
}
.expand-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: var(--color-text-secondary);
}
.expand-placeholder {
  width: 16px;
  flex-shrink: 0;
}
.item-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  color: var(--color-text-secondary);
}
.item-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rename-input {
  flex: 1;
  border: 1px solid var(--color-primary);
  border-radius: 3px;
  padding: 1px 4px;
  font-size: 13px;
  outline: none;
  background: var(--color-bg-card);
  min-width: 0;
}
.item-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  color: var(--color-text-secondary);
  transition: opacity 0.15s;
}
.item-actions .el-icon {
  cursor: pointer;
  padding: 2px;
  border-radius: 3px;
}
.item-actions .el-icon:hover {
  background: var(--color-bg-active);
}
.children {
  /* recursive */
}
</style>

<style>
/* Global context menu styles (not scoped, rendered via Teleport) */
.context-menu {
  position: fixed;
  z-index: var(--z-dropdown);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  box-shadow: var(--shadow-lg);
  padding: 4px 0;
  min-width: 160px;
}
.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
  color: var(--color-text);
}
.context-menu-item:hover {
  background: var(--color-bg-hover);
}
.context-menu-item.danger {
  color: var(--color-danger);
}
.context-menu-item.danger:hover {
  background: var(--color-danger);
  color: #fff;
}
.context-menu-divider {
  height: 1px;
  background: var(--color-border);
  margin: 4px 0;
}
</style>
