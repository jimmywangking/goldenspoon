<template>
  <div class="org-container">
    <div class="page-header">
      <div>
        <div class="page-title">组织管理</div>
        <div class="page-desc">{{ authStore.isAdmin ? '查看全部组织' : '管理本组织' }}</div>
      </div>
      <el-button type="primary" @click="showCreateDialog">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="margin-right:6px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增组织
      </el-button>
    </div>
    <el-card shadow="hover" class="table-card">
      <el-table :data="orgs" v-loading="loading" stripe class="data-table">
        <el-table-column prop="name" label="组织名称" min-width="160" />
        <el-table-column prop="contactName" label="联系人" min-width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.isActive ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="page.current"
        @current-change="fetchOrgs"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑组织' : '新增组织'" width="440px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="组织名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orgApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { Org } from '@/types'

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const orgs = ref<Org[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const page = reactive({ current: 1 })

const form = reactive({ name: '', contactName: '', contactPhone: '' })
const rules = { name: [{ required: true, message: '请输入组织名称', trigger: 'blur' }] }

async function fetchOrgs() {
  loading.value = true
  try {
    const res = await orgApi.list({ current: page.current, size: 10 })
    orgs.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { name: '', contactName: '', contactPhone: '' })
  dialogVisible.value = true
}

function showEditDialog(row: Org) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, { name: row.name, contactName: row.contactName || '', contactPhone: row.contactPhone || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (isEdit.value && editId.value) {
      await orgApi.update(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await orgApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchOrgs()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Org) {
  try {
    await ElMessageBox.confirm(`确定删除组织 "${row.name}" 吗？`, '提示', { type: 'warning' })
    await orgApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchOrgs()
  } catch {}
}

onMounted(fetchOrgs)
</script>

<style scoped>
.org-container { padding: 28px; }
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
.el-pagination { margin-top: 20px; justify-content: flex-end; }
</style>
