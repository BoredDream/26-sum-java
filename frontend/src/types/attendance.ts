import type { PageQuery } from './api'

export interface AttendanceTaskVO {
  taskId: number
  taskTitle: string
  taskType: number
  taskTypeName: string
  scopeType: number
  scopeTypeName: string
  scopeValue?: string
  scopeDisplayName?: string
  startTime: string
  endTime: string
  status: number
  statusName: string
  teacherId: number
  teacherName: string
  signedCount?: number
  totalCount?: number
  requireLocation?: number
  locationLng?: number
  locationLat?: number
  locationRadius?: number
  locationName?: string
  createTime: string
}

export interface AttendanceTaskDetailVO extends AttendanceTaskVO {
  description?: string
  records?: AttendanceRecordVO[]
}

export interface AttendanceTaskCreateDTO {
  taskTitle: string
  taskType: number
  scopeType: number
  scopeValue?: string | number
  startTime: string
  endTime: string
  description?: string
  requireLocation?: number
  locationLng?: number
  locationLat?: number
  locationRadius?: number
  locationName?: string
}

export interface AttendanceTaskUpdateDTO {
  taskTitle: string
  taskType: number
  scopeType: number
  scopeValue?: string | number
  startTime: string
  endTime: string
  description?: string
}

export interface AttendanceTaskQuery extends PageQuery {
  status?: number
  keyword?: string
}

export interface AttendanceRecordVO {
  recordId: number
  taskId: number
  taskTitle: string
  studentId: number
  studentNo: string
  studentName: string
  className?: string
  signTime?: string
  signStatus: number
  signStatusName: string
  isMakeup: number
  remark?: string
  createTime: string
}

export interface AttendanceSignDTO {
  taskId: number
  remark?: string
  signLng?: number
  signLat?: number
}

export interface AttendanceRecordQuery extends PageQuery {
  taskId?: number
  studentId?: number
  signStatus?: number
  startDate?: string
  endDate?: string
  keyword?: string
}

export interface MakeupApplyVO {
  applyId: number
  taskId: number
  taskTitle: string
  recordId?: number
  studentId: number
  studentNo: string
  studentName: string
  applyReason: string
  proofFilePath?: string
  auditStatus: number
  auditStatusName: string
  auditTeacherId?: number
  auditTeacherName?: string
  auditComment?: string
  auditTime?: string
  createTime: string
}

export type MakeupRequestVO = MakeupApplyVO

export interface MakeupCreateDTO {
  taskId: number
  recordId?: number
  applyReason: string
  proofFilePath?: string
}

export interface MakeupAuditDTO {
  auditStatus: number
  auditComment?: string
}

export interface MakeupQuery extends PageQuery {
  taskId?: number
  auditStatus?: number
}

export interface AttendanceStatisticsVO {
  totalCount: number
  normalCount: number
  lateCount: number
  absentCount: number
  makeupCount: number
  attendanceRate: number
  lateTimes: number
  absentTimes: number
}

export interface AttendanceStatisticsQuery {
  taskId?: number
  studentId?: number
  className?: string
  teamId?: number
  startDate?: string
  endDate?: string
}
