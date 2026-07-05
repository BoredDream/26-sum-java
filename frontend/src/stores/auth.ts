import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import type { LoginDTO, LoginVO, UserRole } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginVO | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!user.value)
  const role = computed(() => user.value?.role)
  const isStudent = computed(() => role.value === 'STUDENT')
  const isTeacher = computed(() => role.value === 'TEACHER')
  const isAdmin = computed(() => role.value === 'ADMIN')

  function getHomePath(role?: UserRole) {
    switch (role) {
      case 'STUDENT':
        return '/selection/my-team'
      case 'TEACHER':
        return '/topic/my-list'
      case 'ADMIN':
        return '/admin/dashboard'
      default:
        return '/login'
    }
  }

  async function login(dto: LoginDTO) {
    loading.value = true
    try {
      const vo = await authApi.login(dto)
      user.value = vo
      return vo
    } finally {
      loading.value = false
    }
  }

  async function fetchMe() {
    try {
      const vo = await authApi.me()
      user.value = vo
      return vo
    } catch {
      user.value = null
      return null
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      user.value = null
      window.location.href = '/login'
    }
  }

  function clear() {
    user.value = null
  }

  return {
    user,
    loading,
    isLoggedIn,
    role,
    isStudent,
    isTeacher,
    isAdmin,
    getHomePath,
    login,
    fetchMe,
    logout,
    clear,
  }
})
