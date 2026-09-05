<template>
  <div class="page1-container">
    <Toolbar
      :modules="modules"
      :readonly="!canEdit"
      :is-admin="authStore.isAdmin"
      :is-org-admin="authStore.isOrgAdmin"
      @save="handleSave"
      @load="handleLoad"
      @view-all="adminDialogVisible = true"
    />

    <div class="workspace">
      <ModulePanel
        :templates="templates"
        :readonly="!canEdit"
        @add="addModule"
      />

      <div class="scene-area">
        <ThreeScene
          :modules="modules"
          :selected-id="selectedId"
          :readonly="!canEdit"
          @select="selectedId = $event"
        />
        <div class="scene-hint" v-if="canEdit && modules.length === 0">
          从左侧模块库添加模块开始设计
        </div>
      </div>

      <PropertyPanel
        :modules="modules"
        :selected-id="selectedId"
        :readonly="!canEdit"
        @update="updateModule"
        @delete="deleteModule"
        @duplicate="duplicateModule"
      />
    </div>

    <!-- 管理面板：查看所有用户的设计 -->
    <el-dialog v-model="adminDialogVisible" title="所有用户设计" width="90%" top="5vh">
      <div v-loading="adminLoading">
        <el-table :data="allDesigns" stripe max-height="600">
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column label="内容预览" min-width="200">
            <template #default="{ row }">
              <el-tag v-if="isJsonArray(row.content)" type="success" size="small">
                {{ tryParseModules(row.content)?.length || 0 }} 个模块
              </el-tag>
              <span v-else class="text-preview">{{ row.content?.substring(0, 40) || '(空)' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button
                v-if="isJsonArray(row.content)"
                type="primary"
                link
                size="small"
                @click="loadUserDesign(row)"
              >
                加载到编辑器
              </el-button>
              <el-button
                v-if="isJsonArray(row.content)"
                type="info"
                link
                size="small"
                @click="previewDesign(row.content)"
              >
                预览
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="设计预览" width="70%" top="5vh">
      <ThreeScene
        :modules="previewModules"
        :selected-id="''"
        readonly
        style="height: 500px; border: 1px solid #e4e7ed; border-radius: 4px;"
      />
      <div class="preview-info" v-if="previewModules.length">
        <p>共 {{ previewModules.length }} 个模块</p>
        <p v-for="m in previewModules" :key="m.id">{{ m.name }}: 位置({{ m.position.x }},{{ m.position.y }},{{ m.position.z }}) 缩放({{ m.scale.x }},{{ m.scale.y }},{{ m.scale.z }})</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import ThreeScene from '@/components/ThreeScene.vue'
import ModulePanel from '@/components/ModulePanel.vue'
import PropertyPanel from '@/components/PropertyPanel.vue'
import Toolbar from '@/components/Toolbar.vue'
import { MODULE_TEMPLATES, type ModuleConfig } from '@/types'
import * as pageApi from '@/api/pages'

const authStore = useAuthStore()

// 权限判断
const canEdit = computed(() => {
  if (authStore.isAdmin) return true
  if (authStore.isOrgAdmin) return true
  const perm = authStore.userInfo?.permissions?.find((p: any) => p.pageCode === 'PAGE_1')
  return !!perm?.canEdit
})

// 模块数据
const modules = ref<ModuleConfig[]>([])
const selectedId = ref('')
const templates = MODULE_TEMPLATES

// 管理员面板
const adminDialogVisible = ref(false)
const adminLoading = ref(false)
interface AllDesignItem {
  id: number
  userId: number
  pageCode: string
  content: string
  updatedAt: string
}
const allDesigns = ref<AllDesignItem[]>([])

// 预览
const previewVisible = ref(false)
const previewModules = ref<ModuleConfig[]>([])

// 生成唯一ID
function genId() {
  return 'mod_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5)
}

// 辅助：判断内容是否为 JSON 数组（模块数据）
function isJsonArray(content: string): boolean {
  if (!content) return false
  try {
    const parsed = JSON.parse(content)
    return Array.isArray(parsed)
  } catch {
    return false
  }
}

// 辅助：安全解析模块
function tryParseModules(content: string): ModuleConfig[] | null {
  if (!content) return null
  try {
    const parsed = JSON.parse(content)
    return Array.isArray(parsed) ? parsed : null
  } catch {
    return null
  }
}

// 添加模块
function addModule(template: any) {
  if (!canEdit.value) return
  const id = genId()
  const newModule: ModuleConfig = {
    id,
    name: template.name,
    position: { x: 0, y: 0, z: 0 },
    rotation: { x: 0, y: 0, z: 0 },
    scale: { x: template.sizeX, y: template.sizeY, z: template.sizeZ },
    color: template.color
  }
  modules.value = [...modules.value, newModule]
  selectedId.value = id
  ElMessage.success(`已添加 ${template.name}`)
}

// 更新模块
function updateModule(id: string, data: Partial<ModuleConfig>) {
  modules.value = modules.value.map(m => m.id === id ? { ...m, ...data } : m)
}

// 删除模块
function deleteModule(id: string) {
  modules.value = modules.value.filter(m => m.id !== id)
  if (selectedId.value === id) selectedId.value = ''
}

// 复制模块
function duplicateModule(id: string) {
  const original = modules.value.find(m => m.id === id)
  if (!original) return
  const newModule: ModuleConfig = {
    ...original,
    id: genId(),
    position: { ...original.position, x: original.position.x + 1 }
  }
  modules.value = [...modules.value, newModule]
  selectedId.value = newModule.id
}

// 保存到后端 + localStorage
async function handleSave(content: string) {
  try {
    const data: ModuleConfig[] = JSON.parse(content)
    await pageApi.saveModules(data)
    modules.value = data
    pageApi.saveToLocalStorage(data)
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

// 从后端加载
async function handleLoad(content: string) {
  try {
    const data: ModuleConfig[] = JSON.parse(content)
    modules.value = data
    pageApi.saveToLocalStorage(data)
  } catch {
    ElMessage.error('JSON 格式错误')
  }
}

// 加载当前用户的设计（先读 localStorage，再读后端）
async function loadMyDesign() {
  // 先尝试从 localStorage 恢复（切换页面回来时保持状态）
  const cached = pageApi.loadFromLocalStorage()
  if (cached.length > 0) {
    modules.value = cached
  }

  try {
    const data = await pageApi.getModules()
    if (data.length > 0) {
      modules.value = data
      pageApi.saveToLocalStorage(data)
    } else if (cached.length === 0) {
      modules.value = []
    }
  } catch (e: any) {
    if (e.response?.status !== 404) {
      console.error('加载设计失败', e)
    }
  }
}

// 加载所有用户设计（管理员和组织管理员）
async function loadAllDesigns() {
  adminLoading.value = true
  try {
    const res = await pageApi.loadAllDesigns()
    allDesigns.value = (res || []) as AllDesignItem[]
  } catch (e: any) {
    ElMessage.error('加载失败')
  } finally {
    adminLoading.value = false
  }
}

// 加载指定用户的设计到编辑器
function loadUserDesign(row: AllDesignItem) {
  const parsed = tryParseModules(row.content)
  if (parsed) {
    modules.value = parsed
    pageApi.saveToLocalStorage(parsed)
    adminDialogVisible.value = false
    ElMessage.success('已加载该用户的设计')
  } else {
    ElMessage.warning('该内容不是有效的模块数据')
  }
}

function previewDesign(content: string) {
  const parsed = tryParseModules(content)
  if (parsed) {
    previewModules.value = parsed
    previewVisible.value = true
  }
}

function formatTime(time?: string) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 管理员和组织管理员可查看所有设计
watch(() => authStore.isAdmin || authStore.isOrgAdmin, (isAdminOrOrg) => {
  if (isAdminOrOrg) {
    loadAllDesigns()
  }
}, { immediate: true })

onMounted(loadMyDesign)
</script>

<style scoped>
.page1-container {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.workspace {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.scene-area {
  flex: 1;
  position: relative;
  background: #f5f7fa;
  min-width: 0;
}

.scene-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #909399;
  font-size: 14px;
  pointer-events: none;
}

.preview-info {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.preview-info p {
  margin: 4px 0;
  color: #606266;
}

.text-preview {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 300px;
}
</style>
