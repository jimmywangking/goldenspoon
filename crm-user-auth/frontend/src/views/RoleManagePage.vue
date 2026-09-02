<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="showCreateDialog">新增角色</el-button>
        </div>
      </template>
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="isSystem" label="系统角色">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'warning' : 'info'" size="small">
              {{ row.isSystem ? '系统' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="perm in row.permissions"
              :key="perm.pageCode"
              size="small"
              :type="perm.canEdit ? 'success' : 'info'"
              style="margin-right:4px"
            >
              {{ perm.pageCode }}{{ perm.canEdit ? '编辑' : '查看' }}
            </el-tag>
            <span v-if="!row.permissions || row.permissions.length === 0" style="color:#909399">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row)"
              :disabled="row.isSystem"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        @current-change="fetchRoles"
        @size-change="fetchRoles"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code" v-if="!isEdit">
          <el-input v-model="form.code" placeholder="如: SALES_MANAGER" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

interface Role {
  id: number
  name: string
  code: string
  description: string
  isSystem: boolean
  permissions?: RolePerm[]
}

interface RolePerm {
  pageCode: string
  canView: boolean
  canEdit: boolean
}

interface CreateRoleRequest {
  name: string
  code: string
  description?: string
}

const authStore = useAuthStore()
const loading = ref(false)
const roles = ref<Role[]>([])
const total = ref(0)
const page = reactive({ current: 1, size: 20 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive<CreateRoleRequest>({ name: '', code: '', description: '' })

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

async function fetchRoles() {
  loading.value = true
  try {
    const res = await fetch(`/api/roles?page=${page.current}&size=${page.size}`, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    const json = await res.json()
    if (json.code === 200) {
      roles.value = json.data.records
      total.value = json.data.total
    }
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { name: '', code: '', description: '' })
  dialogVisible.value = true
}

function showEditDialog(row: Role) {
  if (row.isSystem) {
    ElMessage.warning('系统角色不可修改')
    return
  }
  editRoleId.value = row.id
  isEdit.value = true
  Object.assign(form, { name: row.name, code: row.code, description: row.description })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    loading.value = true
    try {
      const url = isEdit.value ? `/api/roles/${editRoleId.value}` : '/api/roles'
      const method = isEdit.value ? 'PUT' : 'POST'
      const res = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${authStore.token}`
        },
        body: JSON.stringify(form)
      })
      const json = await res.json()
      if (json.code === 200) {
        ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
        dialogVisible.value = false
        fetchRoles()
      } else {
        ElMessage.error(json.message || '操作失败')
      }
    } finally {
      loading.value = false
    }
  })
}

const editRoleId = ref<number | null>(null)

async function handleDelete(row: Role) {
  if (row.isSystem) {
    ElMessage.warning('系统角色不可删除')
    return
  }
  await ElMessageBox.confirm(`确定删除角色「${row.name}」？`, '提示', { type: 'warning' })
  const res = await fetch(`/api/roles/${row.id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${authStore.token}` }
  })
  const json = await res.json()
  if (json.code === 200) {
    ElMessage.success('删除成功')
    fetchRoles()
  } else {
    ElMessage.error(json.message || '删除失败')
  }
}

onMounted(fetchRoles)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.el-pagination { margin-top: 16px; }
</style>
