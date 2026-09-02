<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>组织管理</span>
          <el-button type="primary" @click="showCreateDialog">新增组织</el-button>
        </div>
      </template>
      <el-table :data="orgs" v-loading="loading" stripe>
        <el-table-column prop="name" label="组织名称" />
        <el-table-column prop="contactName" label="联系人" />
        <el-table-column prop="contactPhone" label="联系电话" />
        <el-table-column prop="isActive" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        @current-change="fetchOrgs"
        @size-change="fetchOrgs"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑组织' : '新增组织'" width="400px">
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
import type { Org } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const orgs = ref<Org[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()

const page = reactive({ current: 1, size: 10 })

const form = reactive({ name: '', contactName: '', contactPhone: '' })
const rules = { name: [{ required: true, message: '请输入组织名称', trigger: 'blur' }] }

async function fetchOrgs() {
  loading.value = true
  try {
    const res = await orgApi.list({ current: page.current, size: page.size })
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
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
