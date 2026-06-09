import { ref, watch, type Ref } from 'vue'
import { debounce } from '@/utils/debounce'

export function useAutoSave(content: Ref<string | null>, saveFn: (content: string) => Promise<void>, delay = 1000) {
  const isSaving = ref(false)
  const lastSavedAt = ref<Date | null>(null)
  const error = ref<string | null>(null)

  const debouncedSave = debounce(async (value: string) => {
    isSaving.value = true
    error.value = null
    try {
      await saveFn(value)
      lastSavedAt.value = new Date()
    } catch (e: any) {
      error.value = e.message || 'Save failed'
    } finally {
      isSaving.value = false
    }
  }, delay)

  watch(content, (newVal) => {
    if (newVal) {
      debouncedSave(newVal)
    }
  })

  return { isSaving, lastSavedAt, error }
}
