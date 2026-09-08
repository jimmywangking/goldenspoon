<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审批注册申请</span>
          <el-button @click="fetchPending" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-empty v-if="!loading && pending.length === 0" description="暂无待审批的申请" />
      <el-table v-else :data="pending" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column label="所属组织">
          <template #default="{ row }">
            {{ row.orgName || '个人用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">
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
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '审批失败')
    }
  }
}

async function handleReject(row: any) {
  try {
    await ElMessageBox.confirm(`确定拒绝用户 "${row.username}" 的注册申请吗？`, '确认审批', { type: 'warning' })
    await registerApi.reject(row.id)
    ElMessage.success('已拒绝')
    fetchPending()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }
}

onMounted(fetchPending)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
