<template>
  <div class="editor-layout">
    <TopBar>
      <template #left>
        <el-button v-if="docStore.currentDocument" text size="small" @click="showSearch = !showSearch">
          <el-icon><Search /></el-icon>
        </el-button>
        <el-button v-if="docStore.currentDocument" text size="small" @click="showVersionHistory = !showVersionHistory">
          <el-icon><Clock /></el-icon>
          <span>History</span>
        </el-button>
        <span class="save-status" v-if="saveStatus">{{ saveStatus }}</span>
      </template>
      <template #center>
        <el-dropdown v-if="docStore.currentDocument" trigger="click">
          <el-button text size="small">
            <el-icon><Download /></el-icon>
            <span>Export</span>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="exportMarkdown">Markdown (.md)</el-dropdown-item>
              <el-dropdown-item @click="exportHTML">HTML (.html)</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </TopBar>

    <div class="editor-body">
      <DocumentSidebar
        :workspace-id="workspaceId"
        @select="handleDocumentSelect"
        @rename-state="handleRenameState"
      />

      <div class="editor-main">
        <template v-if="docStore.currentDocument">
          <div class="editor-wrapper">
            <DocumentBreadcrumb
              :tree="docStore.tree"
              :current-document-id="docStore.currentDocument?.id || null"
              :workspace-name="workspaceStore.currentWorkspace?.name || ''"
              @navigate="handleBreadcrumbNavigate"
            />
            <EditorTitleInput
              v-model="docTitle"
              @save="handleTitleSave"
            />
            <TiptapEditor
              v-if="docStore.currentDocument.docType === 'DOCUMENT'"
              ref="editorRef"
              :key="docStore.currentDocument.id"
              :document-id="docStore.currentDocument.id"
              :workspace-id="workspaceId"
              @status-change="handleStatusChange"
            />
          </div>
        </template>

        <div v-else class="editor-empty">
          <el-icon :size="48"><Document /></el-icon>
          <p>Select a document from the sidebar or create a new one</p>
        </div>
      </div>
    </div>

    <SearchPanel
      :visible="showSearch"
      :workspace-id="workspaceId"
      @close="showSearch = false"
      @select="handleDocumentSelect"
    />
    <VersionHistory
      ref="versionHistoryRef"
      :visible="showVersionHistory"
      :doc-id="docStore.currentDocument?.id || 0"
      @close="showVersionHistory = false"
      @save="handleSaveVersion"
      @restore="handleRestore"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useWorkspaceStore } from '@/stores/workspace'
import { useDocumentStore } from '@/stores/document'
import TopBar from '@/components/layout/TopBar.vue'
import DocumentSidebar from '@/components/document/DocumentSidebar.vue'
import DocumentBreadcrumb from '@/components/document/DocumentBreadcrumb.vue'
import EditorTitleInput from '@/components/document/EditorTitleInput.vue'
import TiptapEditor from '@/components/editor/TiptapEditor.vue'
import SearchPanel from '@/components/editor/SearchPanel.vue'
import VersionHistory from '@/components/editor/VersionHistory.vue'

import { getVersion, saveVersion } from '@/api/search'

const route = useRoute()
const router = useRouter()
const workspaceStore = useWorkspaceStore()
const docStore = useDocumentStore()

const workspaceId = computed(() => Number(route.params.workspaceId))
const documentId = computed(() => {
  const id = route.params.documentId
  return id ? Number(id) : null
})

const docTitle = ref('')
const saveStatus = ref('')
const editorRef = ref<InstanceType<typeof TiptapEditor>>()
const currentEditorHtml = computed(() => editorRef.value?.getCurrentHtml() || '')
let treeRefreshTimer: ReturnType<typeof setInterval> | null = null
let treeRefreshPaused = false
const showSearch = ref(false)
const showVersionHistory = ref(false)
const versionHistoryRef = ref<InstanceType<typeof VersionHistory>>()

function handleRenameState(renaming: boolean) {
  treeRefreshPaused = renaming
}

