import dayjs from 'dayjs'

export function formatDateTime(value?: string | Date | number) {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

export function formatDate(value?: string | Date | number) {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD')
}

export function formatFileSize(bytes?: number) {
  if (bytes === undefined || bytes === null) return '-'
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
