import type { UserRole } from '@/types/auth'

export interface MenuItem {
  title: string
  path: string
  icon: string
  roles: UserRole[]
  hidden?: boolean
}

export const menus: MenuItem[] = [
  { title: '仪表盘', path: '/admin/dashboard', icon: 'Odometer', roles: ['ADMIN'] },
  {
    title: '题目浏览',
    path: '/topic/browse',
    icon: 'Reading',
    roles: ['STUDENT', 'TEACHER', 'ADMIN'],
  },
  { title: '我的题目', path: '/topic/my-list', icon: 'Document', roles: ['TEACHER'] },
  { title: '新增题目', path: '/topic/create', icon: 'Plus', roles: ['TEACHER'] },
  { title: '题目审核', path: '/topic/review', icon: 'Finished', roles: ['ADMIN'] },
  { title: '题库管理', path: '/topic/manage', icon: 'Collection', roles: ['ADMIN'] },
  { title: '我的团队', path: '/selection/my-team', icon: 'UserFilled', roles: ['STUDENT'] },
  {
    title: '我的选题申请',
    path: '/selection/my-application',
    icon: 'DocumentChecked',
    roles: ['STUDENT'],
  },
  { title: '选题审核', path: '/selection/applications/pending', icon: 'Stamp', roles: ['TEACHER'] },
  {
    title: '我的过程文档',
    path: '/selection/documents/my',
    icon: 'FolderOpened',
    roles: ['STUDENT'],
  },
  { title: '过程文档反馈', path: '/selection/documents', icon: 'ChatDotRound', roles: ['TEACHER'] },
  { title: '我的开发日志', path: '/selection/logs/my', icon: 'Calendar', roles: ['STUDENT'] },
  { title: '开发日志反馈', path: '/selection/logs', icon: 'ChatLineRound', roles: ['TEACHER'] },
  {
    title: '团队详情',
    path: '/selection/teams',
    icon: 'UserFilled',
    roles: ['TEACHER', 'ADMIN'],
    hidden: true,
  },
  { title: '学生管理', path: '/admin/students', icon: 'User', roles: ['ADMIN'] },
  { title: '教师管理', path: '/admin/teachers', icon: 'User', roles: ['ADMIN'] },
  { title: '公告管理', path: '/admin/notices', icon: 'Bell', roles: ['ADMIN', 'TEACHER'] },
  { title: '公告查看', path: '/notices', icon: 'Bell', roles: ['STUDENT'] },
  { title: '数据备份', path: '/admin/backups', icon: 'CopyDocument', roles: ['ADMIN'] },
  { title: '操作日志', path: '/admin/logs', icon: 'Tickets', roles: ['ADMIN'] },
  { title: '签到任务', path: '/attendance/tasks', icon: 'AlarmClock', roles: ['TEACHER', 'ADMIN'] },
  { title: '考勤记录', path: '/attendance/records', icon: 'Checked', roles: ['TEACHER', 'ADMIN'] },
  { title: '补签审核', path: '/attendance/makeup', icon: 'EditPen', roles: ['TEACHER', 'ADMIN'] },
  {
    title: '考勤统计',
    path: '/attendance/statistics',
    icon: 'TrendCharts',
    roles: ['TEACHER', 'ADMIN'],
  },
  { title: '我的考勤', path: '/attendance/records/my', icon: 'Clock', roles: ['STUDENT'] },
  { title: '我的补签', path: '/attendance/makeup/my', icon: 'Edit', roles: ['STUDENT'] },
  { title: '阶段任务', path: '/score/stage-tasks', icon: 'Timer', roles: ['TEACHER', 'ADMIN'] },
  { title: '过程评价', path: '/score/progress', icon: 'DataLine', roles: ['TEACHER', 'ADMIN'] },
  { title: '成绩管理', path: '/score/list', icon: 'Medal', roles: ['TEACHER', 'ADMIN'] },
  { title: '我的成绩', path: '/score/my', icon: 'Trophy', roles: ['STUDENT'] },
]

export const menusByRole: Record<UserRole, MenuItem[]> = {
  STUDENT: menus.filter((m) => m.roles.includes('STUDENT')),
  TEACHER: menus.filter((m) => m.roles.includes('TEACHER')),
  ADMIN: menus.filter((m) => m.roles.includes('ADMIN')),
}

// 不在导航菜单中显示、但需要通过路由守卫的动态路由
export const hiddenRoutes: { title: string; path: string; roles: UserRole[] }[] = [
  { title: '题目详情', path: '/topic/:topicId', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { title: '题目资料', path: '/topic/:topicId/files', roles: ['TEACHER', 'ADMIN'] },
  { title: '编辑题目', path: '/topic/edit/:topicId', roles: ['TEACHER', 'ADMIN'] },
  { title: '团队详情', path: '/selection/teams/:teamId', roles: ['TEACHER', 'ADMIN'] },
]

function pathToRegex(pattern: string): RegExp {
  const escaped = pattern.replace(/[.+^${}()|[\]\\]/g, '\\$&')
  const withParam = escaped.replace(new RegExp(':\\w+', 'g'), '[^/]+')
  return new RegExp(`^${withParam}$`)
}

export function hasRoutePermission(path: string, role?: UserRole) {
  if (!role) return false
  const normalizedPath = path.length > 1 ? path.replace(/\/+$/, '') : path
  const allowedMenus = menusByRole[role]
  if (allowedMenus.some((m) => normalizedPath === m.path)) return true
  return hiddenRoutes.some(
    (r) => r.roles.includes(role) && pathToRegex(r.path).test(normalizedPath)
  )
}

export function getMenuByPath(path: string) {
  return menus.find((m) => path.startsWith(m.path))
}

export const roleTextMap: Record<UserRole, string> = {
  STUDENT: '学生',
  TEACHER: '教师',
  ADMIN: '管理员',
}
