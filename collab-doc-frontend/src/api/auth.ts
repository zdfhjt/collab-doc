import api from './index'
import type { ApiResponse } from '@/types/common'
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth'

export function login(data: LoginRequest) {
  return api.post<ApiResponse<AuthResponse>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return api.post<ApiResponse<AuthResponse>>('/auth/register', data)
}

export function updateProfile(data: { displayName?: string }) {
  return api.put<ApiResponse<void>>('/auth/profile', data)
}

export function changePassword(data: { currentPassword: string; newPassword: string }) {
  return api.put<ApiResponse<void>>('/auth/password', data)
}

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<ApiResponse<string>>('/auth/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
