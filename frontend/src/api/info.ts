import { get, post, put, del, uploadFile } from '@/utils/request'
import { downloadByUrl } from '@/utils/download'
import type { PageResult } from '@/types/api'
import type {
  NoticeVO,
  NoticeCreateDTO,
  NoticeUpdateDTO,
  NoticeQuery,
  StudentVO,
  StudentCreateDTO,
  StudentUpdateDTO,
  StudentImportResult,
  TeacherVO,
  TeacherCreateDTO,
  TeacherUpdateDTO,
  BackupVO,
  BackupCreateDTO,
  OperateLogVO,
  OperateLogQuery,
  DashboardStatisticsVO,
} from '@/types/info'

// 公告管理
export function queryNoticePage(query: NoticeQuery) {
  return get<PageResult<NoticeVO>>('/notices/page', { params: query })
}

export function getNoticeDetail(noticeId: number) {
  return get<NoticeVO>(`/notices/${noticeId}`)
}

export function createNotice(dto: NoticeCreateDTO, file?: File) {
  const formData = new FormData()
  formData.append('title', dto.title)
  formData.append('content', dto.content)
  formData.append('type', dto.type)
  if (dto.topFlag !== undefined) {
    formData.append('topFlag', String(dto.topFlag))
  }
  if (file) {
    formData.append('file', file)
  }
  return uploadFile<NoticeVO>('/notices', formData)
}

export function updateNotice(noticeId: number, dto: NoticeUpdateDTO, file?: File) {
  const formData = new FormData()
  if (dto.title !== undefined) formData.append('title', dto.title)
  if (dto.content !== undefined) formData.append('content', dto.content)
  if (dto.type !== undefined) formData.append('type', dto.type)
  if (dto.topFlag !== undefined) formData.append('topFlag', String(dto.topFlag))
  if (file) formData.append('file', file)
  return uploadFile<NoticeVO>(`/notices/${noticeId}`, formData)
}

export function deleteNotice(noticeId: number) {
  return del<void>(`/notices/${noticeId}`)
}

export function toggleNoticeTop(noticeId: number) {
  return post<void>(`/notices/${noticeId}/top`)
}

export function getNoticeDownloadUrl(noticeId: number) {
  return `/notices/${noticeId}/download`
}

export function downloadNoticeAttachment(noticeId: number) {
  return downloadByUrl(getNoticeDownloadUrl(noticeId))
}

// 学生管理
export function queryStudentPage(query: {
  keyword?: string
  status?: number
  pageNum?: number
  pageSize?: number
}) {
  return get<PageResult<StudentVO>>('/students/page', { params: query })
}

export function getStudentDetail(studentId: number) {
  return get<StudentVO>(`/students/${studentId}`)
}

export function createStudent(data: StudentCreateDTO) {
  return post<StudentVO>('/students', data)
}

export function updateStudent(studentId: number, data: StudentUpdateDTO) {
  return put<StudentVO>(`/students/${studentId}`, data)
}

export function deleteStudent(studentId: number) {
  return del<void>(`/students/${studentId}`)
}

export function resetStudentPassword(studentId: number) {
  return post<void>(`/students/${studentId}/reset-password`)
}

export function toggleStudentStatus(studentId: number) {
  return post<void>(`/students/${studentId}/toggle-status`)
}

export function exportStudents() {
  return downloadByUrl('/students/export', undefined, '学生信息.xlsx')
}

export function importStudents(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return uploadFile<StudentImportResult>('/students/import', formData)
}

export function changeStudentPassword(studentId: number, oldPwd: string, newPwd: string) {
  return post<void>('/students/change-password', null, {
    params: { oldPwd, newPwd, studentId },
  })
}

// 教师管理
export function queryTeacherPage(query: { keyword?: string; pageNum?: number; pageSize?: number }) {
  return get<PageResult<TeacherVO>>('/teachers/page', { params: query })
}

export function getTeacherDetail(teacherId: number) {
  return get<TeacherVO>(`/teachers/${teacherId}`)
}

export function createTeacher(data: TeacherCreateDTO) {
  return post<TeacherVO>('/teachers', data)
}

export function updateTeacher(teacherId: number, data: TeacherUpdateDTO) {
  return put<TeacherVO>(`/teachers/${teacherId}`, data)
}

export function deleteTeacher(teacherId: number) {
  return del<void>(`/teachers/${teacherId}`)
}

export function resetTeacherPassword(teacherId: number) {
  return post<void>(`/teachers/${teacherId}/reset-password`)
}

export function toggleTeacherRole(teacherId: number) {
  return post<void>(`/teachers/${teacherId}/toggle-role`)
}

export function exportTeachers() {
  return downloadByUrl('/teachers/export', undefined, '教师信息.xlsx')
}

export function changeTeacherPassword(teacherId: number, oldPwd: string, newPwd: string) {
  return post<void>('/teachers/change-password', null, {
    params: { oldPwd, newPwd, teacherId },
  })
}

// 数据备份
export function queryBackupPage(query: { pageNum?: number; pageSize?: number }) {
  return get<PageResult<BackupVO>>('/backups/page', { params: query })
}

export function createBackup(_dto?: BackupCreateDTO) {
  return post<BackupVO>('/backups')
}

export function restoreBackup(backupId: number) {
  return post<void>(`/backups/${backupId}/restore`)
}

export function deleteBackup(backupId: number) {
  return del<void>(`/backups/${backupId}`)
}

export function cleanupBackups() {
  return post<void>('/backups/cleanup')
}

export function getBackupDownloadUrl(backupId: number) {
  return `/backups/${backupId}/download`
}

export function downloadBackup(backupId: number) {
  return downloadByUrl(getBackupDownloadUrl(backupId))
}

// 操作日志
export function queryOperateLogPage(query: OperateLogQuery) {
  return get<PageResult<OperateLogVO>>('/logs/page', { params: query })
}

export function clearOperateLogs(days = 90) {
  return post<void>('/logs/clear', null, { params: { days } })
}

// 仪表盘统计
export function getDashboardStatistics() {
  return get<DashboardStatisticsVO>('/dashboard')
}
