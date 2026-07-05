import { ElMessage } from 'element-plus'
import { downloadFile } from './request'

export function extractFilename(disposition?: string): string {
  if (!disposition) return 'download'
  const utf8 = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8) return decodeURIComponent(utf8[1].replace(/\+/g, ' '))
  const ascii = disposition.match(/filename=["']?([^"';]+)["']?/i)
  return ascii ? ascii[1] : 'download'
}

export async function downloadByUrl(
  url: string,
  params?: Record<string, unknown>,
  defaultName?: string
) {
  try {
    const res = await downloadFile(url, { params })
    const blob = new Blob([res.data])
    const filename =
      extractFilename(res.headers['content-disposition']) || defaultName || 'download'
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
  } catch {
    ElMessage.error('下载失败')
  }
}
