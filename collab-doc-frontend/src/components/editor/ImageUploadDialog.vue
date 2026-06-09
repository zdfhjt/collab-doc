<template>
  <el-dialog v-model="visible" title="Insert Image" width="420px">
    <el-upload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :limit="1"
      accept="image/*"
      :on-change="handleFileChange"
    >
      <el-icon :size="40"><UploadFilled /></el-icon>
      <div>Drop image here or <em>click to browse</em></div>
    </el-upload>
    <template #footer>
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" :loading="loading" :disabled="!selectedFile" @click="handleUpload">
        Insert
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { uploadFile } from '@/api/file'
import type { Editor } from '@tiptap/vue-3'

const props = defineProps<{
  modelValue: boolean
  editor: Editor
  workspaceId: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const loading = ref(false)
const selectedFile = ref<File | null>(null)

function handleFileChange(uploadFile: UploadFile) {
  selectedFile.value = uploadFile.raw || null
}

async function handleUpload() {
  if (!selectedFile.value) return
  loading.value = true
  try {
    const { data: response } = await uploadFile(selectedFile.value, props.workspaceId)
    props.editor.chain().focus().setImage({ src: response.data.url }).run()
    visible.value = false
    selectedFile.value = null
  } catch (err: any) {
    ElMessage.error('Failed to upload image')
  } finally {
    loading.value = false
  }
}
</script>
