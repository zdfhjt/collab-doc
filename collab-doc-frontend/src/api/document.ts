import api from './index'
import type { ApiResponse } from '@/types/common'
import type { Document, DocumentTreeNode, DocumentCreateRequest, DocumentUpdateRequest } from '@/types/document'

export function getDocumentTree(workspaceId: number) {
  return api.get<ApiResponse<DocumentTreeNode[]>>(`/workspaces/${workspaceId}/documents`)
}

export function getDocument(workspaceId: number, documentId: number) {
  return api.get<ApiResponse<Document>>(`/workspaces/${workspaceId}/documents/${documentId}`)
}

export function createDocument(workspaceId: number, data: {
  title: string
  docType?: string
  parentId?: number
}) {
  const body: Record<string, any> = { title: data.title }
  if (data.docType) body.docType = data.docType
  if (data.parentId) body.parentId = data.parentId
  return api.post<ApiResponse<Document>>(`/workspaces/${workspaceId}/documents`, body)
}

export function updateDocument(workspaceId: number, documentId: number, data: DocumentUpdateRequest) {
  return api.put<ApiResponse<Document>>(`/workspaces/${workspaceId}/documents/${documentId}`, data)
}

export function deleteDocument(workspaceId: number, documentId: number) {
  return api.delete<ApiResponse<void>>(`/workspaces/${workspaceId}/documents/${documentId}`)
}

export function batchDeleteDocuments(workspaceId: number, documentIds: number[]) {
  return api.post<ApiResponse<void>>(`/workspaces/${workspaceId}/documents/batch-delete`, documentIds)
}

export function moveDocument(workspaceId: number, documentId: number, newParentId: number | null) {
  const params: Record<string, any> = {}
  if (newParentId) params.newParentId = newParentId
  return api.put<ApiResponse<Document>>(
    `/workspaces/${workspaceId}/documents/${documentId}/move`,
    null,
    { params }
  )
}
