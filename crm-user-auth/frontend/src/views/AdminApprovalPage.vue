<template>
  <div class="approval-container">
    <div class="page-header">
      <div>
        <div class="page-title">审批管理</div>
        <div class="page-desc">管理新用户注册申请</div>
      </div>
      <el-button @click="fetchPending" :loading="loading">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="margin-right:6px"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/></svg>
        刷新
      </el-button>
    </div>
    <el-card shadow="hover" class="table-card">
      <el-empty v-if="!loading && pending.length === 0" description="暂无待审批的申请" />
      <el-table v-else :data="pending" v-loading="loading" stripe class="data-table">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column label="所属组织" min-width="100">
          <template #default="{ row }">
            <span class="text-secondary">{{ row.orgName || '个人用户' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="160">
          <template #default="{ row }">
            <span class="text-secondary">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" plain @click="handleApprove(row)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14" style="margin-right:4px"><path d="M9 11l3 3L22 4"/></svg>
              通过
            </el-button>
            <el-button size="small" type="danger" plain @click="handleReject(row)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14" style="margin-right:4px"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="page"
        @current-change="fetchPending"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registerApi } from '@/api'

const loading = ref(false)
const pending = ref<any[]>([])
const total = ref(0)
const page = ref(1)

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function fetchPending() {
  loading.value = true
  try {
    const res = await registerApi.listPending(page.value, 10)
    pending.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleApprove(row: any) {
  try {
    await ElMessageBox.confirm(`确定通过用户 "${row.username}" 的注册申请吗？`, '确认审批', { type: 'info' })
    await registerApi.approve(row.id)
    ElMessage.success('已审批通过')
    fetchPending()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.message || '审批失败')
  }
}

async function handleReject(row: any) {
  try {
    await ElMessageBox.confirm(`确定拒绝用户 "${row.username}" 的注册申请吗？`, '确认审批', { type: 'warning' })
    await registerApi.reject(row.id)
    ElMessage.success('已拒绝')
    fetchPending()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(fetchPending)
</script>

<style scoped>
.approval-container { padding: 28px; }
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title { font-size: 22px; font-weight: 700; color: #1E1B4B; margin-bottom: 4px; }
.page-desc { font-size: 13px; color: #6B7280; }
.table-card { border-radius: 16px; border: none; }
.data-table { border-radius: 12px; overflow: hidden; }
.text-secondary { color: #6B7280; font-size: 13px; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }
</style>
