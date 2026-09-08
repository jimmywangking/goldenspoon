<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">业务页面</div>
        <div class="page-desc">选择您要访问的业务功能模块</div>
      </div>
      <el-tag v-if="authStore.isAdmin" type="danger" effect="plain" class="role-tag">管理员</el-tag>
      <el-tag v-else-if="authStore.isOrgAdmin" type="warning" effect="plain" class="role-tag">组织管理员</el-tag>
      <el-tag v-else type="primary" effect="plain" class="role-tag">普通用户</el-tag>
    </div>
    <div class="page-grid">
      <div
        v-for="page in availablePages"
        :key="page.code"
        class="page-card"
        @click="goToPage(page.code)"
      >
        <div class="page-card-icon" :style="{ background: page.code === 'PAGE_1' ? 'rgba(124,58,237,0.1)' : 'rgba(249,115,22,0.1)' }">
          <svg v-if="page.code === 'PAGE_1'" viewBox="0 0 24 24" fill="none" stroke="#7C3AED" stroke-width="1.5" width="32" height="32"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="1.5" width="32" height="32"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
        </div>
        <div class="page-card-name">{{ page.name }}</div>
        <div class="page-card-desc">{{ page.desc }}</div>
        <div class="page-card-perm">
          <el-tag v-if="page.canEdit" size="small" type="success" effect="plain">可编辑</el-tag>
          <el-tag v-else size="small" type="info" effect="plain">仅查看</el-tag>
        </div>
      </div>
    </div>
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
  desc: string
  canView: boolean
  canEdit: boolean
}

const availablePages = computed<PageItem[]>(() => {
  if (!authStore.userInfo) return []
  const perms = authStore.userInfo.permissions || []
  const result: PageItem[] = []

  const pages: PageItem[] = [
    { code: 'PAGE_1', name: '3D 模块化住房编辑器', desc: '拖拽式 3D 场景编辑器，支持版本控制', canView: false, canEdit: false },
    { code: 'PAGE_2', name: '页面 2', desc: '业务页面 2', canView: false, canEdit: false }
  ]

  if (authStore.isAdmin || authStore.isOrgAdmin) {
    pages.forEach(p => { p.canView = true; p.canEdit = true; result.push(p) })
  } else {
    perms.forEach(p => {
      if (p.canView) {
        const page = pages.find(pg => pg.code === p.pageCode)
        if (page) { page.canView = true; page.canEdit = p.canEdit; result.push(page) }
      }
    })
  }
  return result
})

function goToPage(code: string) {
  router.push('/' + code.toLowerCase().replace('_', ''))
}
</script>

<style scoped>
.page-container { padding: 28px; }
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1E1B4B;
  margin-bottom: 4px;
}
.page-desc {
  font-size: 14px;
  color: #6B7280;
}
.role-tag { margin-top: 4px; }
.page-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.page-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  cursor: pointer;
  border: 1px solid #F3F4F6;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.page-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(30, 27, 75, 0.12);
  border-color: #A78BFA;
}
.page-card-icon {
  width: 64px; height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.page-card-name {
  font-size: 16px;
  font-weight: 600;
  color: #1E1B4B;
  margin-bottom: 6px;
}
.page-card-desc {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 16px;
  line-height: 1.5;
}
.page-card-perm {
  display: flex;
  gap: 6px;
}
</style>
