<template>
  <div class="main-layout">
    <el-container style="height:100vh">
      <el-aside width="240px" class="sidebar">
        <div class="sidebar-logo">
          <svg class="sidebar-logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
          <span>GoldenSpoon</span>
        </div>
        <el-menu :default-active="activeMenu" router background-color="transparent" text-color="#C4B5FD" active-text-color="#fff" class="sidebar-menu">
          <el-menu-item index="/" :class="{ 'is-active': activeMenu === '/' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/orgs" v-if="showOrgs" :class="{ 'is-active': activeMenu === '/orgs' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
            <span>组织管理</span>
          </el-menu-item>
          <el-menu-item index="/users" v-if="showUsers" :class="{ 'is-active': activeMenu === '/users' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/roles" v-if="showRoles" :class="{ 'is-active': activeMenu === '/roles' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/approvals" v-if="showApprovals" :class="{ 'is-active': activeMenu === '/approvals' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>
            <span>审批管理</span>
          </el-menu-item>
          <div class="sidebar-divider"></div>
          <el-menu-item index="/page1" v-if="canAccess('PAGE_1')" :class="{ 'is-active': activeMenu === '/page1' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>
            <span>页面 1 — 3D编辑器</span>
          </el-menu-item>
          <el-menu-item index="/page2" v-if="canAccess('PAGE_2')" :class="{ 'is-active': activeMenu === '/page2' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
            <span>页面 2</span>
          </el-menu-item>
          <el-menu-item index="/instances" v-if="canAccess('PAGE_1') || canAccess('PAGE_2')" :class="{ 'is-active': activeMenu === '/instances' }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
            <span>我的页面</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <span class="welcome">{{ welcomeText }}</span>
            <span class="datetime">{{ datetimeText }}</span>
          </div>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ authStore.userInfo?.realName || authStore.userInfo?.username }}
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="6 9 12 15 18 9"/></svg>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-header>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const now = ref(new Date())
let timer: ReturnType<typeof setInterval> | null = null

function pad(n: number) { return String(n).padStart(2, '0') }

const datetimeText = computed(() => {
  const d = now.value
  return `${d.getFullYear()}/${pad(d.getMonth()+1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
})

const welcomeText = computed(() => {
  const u = authStore.userInfo
  if (!u) return ''
  const name = u.realName || u.username
  const org = u.orgName ? ` · ${u.orgName}` : ''
  const role = authStore.isAdmin ? ` · 管理员` : authStore.isOrgAdmin ? ` · 组织管理员` : ''
  return `你好，${name}${org}${role}`
})

onMounted(() => {
  timer = setInterval(() => { now.value = new Date() }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })

function canAccess(pageCode: string): boolean {
  return authStore.canAccess(pageCode)
}

const showOrgs = computed(() => authStore.isAdmin || authStore.isOrgAdmin)
const showUsers = computed(() => authStore.isAdmin || authStore.isOrgAdmin)
const showRoles = computed(() => authStore.isAdmin)
const showApprovals = computed(() => authStore.isAdmin)

async function handleCommand(command: string) {
  if (command === 'logout') {
    await authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}
.sidebar {
  background: #1E1B4B;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255,255,255,0.06);
}
.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.sidebar-logo-icon {
  width: 28px; height: 28px;
  color: #A78BFA;
  flex-shrink: 0;
}
.sidebar-logo span {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.3px;
}
.sidebar-menu {
  flex: 1;
  padding: 12px 0;
  background: transparent !important;
  border: none !important;
}
.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  color: #C4B5FD;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  gap: 10px;
}
.sidebar-menu .el-menu-item:hover {
  background: rgba(255,255,255,0.08);
  color: #fff;
}
.sidebar-menu .el-menu-item.is-active {
  background: rgba(124, 58, 237, 0.5);
  color: #fff;
}
.sidebar-divider {
  height: 1px;
  background: rgba(255,255,255,0.06);
  margin: 8px 16px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #F3F4F6;
  padding: 0 28px;
  height: 56px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome {
  font-size: 14px;
  color: #1E1B4B;
  font-weight: 600;
}
.datetime {
  font-size: 13px;
  color: #6B7280;
  font-variant-numeric: tabular-nums;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}
.user-info:hover {
  background: #F3F4F6;
}
.main-content {
  background: #F5F3FF;
  overflow-y: auto;
}
:deep(.el-menu) {
  border-right: none !important;
}
</style>
