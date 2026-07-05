import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/ForbiddenView.vue')
    },
    {
      path: '/500',
      name: 'ServerError',
      component: () => import('@/views/error/ServerErrorView.vue')
    },
    {
      path: '/',
      component: () => import('@/layouts/BasicLayout.vue'),
      children: []
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/NotFoundView.vue')
    }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()

  if (!auth.isLoggedIn) {
    await auth.fetchMe()
  }

  // 已登录访问登录页，跳转到对应角色首页
  if (to.path === '/login' && auth.isLoggedIn) {
    return next(auth.getHomePath(auth.role))
  }

  // 公开路由直接放行
  if (to.meta.public) {
    return next()
  }

  // 未登录跳登录
  if (!auth.isLoggedIn) {
    return next('/login')
  }

  // 403/500 等错误页放行
  if (to.path === '/403' || to.path === '/500') {
    return next()
  }

  return next()
})

export default router
