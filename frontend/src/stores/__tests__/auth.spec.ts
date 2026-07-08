import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import * as authApi from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { LoginDTO, LoginVO, UserRole } from '@/types/auth'

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  me: vi.fn(),
  logout: vi.fn(),
}))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.stubGlobal('location', { ...window.location, href: '' })
  })

  const mockLoginVo: LoginVO = {
    userId: 1,
    username: 'student',
    role: 'STUDENT',
    relatedId: 10,
    name: '张同学',
  }

  it('login sets user and clears loading', async () => {
    vi.mocked(authApi.login).mockResolvedValueOnce(mockLoginVo)
    const store = useAuthStore()

    const dto: LoginDTO = { username: 'student', password: '123456', role: 'STUDENT' }
    const result = await store.login(dto)

    expect(authApi.login).toHaveBeenCalledWith(dto)
    expect(result).toEqual(mockLoginVo)
    expect(store.user).toEqual(mockLoginVo)
    expect(store.loading).toBe(false)
  })

  it('fetchMe sets user when API succeeds', async () => {
    vi.mocked(authApi.me).mockResolvedValueOnce(mockLoginVo)
    const store = useAuthStore()

    const result = await store.fetchMe()

    expect(authApi.me).toHaveBeenCalled()
    expect(result).toEqual(mockLoginVo)
    expect(store.isLoggedIn).toBe(true)
  })

  it('fetchMe clears user when API fails', async () => {
    vi.mocked(authApi.me).mockRejectedValueOnce(new Error('session expired'))
    const store = useAuthStore()

    const result = await store.fetchMe()

    expect(result).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isLoggedIn).toBe(false)
  })

  it('logout calls API and clears user', async () => {
    vi.mocked(authApi.logout).mockResolvedValueOnce(undefined)
    const store = useAuthStore()
    store.user = mockLoginVo

    await store.logout()

    expect(authApi.logout).toHaveBeenCalled()
    expect(store.user).toBeNull()
    expect(window.location.href).toBe('/login')
  })

  it('clear resets user', () => {
    const store = useAuthStore()
    store.user = mockLoginVo

    store.clear()

    expect(store.user).toBeNull()
  })

  describe('getHomePath', () => {
    it.each([
      ['STUDENT', '/selection/my-team'],
      ['TEACHER', '/topic/my-list'],
      ['ADMIN', '/admin/dashboard'],
      [undefined, '/login'],
    ] as [UserRole | undefined, string][])('returns %s for role %s', (role, expected) => {
      const store = useAuthStore()
      expect(store.getHomePath(role)).toBe(expected)
    })
  })

  describe('role getters', () => {
    it.each([
      ['STUDENT', { student: true, teacher: false, admin: false }],
      ['TEACHER', { student: false, teacher: true, admin: false }],
      ['ADMIN', { student: false, teacher: false, admin: true }],
    ] as [UserRole, { student: boolean; teacher: boolean; admin: boolean }][])(
      '%s getters',
      (role, expected) => {
        const store = useAuthStore()
        store.user = { ...mockLoginVo, role }

        expect(store.isStudent).toBe(expected.student)
        expect(store.isTeacher).toBe(expected.teacher)
        expect(store.isAdmin).toBe(expected.admin)
      }
    )
  })
})
