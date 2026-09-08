<template>
  <div class="register-wrap">
    <div class="register-bg">
      <div class="register-orb orb1"></div>
      <div class="register-orb orb2"></div>
    </div>
    <div class="register-card">
      <router-link to="/login" class="back-link">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回登录
      </router-link>
      <h1 class="register-title">创建账户</h1>
      <p class="register-subtitle">填写信息注册，管理员审批后即可登录</p>
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <el-input v-model="form.username" placeholder="用户名（3-50位）" />
          </div>
        </el-form-item>
        <el-form-item prop="realName">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <el-input v-model="form.realName" placeholder="真实姓名" />
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <el-input v-model="form.password" type="password" placeholder="密码（6-100位）" show-password />
          </div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password />
          </div>
        </el-form-item>
        <el-form-item prop="email">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            <el-input v-model="form.email" placeholder="邮箱（可选）" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-hint">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="16" x2="12" y2="12"/>
          <line x1="12" y1="8" x2="12.01" y2="8"/>
        </svg>
        注册后需等待管理员审批才能登录
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/request'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
    loading.value = true
    await axios.post('/api/auth/register', {
      username: form.username,
      realName: form.realName,
      password: form.password,
      email: form.email
    })
    ElMessage.success('注册成功，请等待管理员审批')
    setTimeout(() => router.push('/login'), 1500)
  } catch (err: any) {
    if (err !== false) {
      ElMessage.error(err?.response?.data?.message || err?.message || '注册失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1E1B4B;
  position: relative;
  overflow: hidden;
}
.register-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.register-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.orb1 {
  width: 500px; height: 500px;
  background: #7C3AED;
  top: -150px; left: -100px;
}
.orb2 {
  width: 400px; height: 400px;
  background: #F97316;
  bottom: -100px; right: -80px;
  animation-delay: -4s;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 30px) scale(1.05); }
}
.register-card {
  width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px;
  position: relative;
  z-index: 1;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #7C3AED;
  text-decoration: none;
  margin-bottom: 24px;
  font-weight: 500;
  transition: color 0.2s;
}
.back-link:hover {
  color: #5B21B6;
}
.register-title {
  font-size: 26px;
  font-weight: 700;
  color: #1E1B4B;
  margin-bottom: 6px;
}
.register-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 28px;
}
.register-form {
  margin-bottom: 20px;
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
.register-btn {
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
.register-btn:hover {
  background: #5B21B6;
}
.register-btn:active {
  transform: scale(0.98);
}
.register-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #9CA3AF;
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
