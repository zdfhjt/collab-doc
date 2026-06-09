import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as docApi from '@/api/document'
import type { Document, DocumentTreeNode, DocumentCreateRequest, DocumentUpdateRequest } from '@/types/document'

export const useDocumentStore = defineStore('document', () => {
  const tree = ref<DocumentTreeNode[]>([])
  const currentDocument = ref<Document | null>(null)
  const loading = ref(false)
  const pendingImport = ref<string | null>(null)
  let pendingImportDocId = 0

  async function fetchTree(workspaceId: number) {
    loading.value = true
    try {
      const { data: response } = await docApi.getDocumentTree(workspaceId)
      tree.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function selectDocument(workspaceId: number, documentId: number) {
    loading.value = true
    try {
      const { data: response } = await docApi.getDocument(workspaceId, documentId)
      currentDocument.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function createDocument(workspaceId: number, request: { title: string; docType?: string; parentId?: number }) {
    const { data: response } = await docApi.createDocument(workspaceId, request)
    await fetchTree(workspaceId)
    return response.data
  }

  async function updateDocument(workspaceId: number, documentId: number, request: DocumentUpdateRequest) {
    const { data: response } = await docApi.updateDocument(workspaceId, documentId, request)
    if (currentDocument.value?.id === documentId) {
      currentDocument.value = { ...currentDocument.value, ...response.data }
    }
    if (request.title) {
      await fetchTree(workspaceId)
    }
    return response.data
  }

  async function deleteDocument(workspaceId: number, documentId: number) {
    await docApi.deleteDocument(workspaceId, documentId)
    if (currentDocument.value?.id === documentId) {
      currentDocument.value = null
    }
    await fetchTree(workspaceId)
  }

  async function batchDeleteDocuments(workspaceId: number, documentIds: number[]) {
    await docApi.batchDeleteDocuments(workspaceId, documentIds)
    if (currentDocument.value && documentIds.includes(currentDocument.value.id)) {
      currentDocument.value = null
    }
    await fetchTree(workspaceId)
  }

  async function moveDocument(workspaceId: number, documentId: number, newParentId: number | null) {
    await docApi.moveDocument(workspaceId, documentId, newParentId)
  }

  function setPendingImport(docId: number, content: string) {
    pendingImport.value = content
    pendingImportDocId = docId
  }

  function consumePendingImport(docId: number): string | null {
    if (pendingImportDocId === docId && pendingImport.value) {
      const data = pendingImport.value
      pendingImport.value = null
      pendingImportDocId = 0
      return data
    }
    return null
  }

  return {
    tree, currentDocument, loading, pendingImport, pendingImportDocId,
    setPendingImport, consumePendingImport,
    fetchTree, selectDocument, createDocument, updateDocument, deleteDocument, batchDeleteDocuments, moveDocument,
  }
})
