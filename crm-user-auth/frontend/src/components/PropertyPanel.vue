<template>
  <div class="property-panel" v-if="selectedModule">
    <div class="panel-title">属性编辑</div>

    <div class="field-group">
      <label>模块名称</label>
      <el-input v-model="selectedModule.name" size="small" @input="updateModule" />
    </div>

    <div class="field-group">
      <label>颜色</label>
      <div class="color-picker">
        <input type="color" v-model="selectedModule.color" @input="updateModule" />
        <span>{{ selectedModule.color }}</span>
      </div>
    </div>

    <div class="section-title">位置</div>
    <div class="field-row">
      <div class="field-group">
        <label>X</label>
        <el-input-number v-model="selectedModule.position.x" :step="0.5" size="small" @change="updateModule" />
      </div>
      <div class="field-group">
        <label>Y</label>
        <el-input-number v-model="selectedModule.position.y" :step="0.5" size="small" @change="updateModule" />
      </div>
      <div class="field-group">
        <label>Z</label>
        <el-input-number v-model="selectedModule.position.z" :step="0.5" size="small" @change="updateModule" />
      </div>
    </div>

    <div class="section-title">旋转（度）</div>
    <div class="field-row">
      <div class="field-group">
        <label>RX</label>
        <el-input-number v-model="selectedModule.rotation.x" :step="15" :min="-180" :max="180" size="small" @change="updateModule" />
      </div>
      <div class="field-group">
        <label for="RY">RY</label>
        <el-input-number id="RY" v-model="selectedModule.rotation.y" :step="15" :min="-180" :max="180" size="small" @change="updateModule" />
      </div>
      <div class="field-group">
        <label>RZ</label>
        <el-input-number v-model="selectedModule.rotation.z" :step="15" :min="-180" :max="180" size="small" @change="updateModule" />
      </div>
    </div>

    <div class="section-title">缩放</div>
    <div class="field-row">
      <div class="field-group">
        <label>SX</label>
        <el-input-number v-model="selectedModule.scale.x" :min="0.1" :max="5" :step="0.1" size="small" @change="updateModule" />
      </div>
      <div class="field-group">
        <label>SY</label>
        <el-input-number v-model="selectedModule.scale.y" :min="0.1" :max="5" :step="0.1" size="small" @change="updateModule" />
      </div>
      <div class="field-group">
        <label>SZ</label>
        <el-input-number v-model="selectedModule.scale.z" :min="0.1" :max="5" :step="0.1" size="small" @change="updateModule" />
      </div>
    </div>

    <div class="actions" v-if="!readonly">
      <el-button type="danger" size="small" @click="$emit('delete', selectedModule.id)">删除模块</el-button>
      <el-button type="warning" size="small" @click="$emit('duplicate', selectedModule.id)">复制模块</el-button>
    </div>
  </div>

  <div class="empty-tip" v-else>
    <p>点击 3D 场景中的模块以编辑属性</p>
    <p>或从左侧模块库添加新模块</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ModuleConfig } from '@/types'

const props = defineProps<{
  modules: ModuleConfig[]
  selectedId: string
  readonly: boolean
}>()

const emit = defineEmits<{
  (e: 'update', id: string, data: Partial<ModuleConfig>): void
  (e: 'delete', id: string): void
  (e: 'duplicate', id: string): void
}>()

const selectedModule = computed(() =>
  props.modules.find(m => m.id === props.selectedId)
)

function updateModule() {
  if (!selectedModule.value) return
  emit('update', selectedModule.value.id, {
    name: selectedModule.value.name,
    color: selectedModule.value.color,
    position: { ...selectedModule.value.position },
    rotation: { ...selectedModule.value.rotation },
    scale: { ...selectedModule.value.scale }
  })
}
</script>

<style scoped>
.property-panel {
  padding: 16px;
  border-left: 1px solid #e4e7ed;
  height: 100%;
  overflow-y: auto;
}

.panel-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-group label {
  font-size: 12px;
  color: #606266;
}

.field-group :deep(.el-input-number) {
  width: 100%;
}

.field-row {
  display: flex;
  gap: 8px;
}

.field-row .field-group {
  flex: 1;
}

.color-picker {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-picker input[type="color"] {
  width: 40px;
  height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}

.section-title {
  font-size: 12px;
  font-weight: bold;
  color: #909399;
  margin: 16px 0 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 20px;
}

.empty-tip {
  padding: 40px 16px;
  text-align: center;
  color: #909399;
}

.empty-tip p {
  margin: 8px 0;
  font-size: 13px;
}
</style>
