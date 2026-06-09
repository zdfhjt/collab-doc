<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <router-link to="/workspaces" class="back-link">
        <el-icon><ArrowLeft /></el-icon>
      </router-link>
      <span class="workspace-name">{{ workspaceStore.currentWorkspace?.name || 'Workspace' }}</span>
      <el-button text size="small" @click="inviteDialog?.open()" title="Invite member">
        <el-icon><User /></el-icon>
      </el-button>
    </div>

    <div class="sidebar-actions">
      <template v-if="!selectMode">
        <el-button text size="small" @click="handleCreateDocument">
          <el-icon><Document /></el-icon>
          <span>New Page</span>
        </el-button>
        <el-button text size="small" @click="handleCreateFolder">
          <el-icon><Folder /></el-icon>
          <span>New Folder</span>
        </el-button>
        <el-button text size="small" @click="importDialogVisible = true">
          <el-icon><Upload /></el-icon>
          <span>Import</span>
        </el-button>
        <div class="action-divider"></div>
        <el-button text size="small" class="select-btn" @click="toggleSelectMode">
          <el-icon><Delete /></el-icon>
          <span>Select</span>
        </el-button>
      </template>
      <template v-else>
        <el-button size="small" @click="toggleSelectMode">
          Cancel
        </el-button>
        <el-button
          v-if="checkedIds.size > 0"
          type="danger"
          size="small"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          Delete ({{ checkedIds.size }})
        </el-button>
        <span v-else class="select-hint">Click pages to select</span>
      </template>
    </div>

    <div class="sidebar-tree">
      <div v-if="docStore.tree.length === 0" class="empty-tree">
        <p>No pages yet</p>
      </div>
      <DocumentTreeItem
        v-for="node in docStore.tree"
        :key="node.id"
        :node="node"
        :level="0"
        :selected-id="currentDocumentId"
        :select-mode="selectMode"
        :checked-ids="checkedIds"
        @select="handleSelect"
        @create-child="handleCreateChild"
        @delete="handleDelete"
        @rename="handleRename"
        @rename-state="(v) => emit('renameState', v)"
        @move="handleMove"
        @toggle-check="toggleCheck"
      />
    </div>

    <InviteMemberDialog ref="inviteDialog" :workspace-id="workspaceId" />

    <!-- Import Markdown Dialog -->
    <el-dialog v-model="importDialogVisible" title="Import Markdown" width="420px">
      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        accept=".md,.markdown,.txt"
        :on-change="handleImportFileChange"
        :on-exceed="() => ElMessage.warning('Only one file at a time')"
      >
        <el-icon :size="40"><UploadFilled /></el-icon>
        <div>Drop .md file here or <em>click to browse</em></div>
      </el-upload>
      <el-form v-if="importFile" label-position="top" style="margin-top: 16px">
        <el-form-item label="Document Title">
          <el-input v-model="importTitle" placeholder="Document title" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="importing" :disabled="!importFile" @click="handleImport">Import</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspace'
import { useDocumentStore } from '@/stores/document'
import { importExternalImage } from '@/api/file'
import { markdownToTiptapJson } from '@/utils/markdown-to-json'
import DocumentTreeItem from './DocumentTreeItem.vue'
import InviteMemberDialog from '@/components/workspace/InviteMemberDialog.vue'
import type { DocumentTreeNode } from '@/types/document'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{ workspaceId: number }>()
const emit = defineEmits<{
  select: [documentId: number]
  renameState: [renaming: boolean]
}>()
const route = useRoute()
const workspaceStore = useWorkspaceStore()
const docStore = useDocumentStore()

const currentDocumentId = computed(() => {
  const id = route.params.documentId
  return id ? Number(id) : null
})
const inviteDialog = ref<InstanceType<typeof InviteMemberDialog>>()
import type { UploadFile } from 'element-plus'

const importDialogVisible = ref(false)
const importFile = ref<File | null>(null)
const importTitle = ref('')
const importing = ref(false)

// Batch select mode
const selectMode = ref(false)
const checkedIds = ref<Set<number>>(new Set())

function toggleSelectMode() {
  selectMode.value = !selectMode.value
  if (!selectMode.value) {
    checkedIds.value = new Set()
  }
}

function toggleCheck(nodeId: number) {
  const next = new Set(checkedIds.value)
  if (next.has(nodeId)) {
    next.delete(nodeId)
  } else {
    next.add(nodeId)
  }
  checkedIds.value = next
}

