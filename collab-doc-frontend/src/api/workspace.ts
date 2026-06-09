import api from './index'
import type { ApiResponse } from '@/types/common'
import type { Workspace, WorkspaceCreateRequest, InviteMemberRequest, WorkspaceMember } from '@/types/workspace'

export function getMyWorkspaces() {
  return api.get<ApiResponse<Workspace[]>>('/workspaces')
}

export function getWorkspace(id: number) {
  return api.get<ApiResponse<Workspace>>(`/workspaces/${id}`)
}

export function createWorkspace(data: WorkspaceCreateRequest) {
  return api.post<ApiResponse<Workspace>>('/workspaces', data)
}

export function inviteMember(workspaceId: number, data: InviteMemberRequest) {
  return api.post<ApiResponse<void>>(`/workspaces/${workspaceId}/members`, data)
}

export function removeMember(workspaceId: number, userId: number) {
  return api.delete<ApiResponse<void>>(`/workspaces/${workspaceId}/members/${userId}`)
}

export function getMembers(workspaceId: number) {
  return api.get<ApiResponse<WorkspaceMember[]>>(`/workspaces/${workspaceId}/members`)
}
