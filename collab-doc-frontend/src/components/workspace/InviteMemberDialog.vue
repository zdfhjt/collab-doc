<template>
  <el-dialog v-model="visible" title="Invite Member" width="400px" @close="form.usernameOrEmail = ''">
    <el-form :model="form" @submit.prevent="handleSubmit">
      <el-form-item label="Username or Email">
        <el-input v-model="form.usernameOrEmail" placeholder="Enter username or email" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">Invite</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useWorkspaceStore } from '@/stores/workspace'

const props = defineProps<{ workspaceId: number }>()
const workspaceStore = useWorkspaceStore()
const visible = ref(false)
const loading = ref(false)
const form = reactive({ usernameOrEmail: '' })

function open() {
  visible.value = true
}

async function handleSubmit() {
  if (!form.usernameOrEmail.trim()) return
  loading.value = true
  try {
    await workspaceStore.inviteMember(props.workspaceId, form.usernameOrEmail.trim())
    ElMessage.success('Member invited')
    visible.value = false
    form.usernameOrEmail = ''
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to invite member')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
