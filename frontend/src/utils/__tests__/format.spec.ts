import { describe, expect, it } from 'vitest'
import { formatDate, formatDateTime, formatFileSize } from '@/utils/format'

describe('format utils', () => {
  describe('formatDateTime', () => {
    it('returns YYYY-MM-DD HH:mm for valid Date', () => {
      expect(formatDateTime(new Date(2024, 5, 15, 9, 30))).toBe('2024-06-15 09:30')
    })

    it('returns YYYY-MM-DD HH:mm for valid ISO string', () => {
      expect(formatDateTime('2024-06-15T09:30:00')).toBe('2024-06-15 09:30')
    })

    it("returns '-' for null, undefined or empty string", () => {
      expect(formatDateTime(null as unknown as string)).toBe('-')
      expect(formatDateTime(undefined)).toBe('-')
      expect(formatDateTime('')).toBe('-')
    })
  })

  describe('formatDate', () => {
    it('returns YYYY-MM-DD for valid Date', () => {
      expect(formatDate(new Date(2024, 5, 15))).toBe('2024-06-15')
    })

    it("returns '-' for falsy values", () => {
      expect(formatDate(undefined)).toBe('-')
      expect(formatDate('')).toBe('-')
    })
  })

  describe('formatFileSize', () => {
    it("returns '-' for null or undefined", () => {
      expect(formatFileSize(null as unknown as number)).toBe('-')
      expect(formatFileSize(undefined)).toBe('-')
    })

    it("returns '0 B' for 0", () => {
      expect(formatFileSize(0)).toBe('0 B')
    })

    it('returns B for values < 1024', () => {
      expect(formatFileSize(512)).toBe('512 B')
    })

    it('returns KB with two decimals', () => {
      expect(formatFileSize(1536)).toBe('1.5 KB')
    })

    it('returns MB with two decimals', () => {
      expect(formatFileSize(2 * 1024 * 1024)).toBe('2 MB')
    })
  })
})