function exportMarkdown() {
  if (!editorRef.value) return
  const markdown = editorRef.value.exportMarkdown()
  const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${docTitle.value || 'document'}.md`
  a.click()
  URL.revokeObjectURL(url)
}

function exportHTML() {
  if (!editorRef.value) return
  const html = editorRef.value.exportHTML(docTitle.value || 'document')
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${docTitle.value || 'document'}.html`
  a.click()
  URL.revokeObjectURL(url)
}

watch(
  () => docStore.currentDocument,
  (doc) => {
    if (doc) {
      docTitle.value = doc.title
    }
  }
)

function handleStatusChange(status: string) {
  switch (status) {
    case 'connecting':
      saveStatus.value = 'Connecting...'
      break
    case 'connected':
      saveStatus.value = 'Connected'
      setTimeout(() => { saveStatus.value = '' }, 3000)
      break
    case 'disconnected':
      saveStatus.value = 'Offline — editing locally'
      break
  }
}

async function handleSaveVersion() {
  if (!editorRef.value || !docStore.currentDocument) return
  const content = editorRef.value.getCurrentHtml()
  if (!content) {
    ElMessage.warning('No content to save')
    return
  }
  try {
    await saveVersion(docStore.currentDocument.id, content)
    ElMessage.success('Version saved')
    versionHistoryRef.value?.loadVersions()
  } catch {
    ElMessage.error('Failed to save version')
  }
}

async function handleRestore(version: number) {
  if (!editorRef.value || !docStore.currentDocument) return
  try {
    const { data: response } = await getVersion(docStore.currentDocument.id, version)
    if (response.data?.content) {
      editorRef.value.setContent(response.data.content)
      ElMessage.success('Version v' + version + ' restored')
      showVersionHistory.value = false
    } else {
      ElMessage.warning('No content in this version')
    }
  } catch {
    ElMessage.error('Failed to load version')
  }
}

async function handleTitleSave() {
  if (!docStore.currentDocument || docTitle.value === docStore.currentDocument.title) return
  saveStatus.value = 'Saving title...'
  try {
    await docStore.updateDocument(workspaceId.value, docStore.currentDocument.id, { title: docTitle.value })
    saveStatus.value = 'Title saved'
    setTimeout(() => { saveStatus.value = '' }, 2000)
  } catch {
    saveStatus.value = 'Save failed'
  }
}

function handleDocumentSelect(documentId: number) {
  router.push(`/workspace/${workspaceId.value}/document/${documentId}`)
}

function handleBreadcrumbNavigate(documentId: number | null) {
  if (documentId) {
    router.push(`/workspace/${workspaceId.value}/document/${documentId}`)
  } else {
    router.push(`/workspace/${workspaceId.value}`)
  }
}

onMounted(async () => {
  await workspaceStore.selectWorkspace(workspaceId.value)
  await docStore.fetchTree(workspaceId.value)
  if (documentId.value) {
    await docStore.selectDocument(workspaceId.value, documentId.value)
  }
  // Refresh tree every 2s — lightweight query with Redis cache
  treeRefreshTimer = setInterval(() => {
    if (!treeRefreshPaused) {
      docStore.fetchTree(workspaceId.value)
    }
  }, 5000)
})

onBeforeUnmount(() => {
  if (treeRefreshTimer) clearInterval(treeRefreshTimer)
})

watch(
  () => route.params.documentId,
  async (newId) => {
    if (newId) {
      await docStore.selectDocument(workspaceId.value, Number(newId))
    } else {
      docStore.currentDocument = null
    }
  }
)
</script>

<style scoped>
.editor-layout {
  display: flex;
  flex-direction: column;
  height: 100dvh;
}
.editor-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  height: calc(100dvh - var(--topbar-height));
}
.editor-main {
  flex: 1;
  display: flex;
  overflow: auto;
  min-width: 0;
}
.editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 820px;
  width: 100%;
  margin: 0 auto;
  padding: 0 32px;
}
.editor-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  color: var(--color-text-muted);
}
.editor-empty p {
  font-size: 15px;
  max-width: 280px;
  text-align: center;
  line-height: 1.5;
}
.save-status {
  font-size: 12px;
  color: #999;
}
</style>
