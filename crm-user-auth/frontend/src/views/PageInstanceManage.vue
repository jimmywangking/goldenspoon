<template>
  <div class="instance-container">
    <div class="page-header">
      <div>
        <div class="page-title">我的页面实例</div>
        <div class="page-desc">管理您的页面实例内容</div>
      </div>
      <div class="header-actions">
        <el-select v-model="filterPageCode" placeholder="筛选类型" clearable style="width:130px" @change="loadList">
          <el-option label="PAGE_1" value="PAGE_1" />
          <el-option label="PAGE_2" value="PAGE_2" />
        </el-select>
        <el-button type="primary" @click="openDialog(null)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="margin-right:6px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新建实例
        </el-button>
      </div>
    </div>
    <el-card shadow="hover" class="table-card">
      <el-empty v-if="!loading && list.length === 0" description="暂无页面实例" />
      <el-row :gutter="16" v-else>
        <el-col :xs="24" :sm="12" :md="8" v-for="item in list" :key="item.id">
          <div class="instance-card">
            <div class="card-header">
              <div class="card-title">
                <span>{{ item.title || '未命名实例' }}</span>
                <el-tag size="small" type="info" effect="plain">{{ item.pageCode }}</el-tag>
              </div>
              <span class="card-time">{{ formatTime(item.updatedAt) }}</span>
            </div>
            <div class="card-content">{{ truncate(item.content, 100) }}</div>
            <div class="card-actions">
              <el-button size="small" @click="openDialog(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-pagination
        v-if="total > pageSize"
        layout="prev, pager, next, total"
        :total="total"
        :current-page="page"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑实例' : '新建实例'" width="540px" destroy-on-close>
      <el-form label-width="70px">
        <el-form-item label="页面类型">
          <el-select v-model="form.pageCode" :disabled="!!editingId" style="width:100%">
            <el-option label="PAGE_1 — 3D编辑器" value="PAGE_1" />
            <el-option label="PAGE_2" value="PAGE_2" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="便于区分不同实例" />
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

const loading = ref(false)
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
  loading.value = true
  try {
    const res: any = await instanceApi.list({ pageCode: filterPageCode.value, current: page.value, size: pageSize })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) { page.value = p; loadList() }

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
  if (!form.value.content.trim()) { ElMessage.warning('内容不能为空'); return }
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
  } catch {} finally { saving.value = false }
}

async function handleDelete(item: any) {
  await ElMessageBox.confirm(`确定删除实例「${item.title || '未命名'}」吗？`, '确认删除', { type: 'warning' })
  await instanceApi.delete(item.id)
  ElMessage.success('已删除')
  loadList()
}

function truncate(s: string, n: number) { return s.length > n ? s.slice(0, n) + '…' : s }
function formatTime(ts: string) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getMonth()+1}月${d.getDate()}日 ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(loadList)
</script>

<style scoped>
.instance-container { padding: 28px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #1E1B4B; margin-bottom: 4px; }
.page-desc { font-size: 13px; color: #6B7280; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.table-card { border-radius: 16px; border: none; }
.instance-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #F3F4F6;
  padding: 20px;
  margin-bottom: 16px;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.instance-card:hover {
  box-shadow: 0 4px 16px rgba(30,27,75,0.08);
  border-color: #A78BFA;
}
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; color: #1E1B4B; font-size: 14px; }
.card-time { font-size: 12px; color: #9CA3AF; white-space: nowrap; }
.card-content { color: #4B5563; line-height: 1.6; font-size: 13px; min-height: 36px; white-space: pre-wrap; word-break: break-all; background: #F9FAFB; border-radius: 6px; padding: 10px; margin-bottom: 12px; }
.card-actions { display: flex; justify-content: flex-end; gap: 8px; }
.el-pagination { margin-top: 20px; justify-content: center; }
</style>
