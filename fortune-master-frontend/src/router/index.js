import { createRouter, createWebHistory } from 'vue-router'
import { authState, clearSession, fetchSession, getToken, hasPermission } from '../auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'AI 算命大师'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: 'RBAC 登录',
      guestOnly: true
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: {
      title: 'RBAC 工作台',
      requiresAuth: true,
      permission: 'dashboard:view',
      section: 'overview'
    }
  },
  {
    path: '/dashboard/users',
    name: 'DashboardUsers',
    component: () => import('../views/Dashboard.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true,
      permission: 'system:user:list',
      section: 'users'
    }
  },
  {
    path: '/dashboard/roles',
    name: 'DashboardRoles',
    component: () => import('../views/Dashboard.vue'),
    meta: {
      title: '角色权限',
      requiresAuth: true,
      permission: 'system:role:list',
      section: 'roles'
    }
  },
  {
    path: '/fortune-master',
    name: 'FortuneMaster',
    component: () => import('../views/FortuneMaster.vue'),
    meta: {
      title: 'AI 算命大师对话',
      requiresAuth: true,
      permission: 'ai:chat'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let hydrated = false

router.beforeEach(async (to) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const token = getToken()
  if (token && !hydrated && !authState.user) {
    try {
      await fetchSession()
      hydrated = true
    } catch (error) {
      clearSession()
      hydrated = false
    }
  }

  if (to.meta.guestOnly && token) {
    return '/dashboard'
  }

  if (to.meta.requiresAuth && !token) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }

  if (to.meta.requiresAuth && token && !authState.user) {
    try {
      await fetchSession()
      hydrated = true
    } catch (error) {
      clearSession()
      hydrated = false
      return `/login?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }

  if (to.meta.permission && !hasPermission(to.meta.permission)) {
    return '/dashboard'
  }

  return true
})

export default router
