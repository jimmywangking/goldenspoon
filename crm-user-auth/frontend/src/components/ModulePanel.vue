<template>
  <div class="module-panel">
    <div class="panel-title">模块库</div>
    <div class="module-list">
      <div
        v-for="template in templates"
        :key="template.type"
        class="module-item"
        @click="$emit('add', template)"
      >
        <div class="module-preview" :style="{ backgroundColor: template.color }"></div>
        <div class="module-info">
          <div class="module-name">{{ template.name }}</div>
          <div class="module-size">
            {{ template.sizeX }} × {{ template.sizeY }} × {{ template.sizeZ }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModuleTemplate } from '@/types'

defineProps<{
  templates: ModuleTemplate[]
}>()

defineEmits<{
  (e: 'add', template: ModuleTemplate): void
}>()
</script>

<style scoped>
.module-panel {
  padding: 16px;
  border-right: 1px solid #e4e7ed;
  height: 100%;
  overflow-y: auto;
}

.panel-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.module-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.module-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateX(4px);
}

.module-preview {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  flex-shrink: 0;
}

.module-info {
  flex: 1;
  min-width: 0;
}

.module-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.module-size {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}
</style>
