import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginPage.vue'),
    meta: { public: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/Error403.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'orgs',
        name: 'OrgManage',
        component: () => import('@/views/OrgManagePage.vue'),
        meta: { requiresAdminOrOrgAdmin: true }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/UserManagePage.vue'),
        meta: { requiresAdminOrOrgAdmin: true }
      },
      {
        path: 'roles',
        name: 'RoleManage',
        component: () => import('@/views/RoleManagePage.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'page1',
        name: 'Page1',
        component: () => import('@/views/Page1.vue'),
        meta: { requiredPermission: 'PAGE_1' }
      },
      {
        path: 'page2',
        name: 'Page2',
        component: () => import('@/views/Page2.vue'),
        meta: { requiredPermission: 'PAGE_2' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  if (authStore.isLoggedIn && !authStore.userInfo) {
    try {
      await authStore.fetchCurrentUser()
    } catch {
      authStore.logout()
      next('/login')
      return
    }
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }
  if (to.meta.requiredPermission && !authStore.canAccess(to.meta.requiredPermission as string)) {
    next('/403')
    return
  }
  if (to.meta.requiresAdminOrOrgAdmin) {
    if (!authStore.isAdmin && !authStore.isOrgAdmin) {
      next('/403')
      return
    }
  }
  if (to.meta.requiresAdmin) {
    if (!authStore.isAdmin) {
      next('/403')
      return
    }
  }
  next()
})

// Handle navigation duplication gracefully (e.g. login redirect)
router.onError(err => {
  if (err && typeof err === 'object' && 'type' in err && err.type === 'NavigationDuplicated') {
    return
  }
  console.error('Router error:', err)
})

export default router
