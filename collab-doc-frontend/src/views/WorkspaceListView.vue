<template>
  <div class="workspace-page">
    <header class="workspace-header">
      <div class="header-left">
        <div class="brand-mark">
          <div class="brand-logo">C</div>
          <div>
            <h1 class="page-title">Workspaces</h1>
            <p class="page-desc">Your collaborative spaces</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <span class="user-avatar">{{ userInitial }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="dropdown-user-info">
                <span class="dropdown-name">{{ authStore.displayName }}</span>
                <span class="dropdown-email">{{ authStore.user?.email }}</span>
              </div>
              <el-dropdown-item divided @click="openProfile">
                <el-icon><User /></el-icon> Edit Profile
              </el-dropdown-item>
              <el-dropdown-item @click="openPassword">
                <el-icon><Lock /></el-icon> Change Password
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon> Sign out
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" size="large" @click="createDialog?.open()">
          <el-icon><Plus /></el-icon>
          New Workspace
        </el-button>
      </div>
    </header>

    <div v-loading="loading" class="workspace-grid">
      <WorkspaceCard
        v-for="workspace in workspaceStore.workspaces"
        :key="workspace.id"
        :workspace="workspace"
        @click="openWorkspace(workspace.id)"
      />
      <div v-if="!loading && workspaceStore.workspaces.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon :size="40"><Folder /></el-icon>
        </div>
        <h3>No workspaces yet</h3>
        <p>Create your first workspace to get started</p>
      </div>
    </div>

    <CreateWorkspaceDialog ref="createDialog" />

    <!-- Profile Dialog -->
    <el-dialog v-model="profileDialogVisible" title="Edit Profile" width="420px">
      <div class="avatar-section">
        <div class="current-avatar" @click="avatarInput?.click()">
          <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" class="avatar-img" />
          <span v-else class="avatar-text">{{ userInitial }}</span>
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
          </div>
        </div>
        <input ref="avatarInput" type="file" accept="image/*" style="display: none" @change="handleAvatarUpload" />
        <p class="avatar-hint">Click to change avatar</p>
      </div>
      <el-form label-position="top">
        <el-form-item label="Username">
          <el-input :value="authStore.user?.username" disabled />
        </el-form-item>
        <el-form-item label="Email">
          <el-input :value="authStore.user?.email" disabled />
        </el-form-item>
        <el-form-item label="Display Name">
          <el-input v-model="profileForm.displayName" placeholder="Your display name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveProfile">Save</el-button>
      </template>
    </el-dialog>

    <!-- Change Password Dialog -->
    <el-dialog v-model="passwordDialogVisible" title="Change Password" width="420px">
      <el-form label-position="top" :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="Current Password" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="Enter current password" />
        </el-form-item>
        <el-form-item label="New Password" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="At least 6 characters" />
        </el-form-item>
        <el-form-item label="Confirm New Password" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="Confirm new password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="savePassword">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useWorkspaceStore } from '@/stores/workspace'
import { useAuthStore } from '@/stores/auth'
import { updateProfile, uploadAvatar, changePassword } from '@/api/auth'
import WorkspaceCard from '@/components/workspace/WorkspaceCard.vue'
import CreateWorkspaceDialog from '@/components/workspace/CreateWorkspaceDialog.vue'

const router = useRouter()
const workspaceStore = useWorkspaceStore()
const authStore = useAuthStore()
const loading = ref(false)
const createDialog = ref<InstanceType<typeof CreateWorkspaceDialog>>()
const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const profileForm = reactive({ displayName: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const passwordFormRef = ref<FormInstance>()
const avatarInput = ref<HTMLInputElement>()

const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: 'Please enter current password', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm new password', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const userInitial = computed(() => authStore.displayName.charAt(0).toUpperCase())

onMounted(async () => {
  loading.value = true
  try {
    await workspaceStore.fetchWorkspaces()
  } finally {
    loading.value = false
  }
})

function openWorkspace(id: number) {
  router.push(`/workspace/${id}`)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function openProfile() {
  profileForm.displayName = authStore.user?.displayName || ''
  profileDialogVisible.value = true
}

function openPassword() {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

async function handleAvatarUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const { data: response } = await uploadAvatar(file)
    if (authStore.user) {
      authStore.user.avatarUrl = response.data
      authStore.saveToStorage()
    }
    ElMessage.success('Avatar updated')
  } catch {
    ElMessage.error('Failed to upload avatar')
  }
}

async function saveProfile() {
  try {
    await updateProfile({ displayName: profileForm.displayName })
    if (authStore.user) {
      authStore.user.displayName = profileForm.displayName
      authStore.saveToStorage()
    }
    ElMessage.success('Profile updated')
    profileDialogVisible.value = false
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to update')
  }
}

async function savePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('Password changed')
    passwordDialogVisible.value = false
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to change password')
  }
}
</script>

<style scoped>
.workspace-page {
  max-width: 1040px;
  margin: 0 auto;
  padding: 56px 32px 80px;
}
/* Subtle page-level gradient background */
.workspace-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 320px;
  background: linear-gradient(180deg, rgba(196, 98, 45, 0.04) 0%, transparent 100%);
  pointer-events: none;
  z-index: 0;
}
.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48px;
  position: relative;
  z-index: 1;
  animation: headerSlideIn 0.5s cubic-bezier(0.32, 0.72, 0, 1) both;
}
@keyframes headerSlideIn {
  from { opacity: 0; transform: translateY(-12px); }
  to { opacity: 1; transform: translateY(0); }
}
.brand-mark {
  display: flex;
  align-items: center;
  gap: 16px;
}
.brand-logo {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--color-primary), #e07a3a);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  transition: transform 0.3s var(--ease-out);
  box-shadow: 0 4px 16px rgba(196, 98, 45, 0.2);
}
.brand-logo:hover {
  transform: rotate(-5deg) scale(1.05);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s var(--ease-out);
}
.user-avatar:hover {
  transform: scale(1.08);
  box-shadow: 0 0 0 3px rgba(196, 98, 45, 0.15);
}
.page-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.03em;
  line-height: 1;
}
.page-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  letter-spacing: 0.01em;
}
.workspace-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
  position: relative;
  z-index: 1;
}
/* Staggered entry animation for cards */
.workspace-grid > :deep(.workspace-card):nth-child(1) { animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.05s both; }
.workspace-grid > :deep(.workspace-card):nth-child(2) { animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.1s both; }
.workspace-grid > :deep(.workspace-card):nth-child(3) { animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.15s both; }
.workspace-grid > :deep(.workspace-card):nth-child(4) { animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.2s both; }
.workspace-grid > :deep(.workspace-card):nth-child(5) { animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.25s both; }
.workspace-grid > :deep(.workspace-card):nth-child(6) { animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.3s both; }
@keyframes cardReveal {
  from { opacity: 0; transform: translateY(16px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
@media (max-width: 640px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }
  .workspace-page {
    padding: 32px 16px 60px;
  }
  .page-title {
    font-size: 26px;
  }
}
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-secondary);
  grid-column: 1 / -1;
  animation: cardReveal 0.5s cubic-bezier(0.32, 0.72, 0, 1) 0.1s both;
}
.empty-icon {
  width: 80px;
  height: 80px;
  background: var(--color-bg-secondary);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: var(--color-text-muted);
  transition: transform 0.4s var(--ease-out);
}
.empty-state:hover .empty-icon {
  transform: scale(1.05) rotate(-3deg);
}
.empty-state h3 {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}
.empty-state p {
  font-size: 14px;
}
.dropdown-user-info {
  padding: 8px 16px;
}
.dropdown-name {
  display: block;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
}
.dropdown-email {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}
.current-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.2s var(--ease-out);
}
.current-avatar:hover {
  transform: scale(1.05);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
}
.current-avatar:hover .avatar-overlay {
  opacity: 1;
}
.avatar-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 8px;
}
</style>
