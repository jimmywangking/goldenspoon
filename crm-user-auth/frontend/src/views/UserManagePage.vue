<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showCreateDialog">新增用户</el-button>
        </div>
      </template>
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="orgName" label="所属组织">
          <template #default="{ row }">
            {{ row.orgName || '个人用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'info'">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限">
          <template #default="{ row }">
            <el-tag
              v-for="perm in row.permissions"
              :key="perm.pageCode"
              size="small"
              class="perm-tag"
              :type="perm.canView ? 'success' : 'info'"
            >
              {{ perm.pageCode }}{{ perm.canEdit ? '(可编辑)' : '(只读)' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="showResetPwdDialog(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="page.current"
        @current-change="fetchUsers"
      />
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-select v-model="form.orgId" clearable placeholder="选择组织" style="width: 100%">
            <el-option
              v-for="org in orgs"
              :key="org.id"
              :label="org.name"
              :value="org.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio value="USER">普通用户</el-radio>
            <el-radio value="ORG_ADMIN">组织管理员</el-radio>
            <el-radio value="ADMIN">系统管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="350px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwd" :loading="pwdSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, orgApi } from '@/api'
import type { UserDetail, CreateUserRequest, Org } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const users = ref<UserDetail[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const page = reactive({ current: 1 })
const orgs = ref<Org[]>([])

const form = reactive<CreateUserRequest>({ username: '', password: '', realName: '', email: '', phone: '', orgId: undefined, role: 'USER' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 重置密码
const resetPwdVisible = ref(false)
const pwdFormRef = ref()
const pwdSubmitting = ref(false)
const pwdForm = reactive({ password: '' })
const pwdRules = { password: [{ required: true, message: '请输入新密码', trigger: 'blur' }] }
let resetUserId: number | null = null

async function fetchUsers() {
  loading.value = true
  try {
    const res = await userApi.list({ current: page.current, size: 10 })
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchOrgs() {
  const res = await orgApi.list({ size: 100 })
  orgs.value = res.data.records
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { username: '', password: '', realName: '', email: '', phone: '', orgId: undefined, role: 'USER' })
  dialogVisible.value = true
}

function showEditDialog(row: UserDetail) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, { username: row.username, realName: row.realName || '', email: row.email || '', phone: row.phone || '', orgId: row.orgId, role: row.role })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (isEdit.value && editId.value) {
      const { password, ...rest } = form
      await userApi.update(editId.value, rest)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: UserDetail) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch {}
}

function showResetPwdDialog(row: UserDetail) {
  resetUserId = row.id
  pwdForm.password = ''
  resetPwdVisible.value = true
}

async function handleResetPwd() {
  try {
    await pwdFormRef.value.validate()
    if (!resetUserId) return
    pwdSubmitting.value = true
    await userApi.resetPassword(resetUserId, pwdForm.password)
    ElMessage.success('密码已重置')
    resetPwdVisible.value = false
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(() => {
  fetchUsers()
  fetchOrgs()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.perm-tag { margin-right: 4px; }
</style>