async function handleBatchDelete() {
  if (checkedIds.value.size === 0) return
  const count = checkedIds.value.size
  try {
    await ElMessageBox.confirm(
      `Delete ${count} selected page${count > 1 ? 's' : ''} and all their children?`,
      'Batch Delete',
      { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning' }
    )
    const ids = Array.from(checkedIds.value)
    await docStore.batchDeleteDocuments(props.workspaceId, ids)
    checkedIds.value = new Set()
    selectMode.value = false
    ElMessage.success(`Deleted ${count} page${count > 1 ? 's' : ''}`)
  } catch {
    // cancelled
  }
}

function handleSelect(node: DocumentTreeNode) {
  emit('select', node.id)
}

async function handleCreateDocument(parentId?: number | null) {
  try {
    const doc = await docStore.createDocument(props.workspaceId, {
      title: 'Untitled',
      docType: 'DOCUMENT',
      parentId: parentId || undefined,
    })
    if (doc) {
      emit('select', doc.id)
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to create document')
  }
}

async function handleCreateFolder() {
  try {
    await docStore.createDocument(props.workspaceId, {
      title: 'New Folder',
      docType: 'FOLDER',
    })
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to create folder')
  }
}

async function handleCreateChild(parentId: number) {
  try {
    await docStore.createDocument(props.workspaceId, {
      title: 'Untitled',
      docType: 'DOCUMENT',
      parentId,
    })
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to create document')
  }
}

async function handleDelete(nodeId: number) {
  try {
    await ElMessageBox.confirm('Delete this page and all its children?', 'Confirm Delete', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
    await docStore.deleteDocument(props.workspaceId, nodeId)
  } catch {
    // cancelled
  }
}

async function handleRename(nodeId: number, newTitle: string) {
  try {
    // Update document (this also refreshes the tree immediately)
    await docStore.updateDocument(props.workspaceId, nodeId, { title: newTitle })
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to rename')
  } finally {
    // Resume tree refresh AFTER update completes
    emit('renameState', false)
  }
}

function handleImportFileChange(uploadFile: UploadFile) {
  importFile.value = uploadFile.raw || null
  if (uploadFile.raw) {
    importTitle.value = uploadFile.name.replace(/\.(md|markdown|txt)$/i, '')
  }
}

async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  try {
    let markdown = await importFile.value.text()

    // Find image URLs in markdown: ![alt](url)
    const imgRegex = /!\[([^\]]*)\]\(([^)]+)\)/g
    const imageUrls = new Set<string>()
    let match
    while ((match = imgRegex.exec(markdown)) !== null) {
      const url = match[2].trim()
      if (url && !url.startsWith('data:')) {
        imageUrls.add(url)
      }
    }

    // Import each external image to MinIO (backend handles download + upload)
    for (const url of imageUrls) {
      try {
        const { data: response } = await importExternalImage(url, props.workspaceId)
        if (response.data && response.data !== url) {
          markdown = markdown.split(url).join(response.data)
        }
      } catch {
        // Keep original URL if import fails
      }
    }

    const doc = await docStore.createDocument(props.workspaceId, {
      title: importTitle.value || 'Imported Document',
      docType: 'DOCUMENT',
    })

    if (doc) {
      // Store raw markdown for TiptapEditor to parse via marked
      docStore.setPendingImport(doc.id, markdown)
      importDialogVisible.value = false
      importFile.value = null
      importTitle.value = ''
      emit('select', doc.id)
      ElMessage.success('Markdown imported')
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to import')
  } finally {
    importing.value = false
  }
}

function handleRenameStart() {
  emit('renameState', true)
}

async function handleMove(draggedId: number, targetId: number, targetType: string) {
  try {
    if (targetType === 'FOLDER') {
      // Drop onto folder → make it a child of the folder
      await docStore.moveDocument(props.workspaceId, draggedId, targetId)
    } else {
      // Drop onto document → make it a sibling (same parent)
      // Find target's parent from the tree
      const targetNode = findNode(docStore.tree, targetId)
      const newParentId = targetNode?.parentId || null
      await docStore.moveDocument(props.workspaceId, draggedId, newParentId)
    }
    await docStore.fetchTree(props.workspaceId)
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to move')
  }
}

function findNode(nodes: DocumentTreeNode[], id: number): DocumentTreeNode | null {
  for (const node of nodes) {
    if (node.id === id) return node
    const found = findNode(node.children, id)
    if (found) return found
  }
  return null
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100%;
  background: var(--color-bg);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-bg-secondary);
}
.back-link {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s var(--ease-out);
}
.back-link:hover {
  color: var(--color-text);
  background: var(--color-bg-hover);
}
.workspace-name {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.sidebar-actions {
  display: flex;
  gap: 4px;
  padding: 8px 10px;
  border-bottom: 1px solid var(--color-border-light);
  align-items: center;
  flex-wrap: wrap;
}
.sidebar-actions :deep(.el-button) {
  transition: all 0.15s var(--ease-out);
  border-radius: var(--radius-sm);
}
.sidebar-actions :deep(.el-button:hover) {
  background: var(--color-bg-hover);
  color: var(--color-primary);
}
.action-divider {
  width: 1px;
  height: 16px;
  background: var(--color-border);
  margin: 0 2px;
  flex-shrink: 0;
}
.select-btn {
  color: var(--color-danger) !important;
}
.select-btn:hover {
  background: rgba(199, 80, 80, 0.08) !important;
  color: var(--color-danger) !important;
}
.sidebar-tree {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;
}
.empty-tree {
  text-align: center;
  padding: 48px 16px;
  color: var(--color-text-muted);
  font-size: 13px;
}
.select-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  padding-left: 4px;
}
</style>
