import { describe, expect, it } from 'vitest'
import { getMenuByPath, hasRoutePermission, menus, menusByRole } from '@/utils/permission'
import type { UserRole } from '@/types/auth'

describe('permission utils', () => {
  describe('menusByRole', () => {
    it('should export raw menus', () => {
      expect(menus.length).toBeGreaterThan(0)
    })

    it('should filter menus by role', () => {
      ;(['STUDENT', 'TEACHER', 'ADMIN'] as UserRole[]).forEach((role) => {
        expect(menusByRole[role].length).toBeGreaterThan(0)
        expect(menusByRole[role].every((m) => m.roles.includes(role))).toBe(true)
      })
    })

    it('should not include admin-only dashboard for student', () => {
      const studentPaths = menusByRole.STUDENT.map((m) => m.path)
      expect(studentPaths).not.toContain('/admin/dashboard')
    })
  })

  describe('hasRoutePermission', () => {
    it('returns true for an exact menu path matching role', () => {
      expect(hasRoutePermission('/selection/my-team', 'STUDENT')).toBe(true)
      expect(hasRoutePermission('/topic/my-list', 'TEACHER')).toBe(true)
      expect(hasRoutePermission('/admin/dashboard', 'ADMIN')).toBe(true)
    })

    it('returns true for dynamic hidden routes such as /topic/123', () => {
      expect(hasRoutePermission('/topic/123', 'STUDENT')).toBe(true)
      expect(hasRoutePermission('/topic/123', 'TEACHER')).toBe(true)
      expect(hasRoutePermission('/topic/123/files', 'TEACHER')).toBe(true)
    })

    it('returns false when role is undefined', () => {
      expect(hasRoutePermission('/selection/my-team', undefined)).toBe(false)
    })

    it('returns false for a path not allowed for the role', () => {
      expect(hasRoutePermission('/admin/dashboard', 'STUDENT')).toBe(false)
      expect(hasRoutePermission('/selection/my-team', 'TEACHER')).toBe(false)
      expect(hasRoutePermission('/selection/my-team', 'ADMIN')).toBe(false)
    })

    it('ignores trailing slashes on non-root paths', () => {
      expect(hasRoutePermission('/selection/my-team/', 'STUDENT')).toBe(true)
    })
  })

  describe('getMenuByPath', () => {
    it('returns the matching menu item', () => {
      const menu = getMenuByPath('/selection/my-team')
      expect(menu).toBeDefined()
      expect(menu?.path).toBe('/selection/my-team')
    })

    it('returns undefined for unknown path', () => {
      expect(getMenuByPath('/unknown/path')).toBeUndefined()
    })
  })
})
