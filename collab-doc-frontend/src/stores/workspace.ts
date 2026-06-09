import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as workspaceApi from '@/api/workspace'
import type { Workspace, WorkspaceMember, WorkspaceCreateRequest } from '@/types/workspace'

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([])
  const currentWorkspace = ref<Workspace | null>(null)
  const members = ref<WorkspaceMember[]>([])

  async function fetchWorkspaces() {
    const { data: response } = await workspaceApi.getMyWorkspaces()
    workspaces.value = response.data
  }

  async function selectWorkspace(id: number) {
    const { data: response } = await workspaceApi.getWorkspace(id)
    currentWorkspace.value = response.data
  }

  async function createWorkspace(request: WorkspaceCreateRequest) {
    const { data: response } = await workspaceApi.createWorkspace(request)
    workspaces.value.unshift(response.data)
    return response.data
  }

  async function inviteMember(workspaceId: number, usernameOrEmail: string) {
    await workspaceApi.inviteMember(workspaceId, { usernameOrEmail })
  }

  async function fetchMembers(workspaceId: number) {
    const { data: response } = await workspaceApi.getMembers(workspaceId)
    members.value = response.data
  }

  return {
    workspaces, currentWorkspace, members,
    fetchWorkspaces, selectWorkspace, createWorkspace, inviteMember, fetchMembers,
  }
})
