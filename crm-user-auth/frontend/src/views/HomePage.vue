<template>
  <div class="home-container">
    <!-- KPI 看板 -->
    <div class="kpi-grid">
      <div class="kpi-card kpi-card--purple">
        <div class="kpi-icon" style="background:rgba(124,58,237,0.1)">
          <svg viewBox="0 0 24 24" fill="none" stroke="#7C3AED" stroke-width="2" width="24" height="24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
        </div>
        <div class="kpi-num">{{ stats.userTotal }}</div>
        <div class="kpi-label">总用户数</div>
      </div>
      <div class="kpi-card kpi-card--orange">
        <div class="kpi-icon" style="background:rgba(249,115,22,0.1)">
          <svg viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2" width="24" height="24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
        </div>
        <div class="kpi-num">{{ stats.orgTotal }}</div>
        <div class="kpi-label">组织总数</div>
      </div>
      <div class="kpi-card kpi-card--green" v-if="authStore.isAdmin">
        <div class="kpi-icon" style="background:rgba(16,185,129,0.1)">
          <svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2" width="24" height="24"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>
        </div>
        <div class="kpi-num">{{ stats.pendingCount }}</div>
        <div class="kpi-label">待审批</div>
      </div>
      <div class="kpi-card kpi-card--blue" v-if="authStore.isAdmin">
        <div class="kpi-icon" style="background:rgba(59,130,246,0.1)">
          <svg viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2" width="24" height="24"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
        </div>
        <div class="kpi-num">{{ stats.instanceTotal }}</div>
        <div class="kpi-label">页面实例</div>
      </div>
    </div>

    <!-- 角色分布 -->
    <div class="section" v-if="authStore.isAdmin">
      <div class="section-title">角色分布</div>
      <div class="role-distribution">
        <div v-for="r in roleStats" :key="r.role" class="role-stat-item">
          <div class="role-stat-label">{{ roleLabel(r.role) }}</div>
          <div class="role-stat-bar-wrap">
            <div class="role-stat-bar" :style="{ width: `${r.pct}%`, background: roleColor(r.role) }"></div>
          </div>
          <div class="role-stat-num">{{ r.count }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section">
      <div class="section-title">快捷入口</div>
      <div class="shortcut-grid">
        <div class="shortcut-item" v-if="canAccess('PAGE_1')" @click="goTo('/page1')">
          <div class="shortcut-icon" style="background:rgba(124,58,237,0.1)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#7C3AED" stroke-width="2" width="20" height="20"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>
          </div>
          <span class="shortcut-name">3D 编辑器</span>
        </div>
        <div class="shortcut-item" v-if="canAccess('PAGE_2')" @click="goTo('/page2')">
          <div class="shortcut-icon" style="background:rgba(249,115,22,0.1)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2" width="20" height="20"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
          </div>
          <span class="shortcut-name">页面 2</span>
        </div>
        <div class="shortcut-item" v-if="authStore.isAdmin || authStore.isOrgAdmin" @click="goTo('/users')">
          <div class="shortcut-icon" style="background:rgba(124,58,237,0.1)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#7C3AED" stroke-width="2" width="20" height="20"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </div>
          <span class="shortcut-name">用户管理</span>
        </div>
        <div class="shortcut-item" v-if="authStore.isAdmin || authStore.isOrgAdmin" @click="goTo('/orgs')">
          <div class="shortcut-icon" style="background:rgba(249,115,22,0.1)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2" width="20" height="20"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
          </div>
          <span class="shortcut-name">组织管理</span>
        </div>
        <div class="shortcut-item" v-if="authStore.isAdmin" @click="goTo('/roles')">
          <div class="shortcut-icon" style="background:rgba(245,158,11,0.1)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2" width="20" height="20"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          </div>
          <span class="shortcut-name">角色管理</span>
        </div>
        <div class="shortcut-item" v-if="authStore.isAdmin" @click="goTo('/approvals')">
          <div class="shortcut-icon" style="background:rgba(16,185,129,0.1)">
            <svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2" width="20" height="20"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>
          </div>
          <span class="shortcut-name">审批管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orgApi, userApi, registerApi, instanceApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref({ userTotal: 0, orgTotal: 0, pendingCount: 0, instanceTotal: 0 })
const roleStats = ref<{ role: string; count: number; pct: number }[]>([])

function roleLabel(code: string): string {
  const map: Record<string, string> = { ADMIN: '系统管理员', ORG_ADMIN: '组织管理员', USER: '普通用户' }
  return map[code] || code
}
function roleColor(code: string): string {
  const map: Record<string, string> = { ADMIN: '#EF4444', ORG_ADMIN: '#F97316', USER: '#7C3AED' }
  return map[code] || '#6B7280'
}

function canAccess(pageCode: string): boolean {
  return authStore.canAccess(pageCode)
}

async function loadStats() {
  try {
    const [usersRes, orgsRes, pendingRes] = await Promise.allSettled([
      userApi.list({ page: 1, size: 1 }),
      orgApi.list({ page: 1, size: 1 }),
      authStore.isAdmin ? registerApi.listPending(1, 1) : Promise.resolve({ data: { total: 0 } })
    ])
    stats.value.userTotal = (usersRes.status === 'fulfilled' ? (usersRes.value?.data?.total ?? 0) : 0)
    stats.value.orgTotal = (orgsRes.status === 'fulfilled' ? (orgsRes.value?.data?.total ?? 0) : 0)
    stats.value.pendingCount = (pendingRes.status === 'fulfilled' ? (pendingRes.value?.data?.total ?? 0) : 0)

    if (authStore.isAdmin) {
      try {
        const instRes = await instanceApi.adminList({ current: 1, size: 1 })
        stats.value.instanceTotal = instRes.data?.total ?? 0
      } catch {}
    }
  } catch {}
}

async function loadRoleStats() {
  try {
    const res = await userApi.list({ page: 1, size: 1000 })
    const users = res.data?.records ?? []
    const counts: Record<string, number> = {}
    let total = 0
    users.forEach((u: any) => {
      const r = u.role || 'USER'
      counts[r] = (counts[r] || 0) + 1
      total++
    })
    roleStats.value = Object.entries(counts).map(([role, count]) => ({
      role, count, pct: total > 0 ? Math.round(count / total * 100) : 0
    }))
  } catch {}
}

function goTo(path: string) { router.push(path) }

onMounted(() => { loadStats(); loadRoleStats() })
</script>

<style scoped>
.home-container { padding: 28px; }

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.kpi-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #F3F4F6;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: box-shadow 0.2s;
}
.kpi-card:hover { box-shadow: 0 8px 24px rgba(30,27,75,0.08); }
.kpi-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.kpi-num { font-size: 28px; font-weight: 700; color: #1E1B4B; line-height: 1; }
.kpi-label { font-size: 13px; color: #6B7280; margin-top: 4px; }

.section { margin-bottom: 28px; }
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1E1B4B;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #7C3AED;
}

.role-distribution {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #F3F4F6;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.role-stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
}
.role-stat-label {
  width: 90px;
  font-size: 13px;
  color: #374151;
  font-weight: 500;
  flex-shrink: 0;
}
.role-stat-bar-wrap {
  flex: 1;
  height: 8px;
  background: #F3F4F6;
  border-radius: 99px;
  overflow: hidden;
}
.role-stat-bar {
  height: 100%;
  border-radius: 99px;
  transition: width 0.6s ease;
}
.role-stat-num {
  width: 36px;
  font-size: 13px;
  font-weight: 600;
  color: #1E1B4B;
  text-align: right;
  flex-shrink: 0;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 12px;
}
.shortcut-item {
  background: #fff;
  border: 1px solid #F3F4F6;
  border-radius: 12px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.shortcut-item:hover {
  border-color: #A78BFA;
  box-shadow: 0 4px 16px rgba(124,58,237,0.1);
  transform: translateY(-2px);
}
.shortcut-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}
.shortcut-name {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
  text-align: center;
}
</style>
