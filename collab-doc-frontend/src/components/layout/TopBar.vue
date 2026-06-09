<template>
  <div class="topbar">
    <div class="topbar-left">
      <slot name="left" />
    </div>
    <div class="topbar-center">
      <slot name="center" />
    </div>
    <div class="topbar-right">
      <el-dropdown trigger="click">
        <span class="user-avatar">
          <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" class="avatar-img" />
          <span v-else>{{ userInitial }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <div class="dropdown-user">
              <span class="dropdown-name">{{ authStore.displayName }}</span>
            </div>
            <el-dropdown-item divided @click="handleLogout">Sign out</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const userInitial = computed(() => {
  return authStore.displayName.charAt(0).toUpperCase()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--topbar-height);
  padding: 0 20px;
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-xs);
  background: var(--color-bg);
  flex-shrink: 0;
}
.topbar-left, .topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.topbar-center {
  flex: 1;
  text-align: center;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s var(--ease-out);
}
.user-avatar:hover {
  transform: scale(1.05);
}
.user-avatar:active {
  transform: scale(0.95);
  transition-duration: 0.05s;
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}
.dropdown-user {
  padding: 8px 16px;
}
.dropdown-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
}
</style>
