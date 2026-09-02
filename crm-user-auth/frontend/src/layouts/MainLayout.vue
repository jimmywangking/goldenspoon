<template>
  <el-container class="main-layout">
    <el-aside width="220px">
      <div class="logo">CRM 管理</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/orgs" v-if="showOrgs">
          <span>组织管理</span>
        </el-menu-item>
        <el-menu-item index="/users" v-if="showUsers">
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/roles" v-if="showRoles">
          <span>角色管理</span>
        </el-menu-item>
        <el-sub-menu index="pages">
          <template #title>业务页面</template>
          <el-menu-item index="/page1" v-if="canAccess('PAGE_1')">页面 1</el-menu-item>
          <el-menu-item index="/page2" v-if="canAccess('PAGE_2')">页面 2</el-menu-item>
          <el-menu-item index="/instances" v-if="canAccess('PAGE_1') || canAccess('PAGE_2')">我的页面</el-menu-item>
        </el-sub-menu>
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
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title as string || '管理后台')

const now = ref(new Date())
let timer: ReturnType<typeof setInterval> | null = null

function pad(n: number) { return String(n).padStart(2, '0') }

const datetimeText = computed(() => {
  const d = now.value
  return `${d.getFullYear()}年${pad(d.getMonth() + 1)}月${pad(d.getDate())}日  ${pad(d.getHours())}:${pad(d.getMinutes())}`
})

const welcomeText = computed(() => {
  const u = authStore.userInfo
  if (!u) return ''
  const name = u.realName || u.username
  const org = u.orgName ? `· ${u.orgName}` : ''
  return `欢迎，${name}${org}`
})

onMounted(() => {
  timer = setInterval(() => { now.value = new Date() }, 1000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function canAccess(pageCode: string): boolean {
  return authStore.canAccess(pageCode)
}

const showOrgs = computed(() => authStore.isAdmin || authStore.isOrgAdmin)
const showUsers = computed(() => authStore.isAdmin || authStore.isOrgAdmin)
const showRoles = computed(() => authStore.isAdmin)

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
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: #263445;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}
.datetime {
  font-size: 13px;
  color: #909399;
  font-variant-numeric: tabular-nums;
}
.header-title {
  font-size: 16px;
  font-weight: 500;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}
</style>
