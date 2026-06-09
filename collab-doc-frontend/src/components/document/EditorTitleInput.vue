<template>
  <div class="title-input-wrapper">
    <input
      ref="inputRef"
      class="title-input"
      :value="modelValue"
      placeholder="Untitled"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      @blur="handleBlur"
      @keydown.enter="handleBlur"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  save: []
}>()

const inputRef = ref<HTMLInputElement>()

function handleBlur() {
  emit('save')
}
</script>

<style scoped>
.title-input-wrapper {
  padding: var(--space-2xl) 0 var(--space-xs);
}
.title-input {
  width: 100%;
  border: none;
  outline: none;
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 700;
  color: var(--color-text);
  background: transparent;
  padding: 0;
  line-height: 1.15;
  letter-spacing: -0.03em;
  border-bottom: 2px solid transparent;
  transition: border-color 0.3s var(--ease-out);
}
.title-input:focus {
  border-bottom-color: var(--color-primary);
}
.title-input::placeholder {
  color: var(--color-text-muted);
  transition: color 0.2s;
}
.title-input:focus::placeholder {
  color: var(--color-border);
}
</style>
