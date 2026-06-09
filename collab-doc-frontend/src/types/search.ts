export interface DocumentIndex {
  id: number
  title: string
  content: string
  workspaceId: number
  docType: string
  updatedAt: string
}

export interface DocumentSnapshot {
  id: number
  docId: number
  version: number
  yjsState: string | null
  content: string | null
  createdAt: string
}
