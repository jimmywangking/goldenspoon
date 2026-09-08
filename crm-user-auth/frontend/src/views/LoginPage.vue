<template>
  <div class="login-wrap">
    <div class="login-bg">
      <div class="login-orb orb1"></div>
      <div class="login-orb orb2"></div>
    </div>
    <div class="login-card">
      <div class="login-brand">
        <svg class="login-logo" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        <span class="login-brand-name">GoldenSpoon CRM</span>
      </div>
      <h1 class="login-title">欢迎回来</h1>
      <p class="login-subtitle">请登录您的账户以继续</p>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <el-input v-model="form.username" placeholder="用户名" size="large" />
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="handleLogin" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span class="login-hint">默认管理员: admin / admin123</span>
        <router-link to="/register" class="register-link">还没有账号？立即注册</router-link>
      </div>
    </div>
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

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true
    await authStore.login(form)
    ElMessage.success(`欢迎回来，${authStore.userInfo?.realName || authStore.userInfo?.username}`)
    router.push('/')
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '登录失败'
    if (msg !== '用户不存在或已禁用') {
      ElMessage.error(msg)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1E1B4B;
  position: relative;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.login-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.orb1 {
  width: 500px; height: 500px;
  background: #7C3AED;
  top: -150px; right: -100px;
}
.orb2 {
  width: 400px; height: 400px;
  background: #F97316;
  bottom: -100px; left: -80px;
  animation-delay: -4s;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}
.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px;
  position: relative;
  z-index: 1;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 32px;
}
.login-logo {
  width: 32px; height: 32px;
  color: #7C3AED;
  flex-shrink: 0;
}
.login-brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #1E1B4B;
}
.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #1E1B4B;
  margin-bottom: 6px;
}
.login-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 28px;
}
.login-form {
  margin-bottom: 24px;
}
.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #F3F4F6;
  border-radius: 10px;
  padding: 0 14px;
  border: 2px solid transparent;
  transition: border-color 0.2s, background 0.2s;
}
.input-wrap:focus-within {
  border-color: #7C3AED;
  background: #fff;
}
.input-icon {
  width: 18px; height: 18px;
  color: #9CA3AF;
  flex-shrink: 0;
}
.input-wrap:focus-within .input-icon {
  color: #7C3AED;
}
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: #7C3AED;
  border: none;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}
.login-btn:hover {
  background: #5B21B6;
}
.login-btn:active {
  transform: scale(0.98);
}
.login-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.login-hint {
  color: #9CA3AF;
}
.register-link {
  color: #7C3AED;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.register-link:hover {
  color: #5B21B6;
  text-decoration: underline;
}
:deep(.el-form-item) {
  margin-bottom: 0;
}
:deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0;
}
:deep(.el-input__inner) {
  border: none !important;
  background: transparent !important;
  font-size: 14px;
}
</style>
