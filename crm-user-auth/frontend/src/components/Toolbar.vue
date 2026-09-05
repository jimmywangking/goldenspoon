<template>
  <div class="toolbar">
    <div class="toolbar-left">
      <el-button type="primary" @click="handleSave" :loading="saving">
        <el-icon><Document /></el-icon> 保存设计
      </el-button>
      <el-button @click="$emit('viewVersions')">
        <el-icon><Clock /></el-icon> 版本历史
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出 JSON
      </el-button>
      <el-upload action="" :auto-upload="false" :show-file-list="false" accept=".json" @change="handleImport">
        <el-button>
          <el-icon><Upload /></el-icon> 导入 JSON
        </el-button>
      </el-upload>
      <el-button v-if="isAdmin || isOrgAdmin" @click="$emit('viewAll')">
        <el-icon><View /></el-icon> 查看所有设计
      </el-button>
    </div>
    <div class="toolbar-right">
      <span class="status">{{ readonly ? '只读模式' : '编辑模式' }}</span>
      <el-tag v-if="modules.length" type="info" size="small">{{ modules.length }} 个模块</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, Download, Upload, View, Clock } from '@element-plus/icons-vue'

const props = defineProps<{
  modules: any[]
  readonly: boolean
  isAdmin?: boolean
  isOrgAdmin?: boolean
}>()

const emit = defineEmits<{
  (e: 'save', content: string): void
  (e: 'load', content: string): void
  (e: 'viewAll'): void
  (e: 'viewVersions'): void
}>()

const saving = ref(false)

function handleSave() {
  saving.value = true
  const content = JSON.stringify(props.modules, null, 2)
  emit('save', content)
  saving.value = false
}

function handleExport() {
  const data = {
    version: '1.0',
    type: '3d-modular-housing',
    exportTime: new Date().toISOString(),
    modules: props.modules
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `design-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function handleImport(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const json = JSON.parse(e.target?.result as string)
      const modules = json.modules || json
      emit('load', JSON.stringify(modules))
    } catch {
      console.error('JSON 解析失败')
    }
  }
  reader.readAsText(file)
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status {
  font-size: 12px;
  color: #909399;
}
</style>
