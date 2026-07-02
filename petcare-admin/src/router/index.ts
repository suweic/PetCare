import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken, clearAuth } from '@/utils/auth'

/** 需要 keep-alive 的路由 name 列表 */
export const KEEP_ALIVE_NAMES = ['Dashboard', 'DoctorAudit', 'DoctorList', 'UserList', 'ConsultationList']

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue'),
    meta: { title: '工作台', icon: 'Odometer' },
  },
  {
    path: '/doctor/audit',
    name: 'DoctorAudit',
    component: () => import('@/views/doctor/DoctorAudit.vue'),
    meta: { title: '医生审核', icon: 'Checked' },
  },
  {
    path: '/doctor/list',
    name: 'DoctorList',
    component: () => import('@/views/doctor/DoctorList.vue'),
    meta: { title: '医生管理', icon: 'UserFilled' },
  },
  {
    path: '/user/list',
    name: 'UserList',
    component: () => import('@/views/user/UserList.vue'),
    meta: { title: '用户管理', icon: 'Avatar' },
  },
  {
    path: '/consultation/list',
    name: 'ConsultationList',
    component: () => import('@/views/consultation/ConsultationList.vue'),
    meta: { title: '问诊管理', icon: 'ChatDotRound' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/auth/NotFound.vue'),
    meta: { title: '404', noAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title as string} - PetCare 管理后台`
  const token = getToken()

  // 管理后台仅允许 userType=3（管理员）
  if (token && !to.meta.noAuth) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const userType = payload.userType
      if (userType !== 3) {
        // 非管理员 token 无法访问管理后台
        clearAuth()
        return next({ name: 'Login', query: { redirect: to.fullPath } })
      }
    } catch {
      clearAuth()
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }
  }

  if (to.meta.noAuth) {
    if (token && to.name === 'Login') return next('/dashboard')
    return next()
  }

  if (!token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  next()
})

export default router
