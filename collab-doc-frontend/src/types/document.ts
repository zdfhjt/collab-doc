export type DocumentType = 'FOLDER' | 'DOCUMENT'

export interface DocumentTreeNode {
  id: number
  title: string
  docType: DocumentType
  icon?: string
  parentId: number | null
  sortOrder: number
  children: DocumentTreeNode[]
  isExpanded?: boolean
}

export interface Document {
  id: number
  workspaceId: number
  parentId: number | null
  title: string
  docType: DocumentType
  content: string | null
  icon?: string
  sortOrder: number
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface DocumentCreateRequest {
  title: string
  docType?: DocumentType
  parentId?: number | null
  icon?: string
  sortOrder?: number
}

export interface DocumentUpdateRequest {
  title?: string
  content?: string
  icon?: string
  sortOrder?: number
}
