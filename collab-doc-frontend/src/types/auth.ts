export interface User {
  id: number
  username: string
  email: string
  displayName?: string
  avatarUrl?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  userId: number
  username: string
  email: string
  displayName?: string
  avatarUrl?: string
}
