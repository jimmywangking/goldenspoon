<template>
  <div class="home-container">
    <el-card>
      <template #header>
        <span>业务页面</span>
        <el-tag v-if="authStore.isAdmin" type="warning">管理员模式</el-tag>
        <el-tag v-else-if="authStore.isOrgAdmin" type="info">组织管理员</el-tag>
        <el-tag v-else type="success">普通用户</el-tag>
      </template>
      <div class="page-grid">
        <el-card
          v-for="page in availablePages"
          :key="page.code"
          class="page-card"
          shadow="hover"
          @click="goToPage(page.code)"
        >
          <div class="page-icon">{{ page.icon }}</div>
          <div class="page-name">{{ page.name }}</div>
          <div class="page-perm">
            <el-tag v-if="page.canEdit" size="small" type="success">可编辑</el-tag>
            <el-tag v-else size="small" type="info">仅查看</el-tag>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

interface PageItem {
  code: string
  name: string
  icon: string
  canView: boolean
  canEdit: boolean
}

const availablePages = computed<PageItem[]>(() => {
  if (!authStore.userInfo) return []
  const perms = authStore.userInfo.permissions || []
  const result: PageItem[] = []

  const pages: PageItem[] = [
    { code: 'PAGE_1', name: '页面 1', icon: '📄', canView: false, canEdit: false },
    { code: 'PAGE_2', name: '页面 2', icon: '📊', canView: false, canEdit: false }
  ]

  if (authStore.isAdmin || authStore.isOrgAdmin) {
    pages.forEach(p => {
      p.canView = true
      p.canEdit = true
      result.push(p)
    })
  } else {
    perms.forEach(p => {
      if (p.canView) {
        const page = pages.find(pg => pg.code === p.pageCode)
        if (page) {
          page.canView = true
          page.canEdit = p.canEdit
          result.push(page)
        }
      }
    })
  }
  return result
})

function goToPage(code: string) {
  const perm = authStore.userInfo?.permissions?.find(p => p.pageCode === code)
  if (!perm?.canView) return
  router.push('/' + code.toLowerCase().replace('_', ''))
}
</script>

<style scoped>
.home-container { padding: 20px; }
.page-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}
.page-card {
  cursor: pointer;
  transition: transform 0.2s;
  text-align: center;
  padding: 24px;
}
.page-card:hover {
  transform: translateY(-4px);
}
.page-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.page-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}
.page-perm {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>
