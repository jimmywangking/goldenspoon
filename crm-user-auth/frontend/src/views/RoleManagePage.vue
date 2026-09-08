<template>
  <div class="role-container">
    <div class="page-header">
      <div>
        <div class="page-title">角色管理</div>
        <div class="page-desc">管理角色及每个角色可访问的业务页面权限</div>
      </div>
      <el-button type="primary" @click="showCreateDialog">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="margin-right:6px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增角色
      </el-button>
    </div>

    <!-- 角色列表 -->
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="activePermRoleId === row.id ? 'primary' : ''"
              @click="togglePermPanel(row)"
              :disabled="row.isSystem"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14" style="margin-right:4px"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              权限配置
            </el-button>
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

    <!-- 权限配置面板 -->
    <div v-show="activePermRoleId !== null" class="perm-panel-wrap">
      <el-card shadow="hover" class="perm-card">
        <template #header>
          <div class="perm-header">
            <span class="perm-header-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="#7C3AED" stroke-width="2" width="18" height="18"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              权限配置 — {{ activePermRoleName }}
            </span>
            <el-button size="small" text @click="activePermRoleId = null" style="color:#9CA3AF">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </el-button>
          </div>
        </template>
        <div class="perm-body">
          <div class="perm-instructions">为以下业务页面设置查看和编辑权限：</div>
          <div
            v-for="pageDef in PAGE_DEFS"
            :key="pageDef.code"
            class="perm-row"
          >
            <div class="perm-row-left">
              <div class="perm-page-tag" :style="{ background: pageDef.colorBg, color: pageDef.color }">
                {{ pageDef.code }}
              </div>
              <div class="perm-page-desc">{{ pageDef.name }} — {{ pageDef.desc }}</div>
            </div>
            <div class="perm-row-controls">
              <el-checkbox v-model="permForms[pageDef.code].canView" :disabled="pageDef.readonly">
                可查看
              </el-checkbox>
              <el-checkbox v-model="permForms[pageDef.code].canEdit" :disabled="!permForms[pageDef.code].canView || pageDef.readonly">
                可编辑
              </el-checkbox>
            </div>
          </div>
          <div class="perm-footer">
            <el-button @click="activePermRoleId = null">取消</el-button>
            <el-button type="primary" :loading="savingPerms" @click="savePermissions">保存权限</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑角色对话框 -->
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
import { roleApi } from '@/api'
import type { Role, RolePagePermission } from '@/types'

const PAGE_DEFS = [
  { code: 'PAGE_1', name: '3D 模块化住房编辑器', desc: '拖拽式 3D 场景编辑器，支持版本控制', color: '#7C3AED', colorBg: 'rgba(124,58,237,0.1)', readonly: false },
  { code: 'PAGE_2', name: '页面 2', desc: '业务页面 2', color: '#F97316', colorBg: 'rgba(249,115,22,0.1)', readonly: false }
]

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const savingPerms = ref(false)
const roles = ref<Role[]>([])
const total = ref(0)
const page = reactive({ current: 1 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const editRoleId = ref<number | null>(null)
const formRef = ref()

const form = reactive({ name: '', code: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 权限面板状态
const activePermRoleId = ref<number | null>(null)
const activePermRoleName = ref('')
const permForms = ref<Record<string, { canView: boolean; canEdit: boolean }>>({})

async function fetchRoles() {
  loading.value = true
  try {
    const res = await roleApi.list({ page: page.current, size: 20 })
    roles.value = res.data.records as Role[]
    total.value = res.data.total
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

function togglePermPanel(row: Role) {
  if (row.isSystem) { ElMessage.warning('系统角色权限不可修改'); return }
  if (activePermRoleId.value === row.id) {
    activePermRoleId.value = null
    return
  }
  activePermRoleId.value = row.id
  activePermRoleName.value = row.name
  // 异步加载权限数据
  ;(async () => {
    try {
      const res = await roleApi.getPermissions(row.id)
      const perms: RolePagePermission[] = res.data ?? []
      const form: Record<string, { canView: boolean; canEdit: boolean }> = {}
      PAGE_DEFS.forEach(def => {
        const p = perms.find(x => x.pageCode === def.code)
        form[def.code] = { canView: p?.canView ?? false, canEdit: p?.canEdit ?? false }
      })
      permForms.value = form
    } catch {
      const form: Record<string, { canView: boolean; canEdit: boolean }> = {}
      PAGE_DEFS.forEach(def => { form[def.code] = { canView: false, canEdit: false } })
      permForms.value = form
    }
  })()
}

async function savePermissions() {
  if (activePermRoleId.value === null) return
  savingPerms.value = true
  try {
    const permissions: RolePagePermission[] = PAGE_DEFS.map(def => ({
      roleId: activePermRoleId.value!,
      pageCode: def.code,
      canView: permForms.value[def.code]?.canView ?? false,
      canEdit: permForms.value[def.code]?.canEdit ?? false
    }))
    await roleApi.setPermissions(activePermRoleId.value, permissions)
    ElMessage.success('权限已保存')
    activePermRoleId.value = null
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally { savingPerms.value = false }
}

onMounted(fetchRoles)
</script>

<style scoped>
.role-container { padding: 28px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #1E1B4B; margin-bottom: 4px; }
.page-desc { font-size: 13px; color: #6B7280; }
.table-card { border-radius: 16px; border: none; margin-bottom: 16px; }
.data-table { border-radius: 12px; overflow: hidden; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }

/* 权限面板 */
.perm-panel-wrap {
  animation: slideDown 0.2s ease;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to   { opacity: 1; transform: translateY(0); }
}
.perm-card {
  border-radius: 16px;
  border: 1px solid #A78BFA;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.08);
}
.perm-card :deep(.el-card__header) {
  background: linear-gradient(to right, rgba(124,58,237,0.04), transparent);
  border-bottom: 1px solid rgba(124,58,237,0.1);
  padding: 14px 20px;
}
.perm-header { display: flex; align-items: center; justify-content: space-between; }
.perm-header-title {
  font-size: 15px;
  font-weight: 600;
  color: #1E1B4B;
  display: flex;
  align-items: center;
  gap: 8px;
}
.perm-body { padding: 20px 24px 24px; }
.perm-instructions {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F3F4F6;
}
.perm-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #F9FAFB;
  gap: 16px;
}
.perm-row:last-child { border-bottom: none; }
.perm-row-left { display: flex; align-items: center; gap: 12px; flex: 1; min-width: 0; }
.perm-page-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  white-space: nowrap;
  flex-shrink: 0;
}
.perm-page-desc {
  font-size: 13px;
  color: #6B7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.perm-row-controls {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}
.perm-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #F3F4F6;
}
</style>
