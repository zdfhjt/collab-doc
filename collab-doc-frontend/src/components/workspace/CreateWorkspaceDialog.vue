<template>
  <el-dialog v-model="visible" title="Create Workspace" width="420px" @close="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="Name" prop="name">
        <el-input v-model="form.name" placeholder="My Workspace" />
      </el-form-item>
      <el-form-item label="Description" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Optional description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">Create</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useWorkspaceStore } from '@/stores/workspace'

const workspaceStore = useWorkspaceStore()
const visible = ref(false)
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ name: '', description: '' })
const rules = {
  name: [{ required: true, message: 'Please enter workspace name', trigger: 'blur' }],
}

function open() {
  visible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await workspaceStore.createWorkspace({ name: form.name, description: form.description })
    ElMessage.success('Workspace created')
    visible.value = false
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Failed to create workspace')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
