import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { hasRoutePermission } from '@/utils/permission'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/ForbiddenView.vue'),
    },
    {
      path: '/500',
      name: 'ServerError',
      component: () => import('@/views/error/ServerErrorView.vue'),
    },
    {
      path: '/',
      component: () => import('@/layouts/BasicLayout.vue'),
      redirect: (_to) => {
        const auth = useAuthStore()
        return auth.getHomePath(auth.role) || '/login'
      },
      children: [
        {
          path: '/admin/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
        },
        {
          path: '/selection/my-team',
          name: 'MyTeam',
          component: () => import('@/views/selection/MyTeamView.vue'),
        },
        {
          path: '/selection/topics',
          name: 'TopicBrowse',
          component: () => import('@/views/selection/TopicBrowseView.vue'),
        },
        {
          path: '/selection/my-application',
          name: 'MyApplication',
          component: () => import('@/views/selection/MyApplicationView.vue'),
        },
        {
          path: '/selection/applications/pending',
          name: 'PendingApplications',
          component: () => import('@/views/selection/PendingApplicationsView.vue'),
        },
        {
          path: '/selection/documents/my',
          name: 'MyDocuments',
          component: () => import('@/views/selection/MyDocumentsView.vue'),
        },
        {
          path: '/selection/documents',
          name: 'DocumentsFeedback',
          component: () => import('@/views/selection/DocumentsFeedbackView.vue'),
        },
        {
          path: '/selection/logs/my',
          name: 'MyLogs',
          component: () => import('@/views/selection/MyLogsView.vue'),
        },
        {
          path: '/selection/logs',
          name: 'LogsFeedback',
          component: () => import('@/views/selection/LogsFeedbackView.vue'),
        },
        {
          path: '/selection/teams/:teamId',
          name: 'TeamDetail',
          component: () => import('@/views/selection/TeamDetailView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/NotFoundView.vue'),
    },
  ],
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

  // 校验路由权限
  if (!hasRoutePermission(to.path, auth.role)) {
    return next('/403')
  }

  return next()
})

export default router
