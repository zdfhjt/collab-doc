<template>
  <div class="workspace-card" @click="$emit('click')">
    <div class="card-content">
      <div class="card-icon">
        <el-icon :size="24"><Folder /></el-icon>
      </div>
      <div class="card-info">
        <h3 class="card-name">{{ workspace.name }}</h3>
        <p class="card-desc">{{ workspace.description || 'No description' }}</p>
      </div>
    </div>
    <div class="card-footer">
      <span class="card-meta">{{ workspace.memberCount }} member{{ workspace.memberCount !== 1 ? 's' : '' }}</span>
      <span class="card-arrow">&rarr;</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Workspace } from '@/types/workspace'

defineProps<{
  workspace: Workspace
}>()

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.workspace-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  position: relative;
  overflow: hidden;
}
/* Subtle top accent line */
.workspace-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), #e07a3a, var(--color-primary));
  background-size: 200% 100%;
  opacity: 0;
  transition: opacity 0.35s;
}
.workspace-card:hover {
  border-color: transparent;
  box-shadow: 0 8px 32px rgba(196, 98, 45, 0.12), 0 2px 8px rgba(196, 98, 45, 0.06);
  transform: translateY(-4px);
}
.workspace-card:hover::before {
  opacity: 1;
  animation: shimmer 2s linear infinite;
}
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
.workspace-card:active {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(196, 98, 45, 0.08);
  transition-duration: 0.08s;
}
.card-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.card-icon {
  width: 52px;
  height: 52px;
  background: var(--color-primary-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}
.workspace-card:hover .card-icon {
  background: var(--color-primary);
  color: #fff;
  transform: scale(1.05) rotate(-3deg);
  box-shadow: 0 4px 16px rgba(196, 98, 45, 0.25);
}
.card-info {
  flex: 1;
  min-width: 0;
}
.card-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
  transition: color 0.2s;
}
.workspace-card:hover .card-name {
  color: var(--color-primary);
}
.card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border-light);
}
.card-meta {
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}
.card-arrow {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--color-text-muted);
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}
.workspace-card:hover .card-arrow {
  background: var(--color-primary);
  color: #fff;
  transform: translateX(2px);
}
</style>
