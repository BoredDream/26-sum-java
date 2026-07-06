import { beforeEach, describe, expect, it, vi } from 'vitest'
import { get, post } from '@/utils/request'
import { login, logout, me } from '@/api/auth'
import type { LoginDTO } from '@/types/auth'

vi.mock('@/utils/request', () => ({
  post: vi.fn(),
  get: vi.fn(),
}))

describe('auth api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login posts to /auth/login with dto', async () => {
    const dto: LoginDTO = { username: 'admin', password: '123456', role: 'ADMIN' }
    const vo = { userId: 1, username: 'admin', role: 'ADMIN', name: '管理员' }
    vi.mocked(post).mockResolvedValueOnce(vo)

    const result = await login(dto)

    expect(post).toHaveBeenCalledWith('/auth/login', dto)
    expect(result).toEqual(vo)
  })

  it('logout posts to /auth/logout', async () => {
    vi.mocked(post).mockResolvedValueOnce(undefined)

    await logout()

    expect(post).toHaveBeenCalledWith('/auth/logout')
  })

  it('me gets from /auth/me', async () => {
    const vo = { userId: 1, username: 'admin', role: 'ADMIN', name: '管理员' }
    vi.mocked(get).mockResolvedValueOnce(vo)

    const result = await me()

    expect(get).toHaveBeenCalledWith('/auth/me')
    expect(result).toEqual(vo)
  })
})
