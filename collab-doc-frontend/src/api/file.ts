import api from './index'
import type { ApiResponse } from '@/types/common'

interface UploadResult {
  objectKey: string
  url: string
}

export function uploadFile(file: File, workspaceId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('workspaceId', workspaceId.toString())
  return api.post<ApiResponse<UploadResult>>('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function importExternalImage(imageUrl: string, workspaceId: number) {
  return api.post<ApiResponse<string>>(
    `/files/import-external?workspaceId=${workspaceId}`,
    { url: imageUrl }
  )
}
