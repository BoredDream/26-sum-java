import { get, post, put, del, uploadFile, downloadFile } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  AttendanceTaskVO,
  AttendanceTaskDetailVO,
  AttendanceTaskCreateDTO,
  AttendanceTaskUpdateDTO,
  AttendanceTaskQuery,
  AttendanceRecordVO,
  AttendanceSignDTO,
  AttendanceRecordQuery,
  MakeupApplyVO,
  MakeupCreateDTO,
  MakeupAuditDTO,
  MakeupQuery,
  AttendanceStatisticsVO,
  AttendanceStatisticsQuery,
} from '@/types/attendance'

// 考勤任务
export function createAttendanceTask(data: AttendanceTaskCreateDTO) {
  return post<number>('/attendance/task', data)
}

export function updateAttendanceTask(taskId: number, data: AttendanceTaskUpdateDTO) {
  return put<number>(`/attendance/task/${taskId}`, data)
}

export function deleteAttendanceTask(taskId: number) {
  return del<void>(`/attendance/task/${taskId}`)
}

export function finishAttendanceTask(taskId: number) {
  return post<void>(`/attendance/task/${taskId}/finish`)
}

export function getAttendanceTaskDetail(taskId: number) {
  return get<AttendanceTaskDetailVO>(`/attendance/task/${taskId}`)
}

export function queryAttendanceTaskPage(query: AttendanceTaskQuery) {
  return get<PageResult<AttendanceTaskVO>>('/attendance/task/page', { params: query })
}

// 考勤记录
export function signAttendance(data: AttendanceSignDTO) {
  return post<AttendanceRecordVO>('/attendance/record/sign', data)
}

export function updateAttendanceRecordStatus(
  recordId: number,
  data: { signStatus: number; remark?: string }
) {
  return put<void>(`/attendance/record/${recordId}/status`, data)
}

export function queryAttendanceRecordPage(query: AttendanceRecordQuery) {
  return get<PageResult<AttendanceRecordVO>>('/attendance/record/page', { params: query })
}

export function exportAttendanceRecords(query?: AttendanceRecordQuery) {
  return downloadFile('/attendance/record/export', { params: query })
}

// 补签申请
export function applyMakeup(data: MakeupCreateDTO) {
  return post<number>('/attendance/makeup', data)
}

export function reviewMakeup(applyId: number, data: MakeupAuditDTO) {
  return post<void>(`/attendance/makeup/${applyId}/review`, data)
}

export function queryMakeupPage(query: MakeupQuery) {
  return get<PageResult<MakeupApplyVO>>('/attendance/makeup/page', { params: query })
}

export function uploadMakeupProof(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return uploadFile<string>('/attendance/makeup/upload', formData)
}

// 考勤统计
export function getAttendanceStatistics(query?: AttendanceStatisticsQuery) {
  return get<AttendanceStatisticsVO>('/attendance/statistics', { params: query })
}
