import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import type { LoginRequest, RegisterRequest, User } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const displayName = computed(() => user.value?.displayName || user.value?.username || 'User')

  function loadFromStorage() {
    const stored = localStorage.getItem('auth')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        token.value = data.token
        user.value = data.user
      } catch {
        localStorage.removeItem('auth')
      }
    }
  }

  function saveToStorage() {
    localStorage.setItem('auth', JSON.stringify({ token: token.value, user: user.value }))
  }

  async function login(credentials: LoginRequest) {
    const { data: response } = await loginApi(credentials)
    token.value = response.data.token
    user.value = {
      id: response.data.userId,
      username: response.data.username,
      email: response.data.email,
      displayName: response.data.displayName,
      avatarUrl: response.data.avatarUrl,
    }
    saveToStorage()
  }

  async function register(credentials: RegisterRequest) {
    const { data: response } = await registerApi(credentials)
    token.value = response.data.token
    user.value = {
      id: response.data.userId,
      username: response.data.username,
      email: response.data.email,
      displayName: response.data.displayName,
      avatarUrl: response.data.avatarUrl,
    }
    saveToStorage()
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('auth')
  }

  return { token, user, isAuthenticated, displayName, loadFromStorage, saveToStorage, login, register, logout }
})
