export interface Workspace {
  id: number
  name: string
  description: string | null
  ownerId: number
  ownerName: string
  memberCount: number
  createdAt: string
  updatedAt: string
}

export interface WorkspaceMember {
  id: number
  workspaceId: number
  userId: number
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  joinedAt: string
}

export interface WorkspaceCreateRequest {
  name: string
  description?: string
}

export interface InviteMemberRequest {
  usernameOrEmail: string
}
