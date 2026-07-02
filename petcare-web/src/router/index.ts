import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken, clearAuth } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/Home.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/pre-consultation',
    name: 'PreConsultation',
    component: () => import('@/views/consult/PreConsultation.vue'),
    meta: { title: 'AI预问诊' },
  },
  {
    path: '/consult',
    name: 'Consult',
    component: () => import('@/views/consult/ConsultList.vue'),
    meta: { title: '问诊记录' },
  },
  {
    path: '/consult/doctor/:id',
    name: 'DoctorDetail',
    component: () => import('@/views/doctor/DoctorDetail.vue'),
    meta: { title: '医生详情' },
  },
  {
    path: '/consult/chat/:id',
    name: 'Chat',
    component: () => import('@/views/consult/Chat.vue'),
    meta: { title: '问诊对话' },
  },
  {
    path: '/prescription/:consultationId',
    name: 'Prescription',
    component: () => import('@/views/consult/Prescription.vue'),
    meta: { title: '电子处方' },
  },
  {
    path: '/doctor-list',
    name: 'DoctorList',
    component: () => import('@/views/doctor/DoctorList.vue'),
    meta: { title: '医生列表' },
  },
  {
    path: '/pet/list',
    name: 'PetList',
    component: () => import('@/views/pet/PetList.vue'),
    meta: { title: '我的宠物' },
  },
  {
    path: '/pet/add',
    name: 'PetAdd',
    component: () => import('@/views/pet/PetAdd.vue'),
    meta: { title: '添加宠物' },
  },
  {
    path: '/pet/edit/:id',
    name: 'PetEdit',
    component: () => import('@/views/pet/PetAdd.vue'),
    meta: { title: '编辑宠物' },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/Profile.vue'),
    meta: { title: '个人中心' },
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
  scrollBehavior() {
    return { top: 0 }
  },
})

/** 路由守卫：未登录跳 /login，已登录访问 /login 跳首页 */
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || 'PetCare'

  const token = getToken()

  // 校验用户类型：普通用户（userType=1）或医生（userType=2）允许访问
  if (token && !to.meta.noAuth) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const userType = payload.userType
      if (userType !== 1 && userType !== 2) {
        // 非普通用户/医生（如 admin）的 token 不能访问用户端
        clearAuth()
        return next({ name: 'Login', query: { redirect: to.fullPath } })
      }
    } catch {
      // token 解析失败（可能损坏），清除并跳登录
      clearAuth()
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }
  }

  if (to.meta.noAuth) {
    // 已登录用户访问登录页 → 跳首页
    if (token && to.name === 'Login') {
      return next('/home')
    }
    return next()
  }

  if (!token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  next()
})

export default router
