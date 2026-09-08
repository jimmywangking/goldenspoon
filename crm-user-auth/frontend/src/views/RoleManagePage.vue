<template>
  <div class="role-container">
    <div class="page-header">
      <div>
        <div class="page-title">角色管理</div>
        <div class="page-desc">管理角色及其页面访问权限</div>
      </div>
      <el-button type="primary" @click="showCreateDialog">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="margin-right:6px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增角色
      </el-button>
    </div>
    <el-card shadow="hover" class="table-card">
      <el-table :data="roles" v-loading="loading" stripe class="data-table">
        <el-table-column prop="name" label="角色名称" min-width="120" />
        <el-table-column prop="code" label="角色编码" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="160" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'warning' : 'info'" size="small" effect="plain">
              {{ row.isSystem ? '系统' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="perm in row.permissions"
              :key="perm.pageCode"
              size="small"
              :type="perm.canEdit ? 'success' : 'info'"
              effect="plain"
              style="margin:2px 4px 2px 0"
            >
              {{ perm.pageCode }}{{ perm.canEdit ? '编辑' : '查看' }}
            </el-tag>
            <span v-if="!row.permissions?.length" class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)" :disabled="row.isSystem">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)" :disabled="row.isSystem">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="page.current"
        @current-change="fetchRoles"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code" v-if="!isEdit">
          <el-input v-model="form.code" :disabled="isEdit" placeholder="如: SALES_MANAGER" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

interface Role { id: number; name: string; code: string; description: string; isSystem: boolean; permissions?: { pageCode: string; canView: boolean; canEdit: boolean }[] }
interface CreateRoleRequest { name: string; code: string; description?: string }

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const roles = ref<Role[]>([])
const total = ref(0)
const page = reactive({ current: 1 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const editRoleId = ref<number | null>(null)
const formRef = ref()

const form = reactive<CreateRoleRequest>({ name: '', code: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

async function fetchRoles() {
  loading.value = true
  try {
    const res = await fetch(`/api/roles?page=${page.current}&size=20`, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    const json = await res.json()
    if (json.code === 200) { roles.value = json.data.records; total.value = json.data.total }
  } finally { loading.value = false }
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { name: '', code: '', description: '' })
  dialogVisible.value = true
}

function showEditDialog(row: Role) {
  if (row.isSystem) { ElMessage.warning('系统角色不可修改'); return }
  editRoleId.value = row.id
  isEdit.value = true
  Object.assign(form, { name: row.name, code: row.code, description: row.description })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitting.value = true
    try {
      const url = isEdit.value ? `/api/roles/${editRoleId.value}` : '/api/roles'
      const res = await fetch(url, {
        method: isEdit.value ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${authStore.token}` },
        body: JSON.stringify(form)
      })
      const json = await res.json()
      if (json.code === 200) {
        ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
        dialogVisible.value = false
        fetchRoles()
      } else { ElMessage.error(json.message || '操作失败') }
    } finally { submitting.value = false }
  })
}

async function handleDelete(row: Role) {
  if (row.isSystem) { ElMessage.warning('系统角色不可删除'); return }
  await ElMessageBox.confirm(`确定删除角色「${row.name}」？`, '提示', { type: 'warning' })
  const res = await fetch(`/api/roles/${row.id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${authStore.token}` }
  })
  const json = await res.json()
  if (json.code === 200) { ElMessage.success('删除成功'); fetchRoles() }
  else { ElMessage.error(json.message || '删除失败') }
}

onMounted(fetchRoles)
</script>

<style scoped>
.role-container { padding: 28px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #1E1B4B; margin-bottom: 4px; }
.page-desc { font-size: 13px; color: #6B7280; }
.table-card { border-radius: 16px; border: none; }
.data-table { border-radius: 12px; overflow: hidden; }
.text-muted { color: #9CA3AF; font-size: 13px; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }
</style>
