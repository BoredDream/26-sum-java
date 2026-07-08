import { post, get } from '@/utils/request'
import type { LoginDTO, LoginVO } from '@/types/auth'

export function login(data: LoginDTO) {
  return post<LoginVO>('/auth/login', data)
}

export function logout() {
  return post<void>('/auth/logout')
}

export function me() {
  return get<LoginVO>('/auth/me')
}
