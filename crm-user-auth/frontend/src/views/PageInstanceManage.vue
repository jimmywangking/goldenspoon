<template>
  <div class="instance-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的页面实例</span>
          <div class="header-actions">
            <el-select v-model="filterPageCode" placeholder="筛选类型" clearable style="width:120px" @change="loadList">
              <el-option label="PAGE_1" value="PAGE_1" />
              <el-option label="PAGE_2" value="PAGE_2" />
            </el-select>
            <el-button type="primary" icon="Plus" @click="openDialog(null)">新建实例</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="list.length === 0" description="暂无页面实例，点击新建实例" />

      <el-row :gutter="16" v-else>
        <el-col :xs="24" :sm="12" :md="8" v-for="item in list" :key="item.id">
          <el-card class="instance-card" shadow="hover">
            <template #header>
              <div class="card-title">
                <span class="title-text">{{ item.title || '未命名实例' }}</span>
                <el-tag size="small" type="info">{{ item.pageCode }}</el-tag>
              </div>
              <div class="card-meta">{{ formatTime(item.updatedAt) }}</div>
            </template>
            <div class="card-content">
              {{ truncate(item.content, 80) }}
            </div>
            <div class="card-actions">
              <el-button size="small" @click="openDialog(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(item)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-pagination
        v-if="total > pageSize"
        style="margin-top:16px;justify-content:center"
        layout="prev, pager, next"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑实例' : '新建实例'" width="520px" destroy-on-close>
      <el-form label-width="70px">
        <el-form-item label="页面类型">
          <el-select v-model="form.pageCode" :disabled="!!editingId" style="width:100%">
            <el-option label="PAGE_1" value="PAGE_1" />
            <el-option label="PAGE_2" value="PAGE_2" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="可选，便于区分不同实例" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder='{"key":"value"}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { instanceApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const filterPageCode = ref<string | undefined>()

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = ref({ pageCode: 'PAGE_1', title: '', content: '' })

async function loadList() {
  const res: any = await instanceApi.list({
    pageCode: filterPageCode.value,
    current: page.value,
    size: pageSize
  })
  list.value = res.data.records || []
  total.value = res.data.total || 0
}

function handlePageChange(p: number) {
  page.value = p
  loadList()
}

function openDialog(item: any | null) {
  if (item) {
    editingId.value = item.id
    form.value = { pageCode: item.pageCode, title: item.title || '', content: item.content || '' }
  } else {
    editingId.value = null
    form.value = { pageCode: filterPageCode.value || 'PAGE_1', title: '', content: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.content.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await instanceApi.update(editingId.value, { title: form.value.title, content: form.value.content })
      ElMessage.success('更新成功')
    } else {
      await instanceApi.create({ pageCode: form.value.pageCode, title: form.value.title || undefined, content: form.value.content })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

async function handleDelete(item: any) {
  await ElMessageBox.confirm(`确定删除实例「${item.title || '未命名'}」吗？`, '确认删除', { type: 'warning' })
  await instanceApi.delete(item.id)
  ElMessage.success('已删除')
  loadList()
}

function truncate(s: string, n: number) {
  return s.length > n ? s.slice(0, n) + '…' : s
}

function formatTime(ts: string) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(loadList)
</script>

<style scoped>
.instance-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.instance-card { margin-bottom: 16px; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 500; }
.card-meta { font-size: 12px; color: #909399; margin-top: 4px; }
.card-content { color: #606266; line-height: 1.6; min-height: 40px; white-space: pre-wrap; word-break: break-all; }
.card-actions { margin-top: 12px; display: flex; justify-content: flex-end; gap: 8px; }
</style>
