<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>页面 1（PAGE_1）</span>
          <el-tag v-if="authStore.isAdmin" type="warning">管理员模式</el-tag>
          <el-tag v-else-if="authStore.isOrgAdmin" type="info">组织管理员</el-tag>
          <el-tag v-else type="success">{{ editMode ? '可编辑' : '仅查看' }}</el-tag>
        </div>
      </template>
      <div v-if="editMode">
        <el-input v-model="content" type="textarea" :rows="6" placeholder="在此输入内容..." />
        <el-button type="primary" style="margin-top: 12px" @click="saveContent">保存</el-button>
      </div>
      <div v-else class="view-mode">
        <p>{{ content || '暂无内容' }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const editMode = ref(false)
const content = ref('')
const loading = ref(false)

async function loadContent() {
  if (!authStore.token) return
  loading.value = true
  try {
    const res = await fetch('/api/pages/PAGE_1', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    const json = await res.json()
    if (json.code === 200) {
      content.value = json.data || ''
    }
  } finally {
    loading.value = false
  }
}

function toggleEdit() {
  const perm = authStore.userInfo?.permissions?.find(p => p.pageCode === 'PAGE_1')
  if (perm && !perm.canEdit && !authStore.isAdmin) {
    ElMessage.warning('您没有编辑权限')
    return
  }
  editMode.value = !editMode.value
}

async function saveContent() {
  if (!authStore.token) return
  try {
    const res = await fetch('/api/pages/PAGE_1', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authStore.token}`
      },
      body: JSON.stringify({ content: content.value })
    })
    const json = await res.json()
    if (json.code === 200) {
      ElMessage.success('保存成功')
      editMode.value = false
    } else {
      ElMessage.error(json.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  }
}

onMounted(loadContent)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.view-mode { cursor: pointer; }
.view-mode p { line-height: 1.8; color: #303133; padding: 8px 0; }
</style>
