import api from './index'
import type { ApiResponse } from '@/types/common'
import type { DocumentIndex } from '@/types/search'
import type { DocumentSnapshot } from '@/types/search'

export function searchDocuments(workspaceId: number, keyword: string) {
  return api.get<ApiResponse<DocumentIndex[]>>('/search', {
    params: { workspaceId, keyword },
  })
}

export function getVersions(docId: number) {
  return api.get<ApiResponse<DocumentSnapshot[]>>(`/documents/${docId}/versions`)
}

export function getVersion(docId: number, version: number) {
  return api.get<ApiResponse<DocumentSnapshot>>(`/documents/${docId}/versions/${version}`)
}

export function saveVersion(docId: number, content: string) {
  return api.post<ApiResponse<void>>(`/documents/${docId}/versions`, { content })
}
