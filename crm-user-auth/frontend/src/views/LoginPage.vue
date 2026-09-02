<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">CRM 用户管理系统</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-hint">默认管理员: admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function getPreferredPath() {
  if (authStore.isAdmin || authStore.isOrgAdmin) return '/orgs'
  for (const code of ['PAGE_1', 'PAGE_2']) {
    if (authStore.canAccess(code)) return '/' + code.toLowerCase().replace('_', '')
  }
  return '/403'
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true
    await authStore.login(form)
    await authStore.fetchCurrentUser()
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err: any) {
    if (err !== false) {
      ElMessage.error(err?.response?.data?.message || err?.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 24px;
}
.login-form {
  margin-top: 20px;
}
.login-btn {
  width: 100%;
}
.login-hint {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 15px;
}
</style>
