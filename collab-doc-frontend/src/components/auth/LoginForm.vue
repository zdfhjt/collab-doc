<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="Username" prop="username">
      <el-input v-model="form.username" placeholder="Enter your username" size="large" />
    </el-form-item>
    <el-form-item label="Password" prop="password">
      <el-input v-model="form.password" type="password" placeholder="Enter your password" size="large" show-password />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" :loading="loading" style="width: 100%" native-type="submit">
        Sign in
      </el-button>
    </el-form-item>
    <div class="auth-link">
      Don't have an account? <router-link to="/register">Sign up</router-link>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login({ username: form.username, password: form.password })
    ElMessage.success('Welcome back!')
    router.push('/workspaces')
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Login failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
.auth-link a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.auth-link a:hover {
  color: var(--color-primary-hover);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--color-text);
  font-size: 13px;
  padding-bottom: 6px !important;
}
</style>
